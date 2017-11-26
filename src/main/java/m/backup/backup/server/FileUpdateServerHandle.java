package m.backup.backup.server;

import com.google.gson.Gson;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import m.backup.backup.imps.Protocol;
import m.backup.backup.slice.*;
import m.tcps.p.FtcTcpActionsAdapter;
import m.tcps.p.Session;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/11/23.
 */
public class FileUpdateServerHandle extends FtcTcpActionsAdapter {

    private final FtcBackupServer ftcBackupServer;
    private RandomAccessFile randomAccessFile;
    private static final String SUFFEX= ".bkup";
    private final Gson gson = new Gson();

    public FileUpdateServerHandle(FtcBackupServer ftcBackupServer) {
        this.ftcBackupServer = ftcBackupServer;
    }

    @Override
    public void receiveString(Session session, String message) {
            Map<String,String> map = gson.fromJson(message,Map.class);
            //Log.println("接受消息: "+ map);
            handle(session,map);
    }



    @Override
    public void connectClosed(Session session) {
//        Log.println("连接关闭: ");
        closeResource();
    }

    @Override
    public void error(Session session, Throwable throwable, Exception e) {
//        Log.println(session, throwable, e);
    }



    private void handle(Session session,Map<String, String> map) {
        String protocol = map.get("protocol");
        try {
            Log.println(session.getSocket().getRemoteAddress()," >> ",protocol);
        } catch (IOException e) {
        }
        if (protocol.equals(Protocol.C_FILE_BACKUP_QUEST)){
            ClientFileBackupQuest(session,map);
        }else if (protocol.equals(Protocol.C_FILE_BACKUP_TRS_START)){
            receiveFile(map);
        }else if (protocol.equals(Protocol.C_FILE_BACKUP_TRS_END)){
            receiveFileOver(session,map);
        }
    }




    /**
     * 客户端文件同步请求
     * @param map
     */
    private void ClientFileBackupQuest(Session  session,Map<String, String> map)  {
        String dir_path = ftcBackupServer.getDirectory()+map.get("path");
        map.put("protocol",Protocol.S_FILE_BACKUP_QUEST_ACK);
        String slice = "Node";
        File dir_file = new File(dir_path);
        if (!dir_file.exists()){
            //目录不存在,创建目录
            Log.println("目录不存在: "+ dir_path);
            dir_file.mkdirs();
        }else{
            File file = new File(dir_path+map.get("filename"));
            if (file.exists()){
                //切片
                int sliceSize = Integer.parseInt(map.get("block"));
                ArrayList<SliceInfo> arrayList = SliceUtil.fileSliceInfoList(file,sliceSize);
                if (arrayList!=null){
                    slice = gson.toJson(arrayList);
                }
            }
        }
        map.put("slice",slice);
//        Log.println("处理分片完成-告知客户端信息中..");
        session.getOperation().writeString(gson.toJson(map),map.get("charset"));
    }


    private boolean isDiffTranslate = false;
    private List<SliceMapper2> diff_slice_list = null;
    private int index = 0;
    private List<SliceMapper2> same_slice_list = null;
    //关闭资源
    private void closeResource() {
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
                randomAccessFile= null;
            } catch (IOException e) {
            }
        }
        isDiffTranslate = false;
        index= 0;
        if (diff_slice_list!=null) {
            diff_slice_list.clear();
            diff_slice_list = null;
        }
        if (same_slice_list!=null){
            same_slice_list.clear();
            same_slice_list=null;
        }


    }
    //接受客户端发送的传输开始请求
    private void receiveFile(Map<String, String> map) {
        closeResource();
        //获取备份后缀的文件
        String fs_path = ftcBackupServer.getDirectory()+map.get("path")+map.get("filename")+SUFFEX;
        //获取文件大小
        long length = Long.valueOf(map.get("length"));
        String trs_type = map.get("translate");//传输类型 增量或者全部
        if(trs_type.equals("diff")){
            isDiffTranslate = true;
            index = 0;
            String diff_block_str = map.get("different");
            String[] arr = diff_block_str.split("#");
            String[] arr_sub;
            SliceMapper2 slice;
            diff_slice_list = new ArrayList<>();
            for (String str:arr){
                arr_sub = str.split("-");
                slice = new SliceMapper2();
                slice.setPosition(Long.parseLong(arr_sub[0]));
                slice.setLength(Long.parseLong(arr_sub[1]));
                diff_slice_list.add(slice);
            }
            String same_block_str = map.get("same");
            arr = same_block_str.split("#");
            same_slice_list = new ArrayList<>();
            for (String str:arr){
                arr_sub = str.split("-");
                slice = new SliceMapper2();
                slice.setPosition(Long.parseLong(arr_sub[0]));
                slice.setLength(Long.parseLong(arr_sub[1]));
                slice.setMapperPostion(Long.parseLong(arr_sub[2]));
                same_slice_list.add(slice);
            }
        }
        try {

            randomAccessFile = new RandomAccessFile(fs_path,"rw");
//            randomAccessFile.setLength(length);
            if (trs_type.equals("all")){
                randomAccessFile.seek(0);
            }else{
                randomAccessFile.seek(diff_slice_list.get(index).getPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //接受完成
    private void receiveFileOver(Session session,Map<String, String> map) {

        //获取备份后缀的文件
        String fs_path = ftcBackupServer.getDirectory()+map.get("path")+map.get("filename");

        if (randomAccessFile!=null){
            if (isDiffTranslate){
                RandomAccessFile localSrcBlock = null;
                try {
                    //复制 文件块
                    localSrcBlock = new RandomAccessFile(fs_path, "r");
                    byte[] buf = new byte[1024];

                    for (SliceMapper2 slice : same_slice_list) {
                        localSrcBlock.seek(slice.getMapperPostion());
                        localSrcBlock.read(buf,0, (int) slice.getLength());
                        randomAccessFile.seek(slice.getPosition());
                        randomAccessFile.write(buf,0,(int) slice.getLength());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        localSrcBlock.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        closeResource();
        if (FileUtil.rename(new File(fs_path+SUFFEX), new File(fs_path))){
            FileUtil.deleteFile(fs_path+SUFFEX);
        }
        map.remove("path");
        map.remove("filename");
        map.remove("translate");
        map.remove("different");
        map.remove("same");
        map.remove("length");
        map.put("protocol",Protocol.S_FILE_BACKUP_TRS_OVER);
        session.getOperation().writeString(gson.toJson(map),map.get("charset"));
        session.clear();//不调用将无法释放TCP连接 接收或者发送的缓冲区
    }

    private long capacity = 0;
    @Override
    public void receiveBytes(Session session, byte[] bytes) {
            if (randomAccessFile!=null){
                try {
//                    Log.println(Arrays.toString(bytes));
                    randomAccessFile.write(bytes);

                    if (isDiffTranslate){ //如果差异传输 - 特别处理
                        capacity +=bytes.length;
                        if (capacity == diff_slice_list.get(index).getLength()){
                            capacity = 0;
                            index++;
                            if (index<diff_slice_list.size()){
                                randomAccessFile.seek(diff_slice_list.get(index).getPosition());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }


}
