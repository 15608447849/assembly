package com.winone.ftc.mcore.imps;


import com.winone.ftc.mcore.itface.Manager;
import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mentity.mbean.*;
import com.winone.ftc.mtools.*;
import m.tcps.c.FtcSocketClient;
import m.tcps.s.FtcSocketServer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lzp on 2017/5/8.
 * 实现 下载管理器
 */
public class ManagerImp implements Manager {
    private static final String TAG = "下载管理器";
    private static final String HOST = "www.baidu.com";
    private ManagerImp(){

    }

    public DownloadTaskQueue getCurrentTaskQueue() {
        return runQueue;
    }


    private static class InstantHolder{
        private static ManagerImp instant  = new ManagerImp();
    }
    public static ManagerImp get(){
        return InstantHolder.instant;
    }

    //线程执行
    private final MThreadManage threadManage = new MThreadManage();
    //运行中的任务
    private DownloadTaskQueue runQueue = new DownloadTaskQueue(threadManage.getCount());
    // 存储当前运行中的任务 用于查询状态
    private StateList stateList = new StateList(this);
    //等待执行的任务
    private DownloadWaitTaskQueue waitQueue = new DownloadWaitTaskQueue(this,threadManage.getCount());
    // http loader 实现
    private ManagerControl http;
    //ftp 实现
    private ManagerControl ftp;
    private boolean isCheckNetWork = false;

    //tcp socket server 实现
    private FtcSocketServer socketServer;
    //tcp client
    private FtcSocketClient socketClient;


    //初始化参数
    public void initial(ManagerParams params) {
        if (params == null) return;
        stateList.setRecode(params.isRecode());
        Log.setPrint(params.isPrintf());

        int count = threadManage.setCount(params.getRuntimeThreadMax());
        runQueue.setLimit(count);
        waitQueue.setLimit(count);
        isCheckNetWork = params.isCheckNetwork();
        if (params.getLogPath()!=null) Log.LOG_FILE_PATH = params.getLogPath();
    }
    private void createFTP(){
        if (ftp==null){
            ftp = new ManagerControl(this);
            ftp.setLoader("com.winone.ftc.mftp.imps.FtpLoadImp");
            ftp.setUpdateer("com.winone.ftc.mftp.imps.FtpUpdateImp");
        }
    }
    private void createHTTP(){
        if (http==null){
            http = new ManagerControl(this);
            http.setLoader("com.winone.ftc.mhttp.imps.HttpLoadImp");
            http.setUpdateer("com.winone.ftc.mhttp.imps.HttpUpdateImp");
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
//            Log.i("准备添加: "+ task);
            if (runQueue.addTaskRunning(task,runnable)){
                stateList.addSateFile(task);
            }else{
//                Log.i("添加失败,进入等待 > : "+ task);
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
        return runQueue.size() + waitQueue.size();
    }
    @Override
    public Task load(Task task) {

        if (task==null) return null;
        Log.i(" 下载添加:"+ task.getTid()+" ["+task.getUri()+"]");
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
        Log.i(" 上传添加:"+ task.getTid()+" ["+task.getUri()+"]");
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


    public void execute(Task task){
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

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                        }
                        threadManage.addMRun(runnable,0);
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
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        State state = task.getExistState();
        if (state==null){
            execute(task);
            return;
        }
        if (state.getState() == 0){ //下载中
            execute(task);
            return;
        }

        StateNotifyList.getInstant().removeState(task.getExistState());
        runQueue.removeRunning(task);
        stateList.removeStateFile(task);
        Log.w("完成: "+ task.getUri());
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
                            Thread.sleep(1000);
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
