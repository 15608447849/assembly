package bottle.backup.server;

import bottle.backup.slice.SliceInfo;
import bottle.backup.slice.SliceMapper2;
import bottle.backup.slice.SliceUtil;
import bottle.ftc.tools.Log;
import bottle.ftc.tools.MD5Util;
import bottle.tcps.p.FtcTcpActionsAdapter;
import bottle.tcps.p.Session;
import com.google.gson.Gson;
import bottle.ftc.tools.FileUtil;
import bottle.backup.imps.Protocol;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 2017/11/23.
 */
public class FileUpServerHandle extends FtcTcpActionsAdapter {



    private FtcBackupServer ftcBackupServer;
    private RandomAccessFile randomAccessFile;
    private static final String SUFFER = ".bup";
    private final Gson gson = new Gson();
    private String flag ;

    private boolean isDiffTranslate = false;//是否差异传输
    private List<SliceMapper2> diff_slice_list = null;//差异传输 客户端传输过来的差异块
    private List<SliceMapper2> same_slice_list = null;//差异传输 当前本地存在的相同数据块
    private long capacity = 0;//差异传输 当前差异块应接收的数据总量
    private int index = 0;//差异传输 - 当前差异块下标

    public FileUpServerHandle(FtcBackupServer ftcBackupServer) {
        this.ftcBackupServer = ftcBackupServer;
    }

    public void bindSession( Session session){
        this.flag = session.getSocketImp().getSocket().toString()+"-"+getClass().getSimpleName()+"-"+ftcBackupServer.getSockSer().getCurrentClientSize()+" >>  ";
//        Log.i(flag,"服务端文件上传处理-创建");
        session.getSocketImp().setAction(this);//绑定传输管道
        session.getSocketImp().getSession().getOperation().writeString("start","utf-8");
    }



    @Override
    public void receiveString(Session session, String message) {
//            Log.i(flag,message);
            Map<String,String> map = gson.fromJson(message,Map.class);
            handle(session,map);
    }

    @Override
    public void connectClosed(Session session) {
        closeResource();
//        Log.i(flag,"连接关闭,资源清理");
    }

    /**
     * 处理
     */
    private void handle(Session session,Map<String, String> map) {
        String protocol = map.get("protocol");
        if (protocol.equals(Protocol.C_FILE_BACKUP_QUEST)){
            backupRequest(session,map);
        }else if (protocol.equals(Protocol.C_FILE_BACKUP_TRS_START)){
            receiveFileStart(session,map);
        }else if (protocol.equals(Protocol.C_FILE_BACKUP_TRS_END)){
            receiveFileEnd(session,map);
        }else if (protocol.equals(Protocol.C_FILE_LIST_VERIFY_QUEST)){
            verifyFileList(session,map);
        }
    }



    /**
     * 客户端文件同步请求
     * @param map
     */
    private void backupRequest(Session  session, Map<String, String> map)  {
        String dir_path = ftcBackupServer.getDirectory()+map.get("path");
        map.put("protocol",Protocol.S_FILE_BACKUP_QUEST_ACK);
        String slice = "Node";
        File dir_file = new File(dir_path);
        if (!dir_file.exists()){
            //目录不存在,创建目录
            dir_file.mkdirs();
        }else{
            String file_name = map.get("filename");
            if (file_name.endsWith(SUFFER)){
                //此文件后缀不进行同步
                map.put("protocol",Protocol.S_FILE_BACKUP_TRS_OVER);
                session.getOperation().writeString(gson.toJson(map),map.get("charset"));
                return;
            }
            File file = new File(dir_path+file_name);

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
        session.getOperation().writeString(gson.toJson(map),map.get("charset"));
    }

    //关闭资源
    private void closeResource() {
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
            } catch (IOException e) {
            }finally {
                randomAccessFile= null;
            }
        }

        isDiffTranslate = false;
        index= 0;
        capacity=0;
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
    private void receiveFileStart(Session session, Map<String, String> map) {
        closeResource();
        String fs_path = ftcBackupServer.getDirectory()+map.get("path")+map.get("filename") + SUFFER; //获取备份后缀的文件
        long length = Long.valueOf(map.remove("length")); //获取文件大小
        if (FileUtil.checkFile(fs_path)){
            //存在一个备份文件
            if (!FileUtil.deleteFile(fs_path)){
                //无法删除
                map.put("protocol",Protocol.S_FILE_BACKUP_TRS_OVER);
                session.getOperation().writeString(gson.toJson(map),map.get("charset"));
                return;
            }
        }
        String trs_type = map.remove("translate");//传输类型 增量或者全部
        if(trs_type.equals("diff")){
            isDiffTranslate = true;
            index = 0;
            String diff_block_str = map.remove("different");
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
            String same_block_str = map.remove("same");
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
            randomAccessFile.setLength(length);
            if (trs_type.equals("all")){ //全量
                randomAccessFile.seek(0);
            }else{ //增量
                randomAccessFile.seek(diff_slice_list.get(index).getPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //接受完成 (客户端通知 文件传输完成)
    private void receiveFileEnd(Session session, Map<String, String> map) {

        if (randomAccessFile==null) return;

        //获取备份后缀的文件
        String fs_path = ftcBackupServer.getDirectory()+map.remove("path")+map.remove("filename");

        if (isDiffTranslate){
            RandomAccessFile localSrcBlock = null;
            try {
                //复制 文件块
                localSrcBlock = new RandomAccessFile(fs_path, "r");

                byte[] buf = new byte[Integer.parseInt(map.remove("block"))];

                for (SliceMapper2 slice : same_slice_list) {
                    localSrcBlock.seek(slice.getMapperPostion());
                    localSrcBlock.read(buf, 0, (int) slice.getLength());
                    randomAccessFile.seek(slice.getPosition());
                    randomAccessFile.write(buf, 0, (int) slice.getLength());
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

        closeResource();
        if (FileUtil.rename(new File(fs_path+ SUFFER), new File(fs_path))){
            FileUtil.deleteFile(fs_path+ SUFFER);
        }

        map.put("protocol",Protocol.S_FILE_BACKUP_TRS_OVER);
        session.getOperation().writeString(gson.toJson(map),map.get("charset"));
        ftcBackupServer.complete(new File(fs_path));
    }

    //接受文件
//    long cpos = 0;
    @Override
    public void receiveBytes(Session session, byte[] bytes) {
//            cpos+=bytes.length;
//            Log.i("已传输字节数:"  + cpos);
            if (randomAccessFile!=null){
                try {
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


    //客户端 文件列表检查请求
    private void verifyFileList(Session session, Map<String, String> map) {
            String[] arr =  map.remove("list").split(";");
            String dir = ftcBackupServer.getDirectory();
            String[] temp;
            File file;
            String md5;
            StringBuffer sb = new StringBuffer();
            for (String str : arr){
                temp = str.split(",");
                file = new File(dir+temp[0]);
                //判断本地是否存在文件
                if (file.exists()){
                    try {
                        md5 = MD5Util.getFileMd5ByString(file);
                        if (md5.equals(temp[1])){
                           continue;
                        }
                    } catch (Exception ignored) { }
                }
                //文件需要同步
                sb.append(temp[1]).append(";");
            }
            if (sb.length() > 0 )  {
                map.put("valid","true");
                sb.deleteCharAt(sb.length() - 1);
                map.put("md5s",sb.toString());
            }else{
                map.put("valid","false");
            }
        map.put("protocol",Protocol.S_FILE_LIST_VERIFY_ACK);
        session.getOperation().writeString(gson.toJson(map),map.get("charset"));
    }



}
