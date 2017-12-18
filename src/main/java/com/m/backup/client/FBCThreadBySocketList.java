package com.m.backup.client;

import com.winone.ftc.mtools.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/11/24.
 */
class FBCThreadBySocketList extends FBCThread {

    private final int LOOP_TIME = 30*1000;
    private final int max;
    private final ArrayList<FileUpClientSocket> list;
    private final ReentrantLock lock;
    public FBCThreadBySocketList(FtcBackupClient ftcBackupClient,int max) {
        super(ftcBackupClient);
        this.max = max;
        lock = new ReentrantLock();
        list = new ArrayList<>(max);
    }

    @Override
    public void run() {
        //检测队列中的socket连接, 使用时间>30秒, 移除连接
        while (isRunning){
            waitComplete();
        }
    }




    //获取 最快一次的时间
    public int check() {
        int time = LOOP_TIME;
        boolean notify = false;
        try{
            lock.lock();

            if (list!=null && list.size()>0){
                FileUpClientSocket socket;
                Iterator<FileUpClientSocket> iterator = list.iterator();
                while (iterator.hasNext()){
                    socket = iterator.next();
                    //未使用并且闲置时间 > 30秒. 停止连接并移除.
                    if ( !socket.isUsing() ){
                        notify = true;
                        if (socket.isIdle(LOOP_TIME)){
                            if (socket.isConnected()) socket.close();
                            iterator.remove();
                        } else{
                            //未使用 并且还未到空闲状态
                            int remainTime = socket.getRemainTime(LOOP_TIME);
                            time = Math.min(remainTime,time);
                        }
                    }
                }
            }else{
                time = -1;
            }

            if (notify){
//                Log.println("通知 FtcBackupClient.this ");
                //存在未使用的连接池 - 通知客户端
                synchronized (ftcBackupClient){
                    ftcBackupClient.notifyAll();
                }
            }

        }finally {
            lock.unlock();
        }
        return time;
    }

    public FileUpClientSocket getSocket(InetSocketAddress serverAddress) throws Exception{
        try{
            lock.lock();
            FileUpClientSocket socket;
            if (list.size()>0){
                Iterator<FileUpClientSocket> iterator = list.iterator();
                while (iterator.hasNext()){
                    socket = iterator.next();
                    //未使用
                    if (!socket.isUsing() && socket.isConnected() && socket.validServerAddress(serverAddress)){
                        return socket;
                    }
                }
            }
            if (list.size()<max){
                    socket = new FileUpClientSocket(this,serverAddress);
                    list.add(socket);
                    return socket;
            }else{
                throw new IllegalStateException("current max connected number");
            }
        }finally {
            lock.unlock();
        }
    }

    public int getSocketLimit() {
        return max;
    }

    public Object getCurrentSize() {
        return list.size();
    }

    private synchronized void waitComplete(){

        try {
            int time = check();
//            Log.println("list 检测等待 - "+ time);
            synchronized (this){
                if (time>0){
                    this.wait(time);
                }else{
                    this.wait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected synchronized void notifyComplete(){
        try {
//            Log.println("连接完成 - 通知");
            synchronized (this){
                this.notifyAll();
            }
        } catch (Exception e) {
        }
    }
}
