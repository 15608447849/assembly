package com.m.backup.client;

import com.winone.ftc.mtools.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/11/24.
 */
class FBCThreadBySocketList extends FBCThread {

    private int cId = 0;
    private final int max;
    private final ArrayList<FileUpClientSocket> list;
    private final ReentrantLock lock = new ReentrantLock();
    public FBCThreadBySocketList(FtcBackupClient ftcBackupClient,int max) {
        super(ftcBackupClient);
        this.max = max;
        list = new ArrayList<>(max);
    }

    @Override
    public void run() {
        //间隔60秒 ,检测队列中的socket连接, 使用时间>30秒, 移除连接
        while (isRunning){
            try {
                Thread.sleep(15 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            check();
        }
    }

    private void check() {
        try{
            lock.lock();
            if (list!=null && list.size()>0){
                FileUpClientSocket socket;
                Iterator<FileUpClientSocket> iterator = list.iterator();
                while (iterator.hasNext()){
                    socket = iterator.next();
                    //未使用并且闲置时间 >30秒. 停止连接并移除.
                    if ( !socket.isUsing() && socket.isIdle(30*1000)){
                        socket.close();
                        iterator.remove();
                        cId--;
                    }
                }
            }

        }finally {
            lock.unlock();
        }
    }

    public FileUpClientSocket get() {
        try{
            lock.lock();
            FileUpClientSocket socket;
            if (list.size()>0){
                Iterator<FileUpClientSocket> iterator = list.iterator();
                while (iterator.hasNext()){
                    socket = iterator.next();
                    //未使用
                    if ( !socket.isUsing() && socket.isConnected()){
                        return socket;
                    }
                }
            }

            if (list.size()<max){
                try {
                    socket = new FileUpClientSocket(cId,ftcBackupClient.getSocketServerAddress());
                    list.add(socket);
                    cId++;
                    return socket;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }finally {
            lock.unlock();
        }
    }
}
