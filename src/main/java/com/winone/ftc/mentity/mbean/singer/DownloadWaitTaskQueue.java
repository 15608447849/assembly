package com.winone.ftc.mentity.mbean.singer;

import com.winone.ftc.mcore.imps.ManagerImp;
import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.Log;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/24.
 */
public class DownloadWaitTaskQueue extends Thread{
    private final ReentrantLock lock = new ReentrantLock();
    private final LinkedHashMap<Task,MRun> maps = new LinkedHashMap();
    private final Random random = new Random();

    private DownloadWaitTaskQueue() {
      setDaemon(true);
      setName("FTC@TaskWaitQueue");
      start();
    }
    public int getSize() {
        return maps.size();
    }
    private static class Holder{
        private static final DownloadWaitTaskQueue instance = new DownloadWaitTaskQueue();
    }
    public static DownloadWaitTaskQueue get(){
        return Holder.instance ;
    }
    /**
     * 添加任务
     */
    public void addTask(Task task,MRun runnable){
        try {
            lock.lock();
            if (Task.State.NEW == task.getState()){
                //查看是否存在相同任务.相同任务 - 转换回调接口
                Iterator<Map.Entry<Task,MRun>> iterator = maps.entrySet().iterator();
                Map.Entry<Task,MRun> entry;
                Task cTask;
                while (iterator.hasNext()){
                    entry = iterator.next();
                    cTask = entry.getKey();
                    if (task.equals(cTask)){
                        //转移回调接口
                        cTask.setOtherTaskOnResult(task);
                        Log.w("在等待列表中发现重复任务: s:"+ task+"\nd:"+ cTask);
                        return;
                    }
                }
                    maps.put(task,runnable);
            }
        }finally {
            lock.unlock();
        }
    }

    private int getRandomNumber(){
        return (random.nextInt(20)+1) * 10 ;
    }

    /**
     * 检测任务
     */
    private void check(){
        Task task = null;
        MRun runnable = null;
        int tag = 0 ;
        try {
            lock.lock();
//            Log.w(getName()+ "等待检测.当前数量: "+ maps.size());
            if (maps.size() >0 ){
                tag = DownloadTaskQueue.get().checkLimit();
                if (tag>0){
//                    Log.w(getName()+ " next.");
                    Iterator<Map.Entry<Task,MRun>> iterator = maps.entrySet().iterator();
                    Map.Entry<Task,MRun> entry ;
                    if (iterator.hasNext()){
                        entry = iterator.next();
                        iterator.remove();

                        task = entry.getKey();
                        runnable = entry.getValue();
                    }
                }

            }
        }finally {
            lock.unlock();
        }
        ManagerImp.get().executeTask(task,runnable);
        if (tag>2){
            check();
        }
    }
    @Override
    public void run() {
        while (true){
            waitTime();
            check();
        }
    }

    private void waitTime() {
            try {
              Thread.sleep(getRandomNumber());
            } catch (InterruptedException e) {
            }
    }
}
