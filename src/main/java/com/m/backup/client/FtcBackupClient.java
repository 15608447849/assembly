package com.m.backup.client;

import com.google.gson.Gson;
import com.winone.ftc.mtools.FileUtil;
import com.m.backup.imps.FtcBackAbs;
import com.m.backup.beans.BackupFileInfo;
import com.winone.ftc.mtools.IOThreadPool;
import com.winone.ftc.mtools.Log;

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





    /**
     * 绑定socket客户端
     */
    protected void bindSocketSyncUpload(BackupFileInfo backupFileInfo) throws InterruptedException{
        FileUpClientSocket socketClient;

            try {
                socketClient = socketList.getSocket(backupFileInfo.getServerAddress());
//                Log.i(Thread.currentThread()+" 远程同步socket管道 ,是否连接 : "+ socketClient.isConnected());
                if (socketClient!=null) {
//                    Log.i(Thread.currentThread() + " 执行上传 "+backupFileInfo);
                    socketClient.setCur_up_file(backupFileInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
               if (e instanceof IOException){

                   if (backupFileInfo.getLoopCount() >= 3) return;

                   pool.post(()->{
                       //连接不上服务器 ,放入队列 (最多尝试三次)
                       backupFileInfo.setLoopCount(backupFileInfo.getLoopCount()+1);
                       try {
                           Thread.sleep(500 * backupFileInfo.getLoopCount());
                       } catch (InterruptedException e1) {
                       }
                       fileQueue.putFileInfo(backupFileInfo);
                   });


               }else if (e instanceof IllegalStateException){
                   //当前已到达最大连接数,无法获取连接
                   Log.e(Thread.currentThread() +" socket队列达到最大连接数: "+ socketList.getCurrentSize());
                   lockBindSocket();
                   bindSocketSyncUpload(backupFileInfo);
               }

            }
    }

    private void lockBindSocket() {
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void unLockBindSocket() {
        synchronized (this){
                notify();
        }
    }

    private final IOThreadPool pool = new IOThreadPool();


    /**
     * 添加一个文件到任务列表
     */
    public boolean addBackupFile(File file,InetSocketAddress serverAddress) throws IOException {
//        Log.i("添加同步文件 >> ",file);
        String path  = FileUtil.replaceFileSeparatorAndCheck(file.getCanonicalPath(),null,null);
        if (path.contains(directory)){
           path = path.substring(directory.length());
           return fileQueue.putFileInfo(new BackupFileInfo(directory,path,serverAddress));
        }
        return false;
    }

    /**
     * 遍历目录
     */
    public void ergodicDirectory(InetSocketAddress serverAddress,String... filterSuffix){
       pool.post(() -> {
           try {
               this.fileVisitor.startVisitor(serverAddress,filterSuffix);
           } catch (Exception e) {
               e.printStackTrace();
           }
        });

    }

    /**
     * 在某个时间段进行同步
     * list - {"11:00:00","2017-11-27 14:00:00"}
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
//            Log.i("添加远程同步服务器地址 >> ",serverAddress);
            serverAddressList.add(serverAddress);
        }
    }

    public synchronized List<InetSocketAddress> getServerAddressList() {
        return serverAddressList;
    }


}
