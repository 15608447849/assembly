package m.backup.backup.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.winone.ftc.mtools.Log;
import m.backup.backup.imps.Protocol;
import m.backup.backup.beans.BackupFileInfo;
import m.backup.backup.slice.*;
import m.tcps.c.FtcSocketClient;
import m.tcps.p.FtcTcpActionsAdapter;
import m.tcps.p.Session;
import m.tcps.p.SessionOperation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.*;

import static m.backup.backup.imps.Protocol.C_FILE_BACKUP_TRS_END;
import static m.backup.backup.imps.Protocol.C_FILE_BACKUP_TRS_START;

/**
 * Created by user on 2017/11/23.
 */
public class FileUpdateSocketClient  extends FtcTcpActionsAdapter{
    private final FtcSocketClient socketClient;
    private final String flag ;
    private long endUsingTime = System.currentTimeMillis();
    private final Gson gson = new Gson();
    private static final String CHARSET = "UTF-8";
    public FileUpdateSocketClient(int ID,InetSocketAddress serverAddress) {
        this.flag = String.format("文件同步客户端管道-%d >> ",ID);
        this.socketClient = new FtcSocketClient(serverAddress,this);
        this.socketClient.connectServer();//连接服务器
    }

    public String getFlag() {
        return flag;
    }

    private BackupFileInfo cur_up_file; //当前待上传文件信息

    private boolean isUsing = false; //是否在使用中

    public void setCur_up_file(BackupFileInfo cur_up_file) {
        this.cur_up_file = cur_up_file;
        isUsing = true;//设置使用中
        uploadFile();
    }


    public boolean isUsing() {
        return isUsing;
    }


    @Override
    public void connectSucceed(Session session) {
        //Log.println(flag," 连接成功 ",session.getSocketImp().getInfo());
    }

    @Override
    public void receiveString(Session session, String message) {

        Map<String,String> map = gson.fromJson(message,Map.class);
        //Log.println(flag," 收到服务器信息: ",map);
        handle(map);
    }



    @Override
    public void connectClosed(Session session) {
//        Log.println(flag," 连接关闭.");
        clear();
    }

    public boolean isConnected() {
        return socketClient.isAlive();
    }

    public void close() {
//        Log.println(flag," 主动关闭连接.");
        socketClient.close();
    }

    public boolean isIdle(long ideaTime) {
        return (System.currentTimeMillis() - endUsingTime) > ideaTime;
    }


    //文件上传
    private void uploadFile() {
            Log.println(flag,"  准备上传文件: ", cur_up_file);
            //1. 通知服务器, 发送文件 相对路径,文件名
            Map<String,String> map = new HashMap<>();
            map.put("charset",CHARSET);
            map.put("protocol", Protocol.C_FILE_BACKUP_QUEST);
            map.put("path",cur_up_file.getRel_path());
            map.put("filename",cur_up_file.getFileName());
            map.put("block",String.valueOf(SliceUtil.sliceSizeConvert(cur_up_file.getFileLength())));
            socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET);
    }

    //处理
    private void handle(Map<String, String> map) {
        String protocol = map.get("protocol");
        Log.println(flag,protocol);
        if (protocol.equals(Protocol.S_FILE_BACKUP_QUEST_ACK)){
            serverBackupQuestAck(map);
        }else if (protocol.equals(Protocol.S_FILE_BACKUP_TRS_OVER)){
            transOver();
        }
    }



    //客户端文件请求回执
    private void serverBackupQuestAck(Map<String, String> map) {
        int sliceSize = Integer.parseInt(map.remove("block"));

        String slice  = map.get("slice");
        if (slice.equals("Node")){
            //全量传输 0-末尾
            map.put("translate","all");
            backup(map,null);
        }else{
            //滚动对比差异
            ArrayList<SliceInfo> list = gson.fromJson(slice,new TypeToken<ArrayList<SliceInfo>>(){}.getType());
            //分组
            Hashtable<String,LinkedList<SliceInfo>> table = SliceUtil.sliceInfoToTable(list);
            //滚动检测
            Log.println(flag,"文件片段滚动检测...");
            SliceScrollResult result  = SliceUtil.scrollCheck(table,new File(cur_up_file.getFullPath()),sliceSize);
            if (result.getDifferentSize()>0){
                map.put("translate","diff"); //差异传输
                backup(map,result);
            }else{
                if (result.getSameSize() == list.size()){
                    Log.println(flag," 不需要传输.");
                    clear();
                }else{
                    //全量传输
                    map.put("translate","all");
                    backup(map,null);
                }
            }
        }
    }

    private void backup(Map<String, String> map,SliceScrollResult result) {
        map.remove("slice");
        //文件数据传输
        // 文件大小
        // sep 1 ->告知文件大小 - 服务器创建此文件的.backup文件
        // set 2 ->传输类型, 差异传输 ? 全部传输
        // set 3 如果增量传输 发送差异的块position-length 发送相同的块集合 客户端文件position-文件长度-服务端文件position
        //  same_block = srcPosition,dtfPosition,length#...
        //  diff_boloc = position#position#position
        //  如果是全部传输 直接发送数据
        // set 4 通知关闭连接
        if (result!=null){
            String diff_block_str = result.getDifferentBlockSequence();
            map.put("different",diff_block_str);
            String same_block_str = result.getSameBlockSequence();
            map.put("same",same_block_str);
        }
        map.put("length",String.valueOf(cur_up_file.getFileLength()));
        map.put("protocol",C_FILE_BACKUP_TRS_START);
        SessionOperation op =  socketClient.getSession().getOperation();
        try{
            op.writeString(gson.toJson(map),CHARSET); //开始传输

            RandomAccessFile randomAccessFile = cur_up_file.getRandomAccessFile();
            byte[]  buffer = new byte[1024*16];
            int len;
            if (result==null){
                //全部传输
                Log.println(flag," 全量传输.");
                randomAccessFile.seek(0);
                while( ( len = randomAccessFile.read(buffer) )> 0){
//                    Log.println(flag,"   传输..."+len +" : "+ Arrays.toString(buffer));
                    op.writeBytes(buffer,0,len);
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }else{
                //差异传输
                Log.println(flag," 差异传输.");
                long count = 0;
                for (SliceMapper slice : result.getList_diff()){
//                    Log.println(flag,"   传输 "+slice);
                    randomAccessFile.seek(slice.getPosition());
                    count = slice.getLength();
                    while (count>0){
                        len = (int) Math.min(buffer.length,count);

                        randomAccessFile.read(buffer,0,len);
//                        Log.println(flag,"   传输..."+len +" : "+ Arrays.toString(buffer));
                        op.writeBytes(buffer,0, len);
                        count = count - len;
                    }
                }

            }
            map.remove("translate");
            map.remove("different");
            map.remove("same");
            map.remove("length");

            map.put("protocol",C_FILE_BACKUP_TRS_END);
            //传输完成
            op.writeString(gson.toJson(map),CHARSET); //结束传输
            cur_up_file.clear();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    //传输完成
    private void transOver() {
        socketClient.getSession().clear();
        clear();
    }

    private void clear() {
        //设置 未使用状态, 设置最后使用时间,移除资源
        endUsingTime = System.currentTimeMillis();
        isUsing = false;
        if (cur_up_file!=null){
            cur_up_file.clear();
            cur_up_file=null;
        }
    }

}
