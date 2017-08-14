package com.winone.ftc.mentity.mbean;

import com.winone.ftc.mtools.TaskUtils;
import com.winone.ftc.mtools.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by lzp on 2017/5/9.
 * //状态更新
 */
public class StateNotifyList extends LinkedList implements Runnable {
    private ReentrantLock lock = new ReentrantLock();
    private int checkTime = 1;
    private StateNotifyList(){
       Thread thread = new Thread(this);
       thread.setDaemon(true);
       thread.setName("ftc_notify_thread");
       thread.start();
        //Log.i("更新状态线程", "创建完成>>[每秒检测状态信息,写入文件]");
    }
    private static class InstantHolder{
        private static StateNotifyList instant = new StateNotifyList();
    }
    public static StateNotifyList getInstant(){
        return InstantHolder.instant;
    }


    public void putState(State state){
        try{
            lock.lock();
            add(state);
        }finally {
            lock.unlock();
        }

    }
    public void removeState(State state){
        try {
            lock.lock();
            Iterator<State> itr = this.iterator();
            while (itr.hasNext()){
                if (itr.next().getTask().equals(state.getTask())) {
                    itr.remove();
                }
            }
            TaskUtils.sendStateToTask(state);
            Log.w(Thread.currentThread().getName() + "  监听移除: "+ state);
        }finally {
            lock.unlock();
        }

    }
    

    //
    @Override
    public void run() {
        while (true){
            waitTime();
            notifyState();
        }
    }

    private void waitTime() {
        try {
        Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void notifyState() {
        try {
            lock.lock();
            if (size()>0){
                Log.e("------------------------开始检测------------------------");
                Iterator<State> itr = this.iterator();
                State state;
                while (itr.hasNext()){
                    state = itr.next();
                    if (state.getState() == 1 || state.getState()== -1){
                        itr.remove();
                    }

                    if (state.getState() == 0){
                        //進行中
                        if (state.isRecord()){
                            state.setPreCurSizeByTime(checkTime);
                            TaskUtils.sendStateToTask(state);
                        }
                        Log.e(state.toString());
                    }
                }
                Log.e("\n");
            }
        }finally {
            lock.unlock();
        }

    }
}
