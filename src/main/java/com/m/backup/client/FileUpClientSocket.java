package com.m.backup.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.winone.ftc.mtools.Log;
import com.m.backup.imps.Protocol;
import com.m.backup.beans.BackupFileInfo;
import com.m.backup.slice.*;
import com.m.tcps.c.FtcSocketClient;
import com.m.tcps.p.FtcTcpActionsAdapter;
import com.m.tcps.p.Session;
import com.m.tcps.p.SessionOperation;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.*;

import static com.m.backup.imps.Protocol.C_FILE_BACKUP_TRS_END;
import static com.m.backup.imps.Protocol.C_FILE_BACKUP_TRS_START;

/**
 * Created by user on 2017/11/23.
 */
public class FileUpClientSocket extends FtcTcpActionsAdapter{
    private final FtcSocketClient socketClient;
    private final String flag ;
    private long endUsingTime = System.currentTimeMillis();
    private final Gson gson = new Gson();
    private static final String CHARSET = "UTF-8";
    private static final int BUFFER_SIZE = 1024*16;
    private FBCThreadBySocketList fbcThreadBySocketList;
    public FileUpClientSocket(FBCThreadBySocketList fbcThreadBySocketList, InetSocketAddress serverAddress) throws IOException, InterruptedException {
        this.fbcThreadBySocketList = fbcThreadBySocketList;
        this.flag = String.format("文件同步客户端管道-%d >> ",fbcThreadBySocketList.getCurrentSize());
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
    public void receiveString(Session session, String message) {
        Map<String,String> map = gson.fromJson(message,Map.class);
        handle(map);
    }
    @Override
    public void connectClosed(Session session) {
        clear();
    }

    public boolean isConnected() {
        return socketClient.isAlive();
    }

    public void close() {
        socketClient.close();
    }

    public boolean isIdle(long ideaTime) {
        return (System.currentTimeMillis() - endUsingTime) > ideaTime;
    }

    public int getRemainTime(long ideaTime){
        return (int)((ideaTime - (System.currentTimeMillis() - endUsingTime) ));
    }

    //文件上传
    private void uploadFile() {
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
        if (protocol.equals(Protocol.S_FILE_BACKUP_QUEST_ACK)){
            serverBackupQuestAck(map);
        }else if (protocol.equals(Protocol.S_FILE_BACKUP_TRS_OVER)){
            transOver();
        }
    }

    //客户端文件请求回执
    private void serverBackupQuestAck(Map<String, String> map) {
        int sliceSize = Integer.parseInt(map.get("block"));
        String slice  = map.remove("slice");
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
            SliceScrollResult result  = SliceUtil.scrollCheck(table,new File(cur_up_file.getFullPath()),sliceSize);
            if (result.getDifferentSize()>0){
                map.put("translate","diff"); //差异传输
                backup(map,result);
            }else{
                if (result.getSameSize() == list.size()){ //不用传输
                    clear();
                }else{
                    //全量传输
                    map.put("translate","all");
                    backup(map,null);
                }
            }
        }
    }

    /**
     *  文件数据传输
      文件大小
      sep 1 ->告知文件大小 - 服务器创建此文件的.backup文件
      set 2 ->传输类型, 差异传输 ? 全部传输
      set 3 如果增量传输 发送差异的块position-length 发送相同的块集合 客户端文件position-文件长度-服务端文件position
        same_block = srcPosition,dtfPosition,length#...
        diff_boloc = position#position#position
        如果是全部传输 直接发送数据
      set 4 通知关闭连接
     */
    private void backup(Map<String, String> map,SliceScrollResult result) {
//        Log.println(flag,cur_up_file);
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
            byte[]  buffer = new byte[BUFFER_SIZE];
            int len;
            if (result==null){
                //全部传输
                randomAccessFile.seek(0);
                while( ( len = randomAccessFile.read(buffer) )> 0){
                    op.writeBytes(buffer,0,len);
                }
            }else{
                //差异传输
                long count;
                for (SliceMapper slice : result.getList_diff()){
                    randomAccessFile.seek(slice.getPosition());
                    count = slice.getLength();
                    while (count>0){
                        len = (int) Math.min(buffer.length,count);
                        randomAccessFile.read(buffer,0,len);
                        op.writeBytes(buffer,0, len);
                        count = count - len;
                    }
                }
            }
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
        //通知 连接池管理队列
        fbcThreadBySocketList.notifyComplete();
    }

    public boolean validServerAddress(InetSocketAddress socketAddress) {
        try {
            return socketClient.getSocket().getRemoteAddress().equals(socketAddress);
        } catch (IOException e) {
        }
        return false;
    }
}
