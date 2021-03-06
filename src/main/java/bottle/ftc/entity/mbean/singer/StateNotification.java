package bottle.ftc.entity.mbean.singer;

import bottle.ftc.tools.Log;
import bottle.ftc.tools.TaskUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by lzp on 2017/5/9.
 * //状态更新
 */
public class StateNotification extends Thread {
    private ReentrantLock lock = new ReentrantLock();
    private final LinkedList<bottle.ftc.entity.mbean.entity.State> list = new LinkedList<>();
    private int checkTime = 1;

    private StateNotification(){
       setDaemon(true);
       setName("FTC@StateNotification");
       start();
        //Log.i("更新状态线程", "创建完成>>[每秒检测状态信息,写入文件]");
    }
    private static class InstantHolder{
        private static StateNotification instant = new StateNotification();
    }
    public static StateNotification getInstant(){
        return InstantHolder.instant;
    }


    public void putState(bottle.ftc.entity.mbean.entity.State state){
        try{
            lock.lock();
            list.add(state);
        }finally {
            lock.unlock();
        }
    }
    public void removeState(bottle.ftc.entity.mbean.entity.State state){
        try {
            lock.lock();
            Iterator<bottle.ftc.entity.mbean.entity.State> itr = list.iterator();
            while (itr.hasNext()){
                if (itr.next().getTask().equals(state.getTask())) {
                    itr.remove();
                }
            }
            TaskUtils.sendStateToTask(state);
//            Log.e(index+ "> "+state.toString());
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
            if (list.size()>0){
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("\t#\t检测进度\t#").append("\n");
                Iterator<bottle.ftc.entity.mbean.entity.State> itr = list.iterator();
                bottle.ftc.entity.mbean.entity.State state;
                int index = 0;
                while (itr.hasNext()){
                    index++;
                    state = itr.next();
//                    if (state.getState() == 1 || state.getState()== -1){
//                        itr.remove();
//                    }
                    if (state.getState() == 0){
                        //進行中
                        if (state.isRecord()){
                            state.setPreCurSizeByTime(checkTime);
                            TaskUtils.sendStateToTask(state);
                        }
                        stringBuilder.append("Num:"+index+ ">> "+state.toString()).append("\n");
                    }
                }

                Log.e(stringBuilder.toString());
            }
        }finally {
            lock.unlock();
        }
    }
}
