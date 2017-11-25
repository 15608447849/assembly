package com.winone.ftc.mentity.itface;

import com.winone.ftc.mtools.Log;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by user on 2017/7/11.
 * 任务线程运行
 */
public class MThread extends Thread{
    private final LinkedBlockingQueue<MRun> runQueue;
    private long dleTime = System.currentTimeMillis();
    private final int storeLimit ;
    public MThread(String name,int storeLimit) {
        setName(name);
        this.storeLimit = storeLimit;
        runQueue = new LinkedBlockingQueue(storeLimit);
        setPriority(10);
    }
    //是否运行
    private volatile boolean isRunning = false;
    //是个工作
    private boolean isWork = false;
    /**
    * 运行
    * */
    public MThread  play(){
        if (!isRunning){
            isRunning = true;
            start();
        }
      return this;
    }
    public boolean isAction(){
        return isRunning;
    }
    /**
     * 是否可结束
     */
    public boolean over(){
        if (isRunning){
            if  (!runQueue.isEmpty()){
                return false;
            }
            isRunning = false;
            interrupt();//强制中断堵塞
        }
        return true;
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
                isWork = true;
                try {
                    running.onRunning();
                } catch (Exception e) {
                    Log.w(getName()+" >> "+ e);
                }
                isWork = false;
                dleTime = System.currentTimeMillis();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加元素到队列中
     * 失败 返回这个runnning
     */
    public MRun addRunning(MRun running){
            if (!isRunning) return running;
            if (storeLimit == 1 && isWork) return running;
            if (runQueue.offer (running)){
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
        }
        return null;
    }

    //返回线程空闲时间
    public long getDleTime(){
        return isWork?0L:(System.currentTimeMillis() - dleTime);
    }
}
