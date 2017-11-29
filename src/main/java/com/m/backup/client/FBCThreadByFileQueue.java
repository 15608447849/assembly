package com.m.backup.client;

import com.m.backup.beans.BackupFileInfo;
import com.winone.ftc.mtools.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by user on 2017/11/24.
 */
public class FBCThreadByFileQueue extends FBCThread {

    private final BlockingQueue<BackupFileInfo> queue ;
    public FBCThreadByFileQueue(FtcBackupClient ftcBackupClient,int listSize) {
        super(ftcBackupClient);
        queue = new ArrayBlockingQueue(listSize);
    }


    @Override
    public void run() {
        while (isRunning){
            //在队列中查询,如果存在需要同步的文件 - 打开socket连接 -> socket连接在30秒内未使用,需要自动关闭

            try {
                if (queue!=null){
                    BackupFileInfo fileInfo = queue.take();
//                    Log.println("fileInfo,"+fileInfo);
                    FileUpClientSocket fusc = ftcBackupClient.getSocketClient();

//                    Log.println("fileInfo,"+fileInfo+"\nFileUpClientSocket,"+fusc);
                    //设置任务
                    fusc.setCur_up_file(fileInfo);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean putFileInfo(BackupFileInfo fileInfo) {
        try {
            queue.put(fileInfo);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}