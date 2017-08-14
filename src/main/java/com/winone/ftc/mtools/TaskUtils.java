package com.winone.ftc.mtools;

import com.winone.ftc.mentity.mbean.DownloadFileRecordSql;
import com.winone.ftc.mentity.mbean.State;
import com.winone.ftc.mentity.mbean.Task;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by lzp on 2017/5/9.
 */
public class TaskUtils {
    //判断类型是否是执行类型
    public static boolean checkTypeOnTarget(Task task, Task.Type type){
        return task.getType().equals(type);
    }
    //发送状态到任务
    public static Task  sendStateToTask(State state){
        Task task = state.getTask();
        int type = state.getState();
        if (type == 0){// ing
            task.getRecordResult().onLoading(state);
        }else
        if (type == 1){// success
            task.getRecordResult().onSuccess(state);
        }else {//failt
            task.getRecordResult().onFail(state);
        }
        return task;
    }

    //把配置文件 -> map
    public static Map<String,String> configFileToMap(String path){
        //
        File file = new File(path);
        if (!file.exists()){
            return null;
        }
        try {
            Map<String,String> map = new HashMap<>();
            String state = FileUtil.readFilePointToByte(path, State.STATE_POINT,4);
            String threadNumber = FileUtil.readFilePointToByte(path, State.THREAD_NUMBER_POINT,4);
            String totalSize = FileUtil.readFilePointToByte(path, State.TOTAL_SIZE_POINT,32);
            String currentSize = FileUtil.readFilePointToByte(path, State.CURRENT_SIZE_POINT,32);
            map.put("state",state);
            map.put("totalSize",totalSize);
            map.put("threadNumber",threadNumber);
            map.put("currentSize",currentSize);
            int munber = Integer.parseInt(threadNumber);
            for (int i = 0; i < munber;i++){
                int pi = State.SUB_THREAD_SOURCE_POINT  + 32 * i + i;
                map.put(String.valueOf(i),FileUtil.readFilePointToByte(path,pi,32));
            }
            return map;
        }catch (Exception e){
        }
        return null;
    }
    //获取 配置文件路径
    public static String getConfigFile(Task task){
        return task.getLocalPath()+ FileUtil.SEPARATOR + task.getDconfig();
    }
    //获取 临时文件路径
    public static String getTmpFile(Task task){
        return task.getLocalPath()+ FileUtil.SEPARATOR + task.getTmpfile();
    }
    //获取本地文件路径
    public static String getLocalFile(Task task){
        return task.getLocalPath()+ FileUtil.SEPARATOR + task.getLocalFileName();
    }
    //获取远程文件路径
    public static String getRemoteFile(Task task){
        return  task.getRemotePath()!=null && !"".equals(task.getRemotePath())?
                    task.getRemotePath() + FileUtil.SEPARATOR + task.getRemoteFileName():
                    FileUtil.SEPARATOR + task.getRemoteFileName();
    }
    //修改名字
    public static void renameTo(Task task){
        File file = new File(TaskUtils.getLocalFile(task));//转换名之后的文件
        File tempFile = new File(TaskUtils.getTmpFile(task)); //临时文件
        if (task.isCover()){
            //覆盖 - 删除已存在的源文件
            file.delete();
        }else{
            if (file.exists() && file.length() == task.getExistState().getTotalSize() && MD5Util.isEqualFileMd5(file, tempFile) ) {
                //不覆盖 删除临时文件
                FileUtil.deleteFile(TaskUtils.getTmpFile(task));
                return;
            }
        }

        if (tempFile.exists()){
            boolean flag = FileUtil.rename(tempFile, file);
            if (!flag){
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                Log.i("执行重试. 重命名 : " + tempFile.toString() );
                renameTo(task);
            }
        }

    }

    public static void connectParam(HttpURLConnection httpURLConnection, HashMap<String, String> params) {
        if (httpURLConnection!=null && params!=null){
            Iterator<Map.Entry<String,String>> iterator = params.entrySet().iterator();
            Map.Entry<String,String> entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                httpURLConnection.setRequestProperty(entry.getKey(),entry.getValue());
            }
        }
    }
    //文件内容转成文本
    public static void translateText(Task task) {
    String result = FileUtil.getFileText(getTmpFile(task));
    if (result!=null && task.getState()!=null){
        task.getProgressState().setResult(result);
    }
    }
    //插入下载成功记录到数据库
    public static void writeSql(Task task){
        try {
            String url = task.getUri();
            String path = getLocalFile(task);
            File file = new File(path);
            long length = file.length();
            String md5 = MD5Util.bytesGetMD5String(MD5Util.getFileMD5Bytes(file));
            DownloadFileRecordSql.get().addRecord(md5,url,path,0,length,length);
        } catch (Exception e) {
        }
    }


    public static String matchIpAddress(Task task) {
        return StringUtil.matchIpAddress(task.getUri());
    }


}
