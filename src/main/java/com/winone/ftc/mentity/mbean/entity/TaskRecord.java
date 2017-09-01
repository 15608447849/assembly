package com.winone.ftc.mentity.mbean.entity;

import com.winone.ftc.mcore.imps.ManagerImp;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.TaskUtils;

import java.util.*;

/**
 * Created by lzp on 2017/5/9.
 *  //记录状态到文件
 */
public class TaskRecord implements Task.onResult {

    @Override
    public void onSuccess(State state) {

            Task task = state.getTask();

            if (task.getType() == Task.Type.FTP_DOWN || task.getType() == Task.Type.HTTP_DOWN){
                state.setForTime(System.currentTimeMillis());
                //改变临时文件->正式文件
                if (task.isText()){
                    TaskUtils.translateText(task);
                }else{
                    TaskUtils.renameTo(task);//改变文件
                }
            }
             //删除记录config文件
             FileUtil.deleteFile(TaskUtils.getConfigFile(task));
            //删除临时文件
             FileUtil.deleteFile(TaskUtils.getTmpFile(task));
            //记录 - 1.文件MD5 2文件所在路径 3.文件来源url
            TaskUtils.writeSql(task);
            //通知绑定的回调
            callBy(task,state);
    }

    @Override
    public void onFail(State state) {
            Task task = state.getTask();
            state.setForTime(System.currentTimeMillis());
            writeStateToFile(state,TaskUtils.getConfigFile(task));//序列化状态
            //通知绑定的回调
            callBy(task,state);
            reLoad(task);
    }


    @Override
    public void onLoading(State state) {
            Task task = state.getTask();
            if (state.getForTime()<0){
                state.setForTime(System.currentTimeMillis());
            }
            writeStateToFile(state,TaskUtils.getConfigFile(task));//序列化状态
            //通知绑定的回调
            callBy(task,state);
    }


    //写入状态到文件
    public synchronized void writeStateToFile(State state, String path){
        if (!FileUtil.checkFile(path)) return;
        if (!state.isRecord()) return;
            if (!state.isInitWrite()){
                //写入基本数据
                writeString(path, String.valueOf(state.getState()), State.STATE_POINT,4); //状态
                writeString(path, String.valueOf(state.getThreadNumber()), State.THREAD_NUMBER_POINT,4);//线程数
                writeString(path, String.valueOf(state.getTotalSize()), State.TOTAL_SIZE_POINT,32);//总大小
                state.setInitWrite(true);
            }
            //写当前进度
            writeString(path, String.valueOf(state.getCurrentSize()), State.CURRENT_SIZE_POINT,32);
            if (state.getError()!=null){
                writeString(path, String.valueOf(state.getError()), State.ERROR_POINT,1024);//错误信息
            }
            //根据线线程数 写 每个线程的进度
        //判断单线程还是多线程下载 > 线程数0 单线程下载 , 线程数 1 多线程下载
        if (state.getThreadNumber()>0) {
            List<Long> list = state.getThreadMapValueList();
            if (list==null) return;
            //记录 每段线程的进度
            for (int i = 0; i < list.size(); i++) {
                int pi = State.SUB_THREAD_SOURCE_POINT  + 32 * i + i;
                writeString(path, String.valueOf(list.get(i)), pi,32);
            }
        }
    }

    private void writeString(String path,String content,int point,int length){
        FileUtil.writeByteToFilePoint(path,content.getBytes(),length,point);
    }


    private void reLoad(Task task) {
        if (task.isRestart()){
            Log.i("重新下载: "+ task);
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
            }
            task.setState(Task.State.NEW);
            ManagerImp.get().execute(task);
        }

    }

    private void callBy(Task task,State state){
        if (task.getOnResultList().size()>0){
            new Thread(() -> listByCall(task.getOnResultList(),state)).start();
        }
    }
    private void listByCall(List<Task.onResult> onResultList,State state){
        Iterator<Task.onResult> iterator = onResultList.iterator();
        Task.onResult onResult ;
        while (iterator.hasNext()){
            onResult = iterator.next();
            if (state.getState() == 0){
                onResult.onLoading(state);
            }else{
                iterator.remove();
                if (state.getState() == 1){
                    onResult.onSuccess(state);
                }else {
                    onResult.onFail(state);
                }
            }
        }
    }

}
