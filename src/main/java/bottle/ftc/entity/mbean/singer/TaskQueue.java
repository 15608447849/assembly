package bottle.ftc.entity.mbean.singer;

import bottle.ftc.entity.itface.MRun;
import bottle.ftc.entity.mbean.entity.Task;
import bottle.ftc.tools.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lzp on 2017/5/9.
 * 当前下载中
 * 任务 - 线程
 */
public class TaskQueue {

    private TaskQueue() {
    }

    private static class Holder{
        private static TaskQueue instance = new TaskQueue();
    }
    public static TaskQueue get(){
        return Holder.instance;
    }

    private final LinkedHashMap<Task, MRun> maps = new LinkedHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private int limit = 1 ;
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public int getSize() {
        return maps.size();
    }
    /**
     * 检测是否超过限制
     * @return
     */
    public int checkLimit(){
        if (limit<=0) return limit;
        return  limit - maps.size();
    }
    //移除
    public MRun removeRunning(Task task){
        try{
            lock.lock();
            if (task==null || task.getState()!=Task.State.FINISH) return null;
            Iterator<Map.Entry<Task,MRun>> iterator = maps.entrySet().iterator();
            MRun run = null;
            Map.Entry<Task,MRun> entry;
            Task cTask;
            while (iterator.hasNext()){
                entry = iterator.next();
                cTask = entry.getKey();
                if (cTask.equals(task)){
                    iterator.remove();
                    run = entry.getValue();
                    break;
                }
            }
            return run;
        }finally {
            lock.unlock();
        }
    }
    //在 任务管理器imp中被调用
    public MRun getRunning(Task task){
        try{
            lock.lock();
            if (task==null) return null;
            Iterator<Map.Entry<Task,MRun>> iterator = maps.entrySet().iterator();
            Map.Entry<Task,MRun> entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                if (entry.getKey().equals(task)){
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
            if (task==null || runnable == null || task.getState()!= Task.State.NEW) return false;
            if (maps.size()==limit){
                return false;
            }
            Iterator<Map.Entry<Task,MRun>> iterator = maps.entrySet().iterator();
            Map.Entry<Task,MRun> entry;
            Task cTask;
            while (iterator.hasNext()){
                entry = iterator.next();
                cTask = entry.getKey();
                if (task.equals(cTask)){
                    if (cTask.getState() == Task.State.RUNNING){
                        //转移回调接口
                        cTask.setOtherTaskOnResult(task);
                        task.setState(Task.State.RUNNING);
//                        Log.w("在进行中列表中发现重复任务: s:"+ task+"\nd:"+ cTask);
                        return true;
                    }
                    return false;
                }
            }
            maps.put(task,runnable);
            return true;
        }finally {
            lock.unlock();
        }
    }


}
