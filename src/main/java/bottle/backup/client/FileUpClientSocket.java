package bottle.backup.client;

import bottle.backup.slice.SliceInfo;
import bottle.backup.slice.SliceMapper;
import bottle.backup.slice.SliceScrollResult;
import bottle.backup.slice.SliceUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import bottle.ftc.tools.Log;
import bottle.backup.imps.Protocol;
import bottle.backup.beans.BackupFile;

import bottle.tcps.c.FtcSocketClient;
import bottle.tcps.p.FtcTcpActionsAdapter;
import bottle.tcps.p.Session;
import bottle.tcps.p.SessionOperation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.*;

import static bottle.backup.imps.Protocol.*;

/**
 * Created by user on 2017/11/23.
 */
public class FileUpClientSocket extends FtcTcpActionsAdapter{
    private final FtcSocketClient socketClient;
    private final String flag ;
    private long endUsingTime = System.currentTimeMillis();
    private final Gson gson = new Gson();
    private static final String CHARSET = "UTF-8";
    private static final int BUFFER_SIZE =  1024;//1024 * 1024 * 8; //8M
    private final FBCThreadBySocketList fbcThreadBySocketList;

    private volatile RandomAccessFile randomAccessFile;


    public FileUpClientSocket(FBCThreadBySocketList fbcThreadBySocketList, InetSocketAddress serverAddress) throws IOException, InterruptedException {
        this.fbcThreadBySocketList = fbcThreadBySocketList;
        this.flag = String.format(" 文件同步客户端管道-%d -> %s",fbcThreadBySocketList.getCurrentSize(),serverAddress.toString());
        this.socketClient = new FtcSocketClient(serverAddress,this);
        this.socketClient.connectServer();//连接服务器
        lock();
    }


    private synchronized RandomAccessFile getRandomAccessFile() throws FileNotFoundException {
        if (randomAccessFile == null) randomAccessFile = new RandomAccessFile(curFile.getFullPath(),"r");
        return randomAccessFile;
    }

