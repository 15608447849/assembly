package com.winone.ftc.mentity.mbean.singer;

import com.sun.org.apache.regexp.internal.RE;
import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mentity.itface.MThread;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/7/11.
 */
public class MThreadManage extends Thread{


//    private final MThreadByMRunWaitStack waitMRunList;  waitMRunList = new MThreadByMRunWaitStack(this);
    private final long TIME = 30 * 1000;
    //已经启动的 线程
    private final ArrayList<MThread> mThreadArrayList = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 每个线程 任务 存储上限(默认 1)
     */
    private int count = 1;
    /**
     * cup支持的最大同时并发数 ( 最大: 核数 * 2 ) (默认 : 1)
     */
    private int simultaneously =1;

    private MThreadManage() {
        setName("FTC@MT-Manager");
        setDaemon(true);
        start();
    }

    private static class Holder{
        private static final MThreadManage instance = new MThreadManage();
    }
    public static MThreadManage get(){
        return Holder.instance;
    }


    //设置每个线程的最大任务存储数量
    public MThreadManage setSingerThreadLimit(int count){
        if (mThreadArrayList.size()==0){ //如果mThread中的 线程对象 不为0  ,不可设置
            if (count <= 0){
                this.count = 1;
            }else
            if (this.count > Integer.MAX_VALUE){
                this.count = Integer.MAX_VALUE;
            }else{
                this.count = count;
            }
        }
        return this;
    }

    //设置最大并发数
    public MThreadManage setSimultaneously(int simultaneously) {
        if (simultaneously > Runtime.getRuntime().availableProcessors() * 2){
            this.simultaneously = Runtime.getRuntime().availableProcessors() * 2; //最大
        }else
        if (simultaneously <= 0){
            this.simultaneously = 1;
        }else{
            this.simultaneously =simultaneously;
        }
        return this;
    }

    //获取 所有下载线程 可存储任务 的 上限总数
    public int getCountMax(){
        // 单个线程上限数 * 并发线程总数
        return (count * simultaneously)+ (count==1?0:simultaneously);
    }
    //外部调用
    public synchronized void launchTask(Task task,MRun run){
//        Log.w("启动 - "+ run);
        if (mThreadArrayList.size()<simultaneously){
            launchTask(task,run,0);
        }else if (mThreadArrayList.size() == simultaneously){
            Iterator<MThread> iterator = mThreadArrayList.iterator();
            MThread thread;
            while (iterator.hasNext()){
                thread = iterator.next();
                if (thread.addRunning(run) == null){
                    return;
                }
            }
            recovery(task,run);
        }else{
            recovery(task,run);
        }
    }

    // 待启动任务到来
    private void launchTask(Task task,MRun run, int index){ // 从0层开始
        //判断下标是否当前最大并发数
        if ( index >= simultaneously){// 下标从0开始
           recovery(task,run);
        }else{
            if (index == mThreadArrayList.size()){
                //创建,添加 MThread 线程
                addThread(new MThread("FTC@MT-Sub-"+(index),count).play());
            }
            //获取 index映射的线程对象
            MThread thread = mThreadArrayList.get(index++);
            if (thread.isAction()){
                //线程活动 ,尝试添加
                if (thread.addRunning(run) != null){
                    //添加失败 , 队列满 ,向下层添加
                    launchTask(task,run,index);
                }
            }else{
                //不活动线程,向下层添加
                launchTask(task,run,index);
            }
        }
    }



    //回收
    private void recovery(Task task,MRun run) {
        //将任务丢回待下载队列 -> 1.任务状态设置为NEW ,并且从 正在下载的队列中, 移除这个run
        task.setState(Task.State.FINISH);
        MRun getRun  = DownloadTaskQueue.get().removeRunning(task);
        if (getRun !=null && getRun.equals(run)){
            //从记录文件夹中移除
            StateInfoStorage.get().removeStateFile(task);
        }
//        Log.w("回收:" + task);
        task.setState(Task.State.NEW);
        DownloadWaitTaskQueue.get().addTask(task,run);
    }


    //添加线程
    private boolean addThread(MThread thread) {
        try{
            lock.lock();
            Log.w("添加下载线程:"+ thread.getName()+" ,并发总数:"+ simultaneously);
            return mThreadArrayList.add(thread);
        }finally {
            lock.unlock();
        }
    }
    private void check(){
        try{
            lock.lock();
            Iterator<MThread> mThreadIterator  = mThreadArrayList.iterator();
            MThread thread;
            while (mThreadIterator.hasNext()){
                thread = mThreadIterator.next();
                if (thread.getDleTime() > TIME){
                    if (thread.over()){
                        mThreadIterator.remove();
                        Log.w("成功移除空闲下载线程: "+ thread.getName());
                    }
                }
            }
        }finally {
            lock.unlock();
        }
    }
    //检测 - 60秒检测一次
    @Override
    public void run() {
        while (true){
            try {
                sleep(TIME*2);
            } catch (InterruptedException e) {
            }
            check();
        }
    }
}
