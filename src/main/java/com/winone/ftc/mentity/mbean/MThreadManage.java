package com.winone.ftc.mentity.mbean;

import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mentity.itface.MThread;
import com.winone.ftc.mtools.Log;

import java.util.ArrayList;

/**
 * Created by user on 2017/7/11.
 */
public class MThreadManage extends MThread{

    private final ArrayList<MThread> mThreadArrayList = new ArrayList<>();

    private final WaitMRunList waitMRunList = new WaitMRunList(this);
    private int count = 1; //每个线程任务存储上限
    private int simultaneously = Runtime.getRuntime().availableProcessors() * 2;//cup支持的最大同时并发数

    public MThreadManage() {
        super("MT-Manage",1000);
        play();
    }

    //设置每个线程存储的数量上限
    public int setCount(int count) {
        this.count = count;
        if (this.count >= Runtime.getRuntime().availableProcessors() * 2 ) this.count = Runtime.getRuntime().availableProcessors()*2-1;
        return getCount();
    }
    //设置下载队列总数
    public int getCount(){
        if (count <= 1) return 1;
        return count;
    }

    public void addMRun(MRun mRun, int index){

        if (this.count <= 1 || index>=simultaneously) {
            addMRunToNotLimitThread(mRun);
        }else{
            if (mThreadArrayList.size()==index) {
                mThreadArrayList.add(createThread());
            }
            MThread thread = mThreadArrayList.get(index);
            if (thread.addRunning(mRun)!=null){
                index++;
                addMRun(mRun,index);
            }
        }
    }

    private void addMRunToNotLimitThread(MRun mRun) {
        MRun runnable = addRunning(mRun);
        if (runnable!=null){
            Log.i("一个任务添加失败,等待重新添加...");
            waitMRunList.addMRun(runnable);
        }
    }

    private MThread createThread() {
        MThread thread = new MThread("MT-"+mThreadArrayList.size(),getCount());
        thread.play();
//        Log.i("创建: "+ thread);
        return thread;
    }
}