    private synchronized void clearRandomAccessFile() {
        //关闭本地文件流等 清理任务
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                randomAccessFile=null;
            }
        }
    }

    private void lock() {
        synchronized (this){
            try{
               this.wait();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private void unLock(){
        synchronized (this){
            try{
                this.notify();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getFlag() {
        return flag;
    }

    private BackupFile curFile; //当前待上传文件信息

    private List<BackupFile> curList;//当前待上传的文件组信息

    private volatile boolean isUsing = false; //是否在使用中

    public void setCurFile(BackupFile curFile) {
        if (isUsing) return;
        isUsing = true;//设置使用中
        this.curFile = curFile;
        uploadFile();
    }

    public boolean isUsing() {
        return isUsing;
    }

    @Override
    public void connectSucceed(Session session) {
        super.connectSucceed(session);
    }

    @Override
    public void receiveString(Session session, String message) {
        if (message.equals("start")){
            unLock();
            return;
        }
        Map map = gson.fromJson(message,Map.class);
        handle(map);
    }

    @Override
    public void connectClosed(Session session) {
        transOver(true);
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


    //文件上传开始点
    private void uploadFile() {
            endUsingTime = System.currentTimeMillis();
            //1. 通知服务器, 发送文件 相对路径,文件名 ,分片信息
            Map<String,String> map = new HashMap<>();
            map.put("charset",CHARSET);
            map.put("protocol", Protocol.C_FILE_BACKUP_QUEST);
            map.put("path", curFile.getRelPath());
            map.put("filename", curFile.getFileName());
            map.put("block",String.valueOf(SliceUtil.sliceSizeConvert(curFile.getFileLength())));
            socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET);

    }

    //处理
    private void handle(Map<String, String> map) {
        endUsingTime = System.currentTimeMillis();
        String protocol = map.get("protocol");
//        Log.i(flag+" "+protocol);
        if (protocol.equals(Protocol.S_FILE_BACKUP_QUEST_ACK)){
            serverBackupQuestAck(map);
        }else if (protocol.equals(Protocol.S_FILE_BACKUP_TRS_OVER)){
            transOver(false);
        }else if (protocol.equals(S_FILE_LIST_VERIFY_ACK)){
            fileListVerifyAck(map);
        }else if (protocol.equals(S_FILE_BACKUP_NOTIFY_STREAM)){
            transferStream(map);
        }
    }



    private SliceScrollResult result;
    // 服务端响应上传文件请求 ,告知服务端本地是否存在文件及文件的分片情况
    private void serverBackupQuestAck(Map<String, String> map){
        endUsingTime = System.currentTimeMillis();
        String slice  = map.remove("slice");//服务器对文件的分片信息
        int sliceSize = Integer.parseInt(map.get("block")); //服务器对文件分片大小
        if (slice.equals("Node")){
            //通知 - 全量传输
            map.put("translate","all");
        }else{
            //计算增量
            //滚动对比差异
            ArrayList<SliceInfo> list = gson.fromJson(slice,new TypeToken<ArrayList<SliceInfo>>(){}.getType());
            //分组
            Hashtable<String,LinkedList<SliceInfo>> table = SliceUtil.sliceInfoToTable(list);
            //滚动检测
            result  = SliceUtil.scrollCheck(table,new File(curFile.getFullPath()),sliceSize);
            if (result.getDifferentSize()>0){
                map.put("translate","diff"); //差异传输
                String diff_block_str = result.getDifferentBlockSequence();
                map.put("different",diff_block_str);
                String same_block_str = result.getSameBlockSequence();
                map.put("same",same_block_str);
            }else{
                if (result.getSameSize() == list.size()){
                    result = null;
                    //不用传输
                    //通知服务器完成
                    notifyEnd(map);
                    transOver(false);
                    return;
                }else{
                    //全量传输
                    map.put("translate","all");
                    result = null;
                }
            }
        }
        map.put("length",String.valueOf(curFile.getFileLength()));
        map.put("protocol",C_FILE_BACKUP_TRS_START);
        socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET); //通知开始传输
    }

    //流传输
    private void transferStream(Map<String, String> map) {
        try{
            SessionOperation op =  socketClient.getSession().getOperation();

            long time = System.currentTimeMillis();
            //等待服务端通知 - 最多等待30秒
            RandomAccessFile randomAccessFile = getRandomAccessFile();
            byte[] bytes = new byte[BUFFER_SIZE];
            int len;
            if (result==null){
                //全部传输
                randomAccessFile.seek(0);
                long pos = 0;
                while( isUsing && ( len = randomAccessFile.read(bytes) )> 0){
                    pos += len;
                    op.writeBytes(bytes,0,len);
//                    Log.i(Thread.currentThread(),flag," "+curFile," 单次量 : "+ len,
//                            " 进度: "+ String.format("%.2f",(pos * 1.0f / curFile.getFileLength())));
//                    endUsingTime = System.currentTimeMillis();
                }
            }else{
                //差异传输
                long count;
                for (SliceMapper slice : result.getList_diff()){
                    if (!isUsing) return;
                    randomAccessFile.seek(slice.getPosition());
                    count = slice.getLength();
                    while (count>0){
                        len = (int) Math.min(bytes.length,count);
                        randomAccessFile.read(bytes,0,len);
                        op.writeBytes(bytes,0, len);
                        count = count - len;
                        endUsingTime = System.currentTimeMillis();
                    }
                }
            }

            if (isUsing) Log.i(
                    Thread.currentThread(),flag," 结束 ", curFile.getFullPath(),
                    " 大小:"+curFile.getFileLength() +
                            " 时间:" +(System.currentTimeMillis() - time)
            );

            Thread.sleep(5 * 1000);
            notifyEnd(map);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void notifyEnd(Map<String, String> map) {
        //通知服务器传输完成
        map.put("protocol",C_FILE_BACKUP_TRS_END);
        socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET); //结束传输
    }

    //客户端文件请求回执
    /*private void serverBackupQuestAck(Map<String, String> map) {
        endUsingTime = System.currentTimeMillis();
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
            SliceScrollResult result  = SliceUtil.scrollCheck(table,new File(curFile.getFullPath()),sliceSize);
            if (result.getDifferentSize()>0){
                map.put("translate","diff"); //差异传输
                backup(map,result);
            }else{
                if (result.getSameSize() == list.size()){
                    //不用传输
                    //通知服务器完成
                    transOver(false);
                }else{
                    //全量传输
                    map.put("translate","all");
                    backup(map,null);
                }
            }
        }
    }*/

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
    /*private void backup(Map<String, String> map,SliceScrollResult result) {
        endUsingTime = System.currentTimeMillis();
        if (result!=null){
            String diff_block_str = result.getDifferentBlockSequence();
            map.put("different",diff_block_str);
            String same_block_str = result.getSameBlockSequence();
            map.put("same",same_block_str);
        }
        map.put("length",String.valueOf(curFile.getFileLength()));
        map.put("protocol",C_FILE_BACKUP_TRS_START);
        socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET); //通知开始传输

        try{
            SessionOperation op =  socketClient.getSession().getOperation();

            //等待服务端通知 - 最多等待30秒

            RandomAccessFile randomAccessFile = getRandomAccessFile();
            byte[] bytes = new byte[BUFFER_SIZE];
            int len;
            if (result==null){
                //全部传输
                randomAccessFile.seek(0);
//                long pos = 0;
                while( isUsing && ( len = randomAccessFile.read(bytes) )> 0){
//                    pos += len;
//                    Log.i(Thread.currentThread(),flag," "+curFile," 单次量 : "+ len," 进度: "+ String.format("%.2f",((double)pos/ curFile.getFileLength())));
                    op.writeBytes(bytes,0,len);
                    endUsingTime = System.currentTimeMillis();
                }
            }else{
                //差异传输
                long count;
                for (SliceMapper slice : result.getList_diff()){
                    if (!isUsing) return;
                    randomAccessFile.seek(slice.getPosition());
                    count = slice.getLength();
                    while (count>0){
                        len = (int) Math.min(bytes.length,count);
                        randomAccessFile.read(bytes,0,len);
                        op.writeBytes(bytes,0, len);
                        count = count - len;
                        endUsingTime = System.currentTimeMillis();
                    }
                }
            }
            double sv = ( curFile.getFileLength() / 1024.0f )/ ((System.currentTimeMillis() - endUsingTime) / 1000.0f);
            if (isUsing) Log.i(Thread.currentThread(),flag," 结束 ", curFile.getFullPath()," ",
                    sv > 0.0? " 速度 = " + String.format("%.2f kb/s",sv) : "");

            //通知服务器传输完成
            map.put("protocol",C_FILE_BACKUP_TRS_END);
            socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET); //结束传输

        }catch (Exception e){
            e.printStackTrace();
        }
    }*/


    private void clearCurFile(){
        clearRandomAccessFile();
        curFile =null;
    }

    //传输完成
    private void transOver(boolean isCloseConn) {
        clearCurFile();

        if (isCloseConn){
           curList = null;
        }

        if (loopListUp()) return;

        if (!isUsing) return;
        //设置 未使用状态, 设置最后使用时间,移除资源
        endUsingTime = System.currentTimeMillis();
        isUsing = false;
        //通知连接池管理队列
        fbcThreadBySocketList.notifyCheckSocketIdle();
    }

    public boolean validServerAddress(InetSocketAddress socketAddress) {
        try {
            return socketClient.getSocket().getRemoteAddress().equals(socketAddress);
        } catch (IOException e) {
        }
        return false;
    }

    public void setCurList(List<BackupFile> list) {
        if (list==null || list.size() == 0) return;
        if (isUsing) return;
        isUsing = true;//设置使用中
        this.curList = list;
        fileListVerify();
    }
    /**
     * 客户端 文件列表 确认
     */
    private void fileListVerify(){
        endUsingTime = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        for (BackupFile file : curList){
            sb.append(file.getRelPath() + file.getFileName() +","+file.getMd5()).append(";");
        }
        sb.deleteCharAt(sb.length() - 1);

        //1. 通知服务器, 发送文件 相对路径,文件名,MD5
        Map<String,String> map = new HashMap<>();
        map.put("charset",CHARSET);
        map.put("protocol", Protocol.C_FILE_LIST_VERIFY_QUEST);
        map.put("list", sb.toString());
        socketClient.getSession().getOperation().writeString(gson.toJson(map),CHARSET);
    }
    /**
     * 服务端响应文件列表确认
     * */
    private void fileListVerifyAck(Map<String, String> map) {
        endUsingTime = System.currentTimeMillis();
        if (!Boolean.parseBoolean(map.get("valid"))) {
            //不需要同步
            curList = null;
            transOver(false);
            return;
        }
        String[] md5Arr = map.remove("md5s").split(";"); //需要同步的文件MD5值 ;分割
        List<String> md5List = Arrays.asList(md5Arr);
        Iterator<BackupFile> it = curList.iterator();
        BackupFile f;
        while (it.hasNext()){
            f = it.next();
            if (!md5List.contains(f.getMd5())){
                it.remove();
            }
        }
        loopListUp();
    }
    //文件列表
    private boolean loopListUp() {
        if (curList!=null && curList.size() > 0){
            Iterator<BackupFile> it = curList.iterator();
            if (it.hasNext()){
                this.curFile = it.next();
                it.remove();
                uploadFile();
                return true;
            }
        }

        return false;
    }


}
