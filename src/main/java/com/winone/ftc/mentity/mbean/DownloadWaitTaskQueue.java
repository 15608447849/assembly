package com.winone.ftc.mentity.mbean;

import com.winone.ftc.mcore.imps.ManagerImp;
import com.winone.ftc.mcore.itface.Manager;
import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mtools.Log;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/24.
 */
public class DownloadWaitTaskQueue extends LinkedHashMap<Task,MRun> implements Runnable{
    private final ReentrantLock lock = new ReentrantLock();
    private final ManagerImp manager;
    private int limit;
    public DownloadWaitTaskQueue(ManagerImp manager,int limit) {
        this.manager = manager;
       Thread thread  = new Thread(this);
       thread.setDaemon(true);
       thread.setName("ftc_wait_queue");
       this.limit = limit;
       thread.start();
    }

    /**
     * 添加任务
     */
    public void addTask(Task task,MRun runnable){
        try {
            lock.lock();
            put(task,runnable);
//            Log.i("等待任务 " + task.getUri() + ", 当前等待队列数量 : "+ size());
        }finally {
            lock.unlock();
        }
    }

    /**
     * 检测任务
     */
    private void check(){
        if (manager.getCurrentTaskQueue().size() == limit) {
//            Log.w("不可放入任务,当前等待队列数量:"+size()+" ,当前进行中任务数:"+manager.getCurrentTaskQueue().size()+" 限制数:"+ limit);
           return;
        }

        Task task = null;
        MRun runnable = null;
        try {
            lock.lock();
            if (size() >0 ){
                Iterator<Map.Entry<Task,MRun>> iterator = entrySet().iterator();
                Map.Entry<Task,MRun> entry ;
                if (iterator.hasNext()){
                    entry = iterator.next();
                    task = entry.getKey();
                    runnable = entry.getValue();
                    iterator.remove();
                }
            }

        }finally {
            lock.unlock();
        }
        manager.executeTask(task,runnable);

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
                    Thread.sleep(25);
            } catch (InterruptedException e) {
            }
    }

    public void setLimit(int limit) {
        if (limit<1) return;
        this.limit = limit;
    }
}
