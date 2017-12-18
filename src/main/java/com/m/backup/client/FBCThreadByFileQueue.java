package com.m.backup.client;

import com.m.backup.beans.BackupFileInfo;
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
                    ftcBackupClient.bindSocketClient(fileInfo);
                    if (fileInfo.getLoopCount()>0){
                        if (fileInfo.getLoopCount()>=3){
                            //丢弃任务
                            throw new IllegalArgumentException("file '"+fileInfo.getFullPath()+"' backup to '"+ fileInfo.getServerAddress()+"' fail.");
                        }else{
                            putFileInfo(fileInfo);
                        }
                    }

                }

            }catch (Exception e){
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
