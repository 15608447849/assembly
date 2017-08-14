package com.winone.ftc.mentity.itface;

import com.winone.ftc.mtools.Log;
import m.bytebuffs.MyBuffer;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/7/11.
 * 任务线程运行
 */
public class MThread extends Thread{

    private final LinkedBlockingQueue<MRun> runQueue ;

    public MThread(String name,int storeLimit) {
        super(name);
        this.setDaemon(true);
        this.setPriority(10);
        runQueue = new LinkedBlockingQueue(storeLimit);
    }

    //是否运行
    private volatile boolean isRunning = false;
    /**
    * 运行
    * */
    public void  play(){
        if (isRunning) return;
        isRunning = true;
        start();
    }
    /**
     * 结束
     */
    public void over(){
        if (isRunning){
            while (runQueue.size()!=0);
            isRunning = false;
        }
    }
    /**
     * 如果队列存在元素,取出元素执行
     */
    @Override
    public void run() {
        MRun running;
        while (isRunning){
            try {
                running = getExecuteRunning();
                if (running == null) continue;
                running.onRunning();
            } catch (Exception e) {
                Log.e("下载线程错误: "+ e);
            }
        }
    }

    /**
     * 添加元素到队列中
     * 失败 返回这个runnning
     */
    public MRun addRunning(MRun running){
            if (!isRunning) return running;
            if (runQueue.offer (running)){
//                Log.i(this+ " 添加成功:"+ running);
                return null;
            }
            return running;

    }
    /**
     * 获取元素的当前大小
     */
    public int getQueueSize(){
        return runQueue.size();
    }
    /**
     * 移除并返问队列头部的元素
     * 如果队列为空，则阻塞
     */
    public MRun getExecuteRunning(){
        try {
            return runQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


}
