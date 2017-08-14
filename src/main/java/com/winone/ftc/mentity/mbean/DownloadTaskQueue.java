package com.winone.ftc.mentity.mbean;

import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mtools.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lzp on 2017/5/9.
 * 任务 - 线程
 */
public class DownloadTaskQueue extends LinkedHashMap<Task,MRun> {

    private int limit;

    public DownloadTaskQueue(int limit) {
        this.limit = limit;
    }

    private final ReentrantLock lock = new ReentrantLock();
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public MRun removeRunning(Task task){
        try{
            lock.lock();
            if (task==null) return null;
            Log.e("移除: "+ task);
            Iterator<Map.Entry<Task,MRun>> iterator = entrySet().iterator();
            MRun run = null;
            Map.Entry<Task,MRun> entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                if (task.getTid() == entry.getKey().getTid()){
                    iterator.remove();
                    entry.getKey().setState(Task.State.FINISH);
                    run = entry.getValue();
                    Log.i("下载队列 移除 任务: ["+ task.getTid()+" # "+ task.getUri() +"] ,当前下载中 任务数: "+ size());
                    break;
                }
            }
            return run;
        }finally {
            lock.unlock();
        }
    }

    public MRun getRunning(Task task){
        try{
            lock.lock();
            if (task==null) return null;
            Iterator<Map.Entry<Task,MRun>> iterator = entrySet().iterator();
            Map.Entry<Task,MRun> entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                if (entry.getKey().getTid() == task.getTid()){
                    return entry.getValue();
                }
            }
            return null;
        }finally {
            lock.unlock();
        }

    }

    public boolean addTaskRunning(Task task,MRun runnable){
        try{
            lock.lock();
            if (task==null || runnable==null) return false;
            if (limit > 0){
//                Log.i("超过限制数:"+ limit+ "　－　"+ task);
                if (size() == limit) return false;
            }
            Iterator<Map.Entry<Task,MRun>> iterator = entrySet().iterator();
            Map.Entry<Task,MRun> entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                if (task.equals(entry.getKey())){
                    Log.w("重复: " + task.getUri()+ " - "+ entry.getKey().getUri());
                    if (entry.getKey().getState() == Task.State.RUNNING){
                        //转移回调接口
                        for (Task.onResult onResult : task.getOnResultList() ){
                            entry.getKey().setOnResult(onResult);
                        }
                        task.setState(Task.State.RUNNING);
                        return true;
                    }
                    return false;
                }
            }
            Log.w("进行中队列 添加: "+ task.getTid());
            put(task,runnable);
            return true;
        }finally {
            lock.unlock();
        }
    }

    //根据TID URL 本地完整路径,远程完整路径 -> 查询是否存在任务
    public boolean isExistTask(Task task){
        try{
            lock.lock();
            Iterator<Task> itr = keySet().iterator();
            Task t;
            while (itr.hasNext()){
                t =  itr.next();
                if (t.equals(task)){
                    return true;
                }
            }
            return false;
        }finally {
            lock.unlock();
        }
    }

}
