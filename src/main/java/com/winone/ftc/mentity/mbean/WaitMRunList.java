package com.winone.ftc.mentity.mbean;

import com.winone.ftc.mentity.itface.MRun;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/7/23.
 */
class WaitMRunList extends Thread {
    private MThreadManage mThreadManage;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private List<MRun> mRuns = new ArrayList<>();
    public WaitMRunList(MThreadManage mThreadManage) {
        this.setName("WaitMRunList");
        this.setDaemon(true);
        this.setPriority(6);
        this.mThreadManage = mThreadManage;
        this.start();
    }

    protected  void addMRun(MRun run){
        try{
            reentrantLock.lock();
            mRuns.add(run);
        }finally {
            reentrantLock.unlock();
        }

    }

    @Override
    public void run() {
        while (true){
            if (mRuns.size()==0){
                try {
                    sleep(30 * 1000);
                } catch (InterruptedException e) {
                }
            }else{
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            work();
        }
    }

    private void work() {
        try{
            reentrantLock.lock();
                Iterator<MRun> iterator = mRuns.iterator();
                if (iterator.hasNext()){
                    mThreadManage.addRunning( iterator.next());
                    iterator.remove();
                }
        }
        finally {
            reentrantLock.unlock();
        }
    }
}
