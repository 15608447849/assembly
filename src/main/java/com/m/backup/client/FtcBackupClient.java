package com.m.backup.client;

import com.google.gson.Gson;
import com.winone.ftc.mtools.FileUtil;
import com.m.backup.imps.FtcBackAbs;
import com.m.backup.beans.BackupFileInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/11/23.
 * 文件同步客户端
 */
public class FtcBackupClient extends FtcBackAbs {

    private final FBCTimeTaskOp timeMaps;

    private final FBCThreadBySocketList socketList;
    private final FBCThreadByFileQueue fileQueue;
    private final FileVisitor fileVisitor;
    private final FBCWatchServer watchServer;

    //存储相关联的同步服务器地址
    private List<InetSocketAddress> serverAddressList;

    public FtcBackupClient(String directory,int socketMax,int fileUpListLimit) {
        //同时传输最大数量
        super(directory);
        this.socketList = new FBCThreadBySocketList(this,socketMax);
        this.fileQueue = new FBCThreadByFileQueue(this,fileUpListLimit);
        this.fileVisitor = new FileVisitor(this);
        this.timeMaps = new FBCTimeTaskOp(this);
        this.watchServer = new FBCWatchServer(this);
    }

    protected FileUpClientSocket getSocketClient(InetSocketAddress InetSocketAddress) throws InterruptedException{
        FileUpClientSocket socketClient;
        int loopIndex=0;
        while (true){
//            Thread.sleep(1000); //休眠1秒 ,直到拿到为止
            socketClient = socketList.getSocket(InetSocketAddress);
            if (socketClient!=null || loopIndex>socketList.getSocketLimit()) break;
            loopIndex++;
        }
        return socketClient;
    }




    public boolean addBackupFile(File file,InetSocketAddress serverAddress) throws IOException {
//        Log.println(file);
        String path  = FileUtil.replaceFileSeparatorAndCheck(file.getCanonicalPath(),null,null);
        if (path.contains(directory)){
           path = path.substring(directory.length());
           return fileQueue.putFileInfo(new BackupFileInfo(directory,path,serverAddress));
        }
        return false;
    }


    public void ergodicDirectory(InetSocketAddress serverAddress){
        this.fileVisitor.startVisitor(serverAddress);
    }

    /**
     * list - {"11:00:00","2017-11-27 14:00:00"}
     * @param json
     */
    public void setTime(String json){
        try {
            List<String> list = new Gson().fromJson(json,List.class);
            for (String timeStr:list){
                timeMaps.setTime(timeStr);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 监听文件变化 :设置发现文件后同步到目的地
     */
    public void setServerAddress(InetSocketAddress serverAddress,List<InetSocketAddress> listAddress){
        if (serverAddress!=null){
            setServerAddress(serverAddress);
        }
        if (listAddress!=null && listAddress.size()>0){
            for (InetSocketAddress it : listAddress){
                setServerAddress(it);
            }
        }
    }
    /**
     * 是否监听文件变化
     */
    public void watchDirectory(boolean isWatch){
        try {
            if (isWatch){
                    watchServer.start();
            }else{
                    watchServer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void setServerAddress(InetSocketAddress serverAddress) {
        if (serverAddressList==null) serverAddressList = new ArrayList<>();
        if (!serverAddressList.contains(serverAddress)){
            serverAddressList.add(serverAddress);
        }
    }

    public synchronized List<InetSocketAddress> getServerAddressList() {
        return serverAddressList;
    }

}
