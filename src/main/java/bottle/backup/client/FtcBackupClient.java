package bottle.backup.client;

import bottle.backup.beans.BackupTask;
import com.google.gson.Gson;
import bottle.ftc.tools.FileUtil;
import bottle.backup.imps.FtcBackAbs;
import bottle.backup.beans.BackupFile;
import bottle.ftc.tools.IOThreadPool;
import bottle.ftc.tools.Log;

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
    private final List<InetSocketAddress> serverAddressList;

    //线程池
    private final IOThreadPool pool = new IOThreadPool();

    public FtcBackupClient(String directory,int socketMax,int fileUpListLimit) {
        //同时传输最大数量
        super(directory);
        if (socketMax == 0) socketMax = 32;
        if (fileUpListLimit == 0) fileUpListLimit = 1000;
        this.socketList = new FBCThreadBySocketList(this,socketMax);
        this.fileQueue = new FBCThreadByFileQueue(this,fileUpListLimit);
        this.fileVisitor = new FileVisitor(this);
        this.timeMaps = new FBCTimeTaskOp(this);
        this.watchServer = new FBCWatchServer(this);
        this.serverAddressList = new ArrayList<>();
    }

    /**
     * 绑定socket客户端
     */
    protected void bindSocketSyncUpload(BackupTask task) throws InterruptedException{
        FileUpClientSocket sock;

            try {
                sock = socketList.getSocket(task.getServerAddress());

                if (sock !=null) {
//                    Log.i(Thread.currentThread() + " 执行上传 "+backupFileInfo);
                    if (task.getType() == 1){
                        sock.setCurFile(task.getBackupFile());
                    }else if (task.getType() == 2){
                        sock.setCurList(task.getBackupFileList());
                    }

                }
            } catch (Exception e) {

               if (e instanceof IOException){

                   if (task.getLoopCount() > 3) {
                       Log.i("无法同步任务到目标服务器("+task.getServerAddress()+")");
                       return;
                   }

                   pool.post(()->{
                       //连接不上服务器 ,放入队列 (最多尝试三次)
                       task.incLoopCount();
                       try { Thread.sleep(1000 * task.getLoopCount()); } catch (InterruptedException ignored) { }
                       fileQueue.putTask(task);
                   });


               }else if (e instanceof IllegalStateException){
                   //当前已到达最大连接数,无法获取连接
                   Log.e(Thread.currentThread() +" socket队列达到最大连接数: "+ socketList.getCurrentSize() +" 进入连接等待");
                   lockBindSocket();
                   bindSocketSyncUpload(task);
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



    public BackupFile genBackupFile(File file) {
        try {
            String path  = FileUtil.replaceFileSeparatorAndCheck(file.getCanonicalPath(),null,null);
            if (path.contains(directory)){
                path = path.substring(directory.length());
                return new BackupFile(directory,path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加一个文件到任务列表 - 指定服务器地址
     */
    public void addBackupFile(File file,InetSocketAddress serverAddress) {

        if (isFilterSuffixFile(file)) return;

        BackupFile backupFile = genBackupFile(file);
        if (backupFile!=null){
            fileQueue.putTask(new BackupTask(serverAddress,backupFile));
        }
    }

    /**
     * 从服务器列表获取
     */
    public void addBackupFile(File file) {
        if (isFilterSuffixFile(file)) return;
        BackupFile backupFile = genBackupFile(file);
        if (backupFile!=null){
            for (InetSocketAddress socket : serverAddressList){
                fileQueue.putTask(new BackupTask(socket,backupFile));
            }
        }
    }

    private final List<String> filterSuffixList = new ArrayList<>();

    /**
     * 添加同步文件后缀过滤
     */
    public void addFilterSuffix(String... filterSuffix){
        if (filterSuffix!=null){
            for (String suffix : filterSuffix){
                filterSuffixList.add(suffix);
            }
        }
    }

    /**
     * 是否是过滤文件
     */
    protected boolean isFilterSuffixFile(File file){
        String fileName = file.getName();
        if (fileName.contains(".")){
            String suffix = file.getName().substring(file.getName().lastIndexOf("."));
            if (filterSuffixList.contains(suffix)){
               return true;
            }
        }
        return false;
    }

    /**
     * 遍历目录
     * 1. 获取目录下 过滤掉指定后缀 的 所有文件列表
     * 2. 通知服务器 文件列表, 移除相同文件
     * 3.
     */
    public void ergodicDirectory(){
       pool.post(() -> {
           try {
                   List<BackupFile> list = this.fileVisitor.startVisitor();
                   for (InetSocketAddress socket : serverAddressList){
                       fileQueue.putTask(new BackupTask(socket,list));
                   }
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
     * 监听文件变化 : 设置发现文件后同步到目的地
     */
    public void addServerAddress(InetSocketAddress... serverAddressArray){
        for (InetSocketAddress it : serverAddressArray){
            addServerAddress(it);
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

    private synchronized void  addServerAddress(InetSocketAddress serverAddress) {

        if (!serverAddressList.contains(serverAddress)){
            Log.i("添加远程同步服务器地址 >> ",serverAddress);
            serverAddressList.add(serverAddress);
        }
    }

    public synchronized List<InetSocketAddress> getServerAddressList() {
        return serverAddressList;
    }


}
