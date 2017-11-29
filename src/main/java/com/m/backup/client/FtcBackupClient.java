package com.m.backup.client;

import com.google.gson.Gson;
import com.winone.ftc.mtools.FileUtil;
import com.m.backup.imps.FtcBackAbs;
import com.m.backup.beans.BackupFileInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by user on 2017/11/23.
 * 文件同步客户端
 */
public class FtcBackupClient extends FtcBackAbs {

    private final FBCTimeTaskOp timeMaps;
    private final InetSocketAddress socketServerAddress;
    private final FBCThreadBySocketList socketList;
    private final FBCThreadByFileQueue fileQueue;
    private final FileVisitor fileVisitor;
    private final FBCWatchServer watchServer;


    public FtcBackupClient(String directory, InetSocketAddress socketServerAddress,int socketMax,int fileUpListLimit) {
        //服务器地址, 同时传输最大数量
        super(directory);
        this.socketServerAddress = socketServerAddress;
        this.socketList = new FBCThreadBySocketList(this,socketMax);
        this.fileQueue = new FBCThreadByFileQueue(this,fileUpListLimit);
        this.fileVisitor = new FileVisitor(this);
        this.timeMaps = new FBCTimeTaskOp(this);
        this.watchServer = new FBCWatchServer(this);
    }

    protected FileUpClientSocket getSocketClient() throws InterruptedException{
        FileUpClientSocket socketClient;
        while (true){
//            Thread.sleep(1000); //休眠1秒 ,直到拿到为止
            socketClient = socketList.get();
            if (socketClient!=null) break;
        }
        return socketClient;
    }


    public InetSocketAddress getSocketServerAddress() {
        return socketServerAddress;
    }

    public boolean addBackupFile(File file) throws IOException {
//        Log.println(file);
        String path  = FileUtil.replaceFileSeparatorAndCheck(file.getCanonicalPath(),null,null);
        if (path.contains(directory)){
           path = path.substring(directory.length());
           return fileQueue.putFileInfo(new BackupFileInfo(directory,path));
        }
        return false;
    }


    public void ergodicDirectory(){
        this.fileVisitor.startVisitor();
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

}
