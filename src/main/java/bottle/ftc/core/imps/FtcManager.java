package bottle.ftc.core.imps;


import bottle.ftc.entity.itface.MRun;
import bottle.ftc.entity.mbean.entity.ManagerParams;
import bottle.ftc.entity.mbean.entity.State;
import bottle.ftc.entity.mbean.entity.Task;
import bottle.ftc.entity.mbean.singer.*;
import bottle.ftc.tools.*;

import bottle.ftc.core.itface.Manager;


import java.util.Iterator;
import java.util.List;

/**
 * Created by lzp on 2017/5/8.
 * 实现 下载管理器
 */
public class FtcManager implements Manager {
    private static final String TAG = "FTC管理器";
    private static final String HOST = "www.baidu.com";

    // http loader 实现
    private ManagerControl http;
    //ftp 实现
    private ManagerControl ftp;
    //是否检测网络状态
    private boolean isCheckNetWork = false;

    //线程执行池
    private final MThreadManage threadManage;
    // 存储当前运行中的任务 用于查询状态
    private final StateInfoStorage stateList;
    //运行中的任务
    private final TaskQueue runQueue;
    //等待执行的任务
    private final TaskWaitQueue waitQueue;
    //下载上传状态更新
    private final StateNotification stateNotification;

    private FtcManager(){
        threadManage = MThreadManage.get();
        stateList = StateInfoStorage.get();
        runQueue = TaskQueue.get();
        waitQueue = TaskWaitQueue.get();
        stateNotification = StateNotification.getInstant();
        runQueue.setLimit(threadManage.getCountMax());
    }

    public TaskQueue getCurrentTaskQueue() {
        return runQueue;
    }

    private static class InstantHolder{
        private static FtcManager instant  = new FtcManager();
    }
    public static FtcManager get(){
        return InstantHolder.instant;
    }

    //初始化参数
    public void initial(ManagerParams params) {
        if (params != null) {
            if (!StringUtil.isEntry(params.getLogPath())) Log.LOG_FILE_PATH = params.getLogPath();
            Log.setPrint(params.isPrintf());
            stateList.setRecode(params.isRecode());
            runQueue.setLimit(threadManage.setSimultaneously(params.getRuntimeThreadMax()).getCountMax());
            isCheckNetWork = params.isCheckNetwork();
        }
    }
    private void createFTP(){
        if (ftp==null){
            ftp = new ManagerControl(this);
            ftp.setLoader("bottle.ftc.ftp.imps.FtpLoadImp");
            ftp.setUploader("bottle.ftc.ftp.imps.FtpUpdateImp");
        }
    }
    private void createHTTP(){
        if (http==null){
            http = new ManagerControl(this);
            http.setLoader("bottle.ftc.http.imps.HttpLoadImp");
            http.setUploader("bottle.ftc.http.imps.HttpUpdateImp");
        }
    }
    @Override
    public Task putTask(Task task, MRun runnable) {
        if (isCheckNetWork){
            if(!NetworkUtil.ping(TaskUtils.matchIpAddress(task))){
                return null;
            }
        }
        if (task!=null && runnable!=null){
//            Log.i("传输管理器,执行中任务 : "+ task.getTid()+" , "+ task.getUri());
            if (runQueue.addTaskRunning(task,runnable)){
                stateList.addSateFile(task);
            }else{
                waitQueue.addTask(task,runnable);
            }
            return task;
        }
        return null;
    }
    @Override
    public void released() {
            //
    }
    @Override
    public int getCurrentTaskSize() {
        return runQueue.getSize() + waitQueue.getSize();
    }
    @Override
    public Task load(Task task) {

        if (task==null) return null;
        if (!FileUtil.checkDir(task.getLocalPath())) return null;

        if (task.getType() == Task.Type.HTTP_DOWN){
            createHTTP();
            if (http!=null){
                return executeTask(http.load(task));
            }
        }
        if (task.getType() == Task.Type.FTP_DOWN){
            createFTP();
            if (ftp!=null){
                return executeTask(ftp.load(task));
            }
        }
        return null;
    }

    @Override
    public Task upload(Task task) {

        if (task==null) return null;
        if (task.getType() == Task.Type.HTTP_UP){
            createHTTP();
            if (http!=null){
                return  executeTask(http.upload(task));
            }
        }
        if (task.getType() == Task.Type.FTP_UP){
            createFTP();
            if (ftp!=null){
                return executeTask(ftp.upload(task));
            }
        }
        return null;
    }

    @Override
    public Task finish(Task task) {
        if(task!=null){
            removeTask(task);
        }
        return null;
    }


    public void execute(final Task task){

        if (task == null) return;
                if (task!=null && (task.getType() == Task.Type.HTTP_DOWN || task.getType() == Task.Type.FTP_DOWN)){
                    load(task);
                }
                if (task!=null && (task.getType() == Task.Type.HTTP_UP || task.getType()== Task.Type.FTP_UP)){
                    upload(task);
                }
    }

    @Override
    public Task executeTask(Task task){

            if (task!=null) {
                MRun runnable = runQueue.getRunning(task);
                    if (runnable != null && task.getState() == Task.State.NEW) {
                        task.setState(Task.State.RUNNING);
                        threadManage.launchTask(task,runnable);
                      }
                return task;
            }
            return null;
    }

    public void executeTask(Task task,MRun runnable){
        if(task!=null && runnable!=null){
            executeTask(putTask(task,runnable));
        }
    }

    @Override
    public void removeTask(Task task) {
        if (task==null) return;
        task.setState(Task.State.FINISH);//设置任务状态 -> 完成!
        State state = task.getExistState();
        if (state==null){
            execute(task);
            return;
        }
        if (state.getState() == 0){ //下载中
            execute(task);
            return;
        }
        //下载队列中移除
        if (runQueue.removeRunning(task)!=null){
            //从记录文件夹中移除
            stateList.removeStateFile(task);
        }
        //从通知更新中移除
        stateNotification.removeState(task.getExistState());

        Log.i("传输管理器@结束任务: "+task.getTid()+" "+ task.getExistState().getStateString());
    }
    /**
     * 队列任务
     * 堵塞
     */
    public boolean listExecuteBlock(List<Task> list){
        if (list==null && list.isEmpty()) return false;

        try {
            for (Task task : list){
                execute(task);
            }

            while (list.size()>0){
                Iterator<Task> iterator = list.iterator();
                while (iterator.hasNext()){
                    if (iterator.next().getState() == Task.State.FINISH){
                        iterator.remove();
                    }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                        }
                }
            }
            return true;
        } catch (Exception e) {
        }
        return true;
    }


}
