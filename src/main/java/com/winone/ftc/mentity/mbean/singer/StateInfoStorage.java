package com.winone.ftc.mentity.mbean.singer;

import com.winone.ftc.mcore.imps.FtcManager;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mentity.mbean.entity.TaskFactory;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.StringUtil;
import com.winone.ftc.mtools.TaskUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lzp on 2017/5/8.
 *  保存 当前正在下载中的任务 - 信息
 */
public class StateInfoStorage extends Thread{

    private static final String TAG = "保存下载中信息";
    private static final String FTC_DOWN_LIST = "ftc";
    private long TIME = 6*60*60*1000L;
    private ReentrantLock lock = new ReentrantLock();
    private String systemPath ;
    private File dir;
    private boolean isRecode = false;//默认不记录 - 请手动开启
    private final int LOOP_TIME = 1000 * 60 * 10; //10分钟;

    private StateInfoStorage() {
        setName("FTC@StorageTaskInfo");
        setDaemon(true);
        start();
    }

    private static class  Holder{
        private static StateInfoStorage storage = new StateInfoStorage();
    }
    public static StateInfoStorage get(){
        return Holder.storage;
    }

    private void createDirs() {
        if (systemPath==null && dir==null){
            systemPath = FileUtil.PROGRESS_HOME_PATH + FileUtil.SEPARATOR +FTC_DOWN_LIST+FileUtil.SEPARATOR;
            dir = new File(systemPath);
            if (!dir.exists()){
                dir.mkdirs();
            }
        }

    }

    public void addSateFile(Task task){

        try{
            lock.lock();
            if (task.getState() != Task.State.NEW) return;
            if (isRecode){
                createDirs();
                //获取文件下所有文件的文件名 - 是否存在和这个任务相同的文件
                File[] array = dir.listFiles();
                for (File file : array){
                    if (file.getName().equals(String.valueOf(task.getTid()))){
                        return;
                    }
                }
                addTaskToFile(task);
                //在指定文件下 创建 这个任务信息文件 - 文件名 tid
//                    Log.i("任务记录: "+ task+" "+(addTaskToFile(task)?"成功.":"失败."));
            }
        }finally {
            lock.unlock();
        }
    }

    public  void removeStateFile(Task task) {
        try{
            lock.lock();
            if (!isRecode) return;
            createDirs();
            File[] array = dir.listFiles();
            for (File file : array){
                if (file.getName().equals(String.valueOf(task.getTid()))) {
                    file.delete();
                }
            }
        }finally {
            lock.unlock();
        }

    }


    private boolean addTaskToFile(Task task) {
            String fileName = Long.toString(task.getTid()); // 下载任务id
            Map<String,String> map = new HashMap<>();
                map.put("tid",fileName);
                map.put("uri",task.getUri());//协议
                map.put("config", TaskUtils.getConfigFile(task));//配置文件
                map.put("tmp",task.getTmpfile());
                map.put("type",String.valueOf(task.getType()));//类型
                //本地文件路径
                map.put("localPath",TaskUtils.filterPath(task.getLocalPath()));//local path - file
                map.put("localFileName",task.getLocalFileName());
                //http
                map.put("httpType",task.getHttpType());// http type
                map.put("isMumThread",String.valueOf(task.isMumThread()));//是否多线程下载
                map.put("maxThread",String.valueOf(task.getMaxThread())); //多线程数量
                //远程文件路径
                map.put("remotePath",TaskUtils.filterPath(task.getRemotePath()));
                map.put("remoteFileName",task.getRemoteFileName());
                //是否覆盖
                map.put("cover",String.valueOf(task.isCover()));//是否覆盖
                //参数
                map.put("params",StringUtil.map2string(task.getParams()));
                map.put("isText",String.valueOf(task.isText()));
            //写入数据
        return FileUtil.writeMapToFile(map,systemPath+fileName);
    }


    @Override
    public void run() {
        while (true){
            try {
               sleep(LOOP_TIME);
            } catch (InterruptedException e) {
            }
            checkTask();
        }
    }



    public void checkTask(){

        try{
            lock.lock();
            if (!isRecode) return;
            createDirs();
            //查询 任务列表
            File[] files = dir.listFiles();
            if (files.length>0){

                Map<String,String> map;
                String configPath;
                String uri;

                for (int i = 0; i < files.length;i++){
                   map =  FileUtil.readFileToMap(files[0].getAbsolutePath());
                    if (map!=null){
                        //还原一个任务
                        Task task = restore(map);
                        //查看是否存在config文件
                        if (FileUtil.checkFileNotCreate(TaskUtils.getConfigFile(task))){
                            //获取文件最后修改时间
                            if (( System.currentTimeMillis() -  (new File(TaskUtils.getConfigFile(task)).lastModified()) )< TIME){
                             continue;
                            }
                        }
                        if (files[i].delete()){
                            //执行任务
                            FtcManager.get().execute(task);
                        }
                    }

                }
            }
        }finally {
            lock.unlock();
        }
    }

    private Task restore(Map<String, String> map) {

        try {
            Task task = new Task();
            task.setState(Task.State.NEW);
            task.setTid(Long.parseLong(map.get("tid")));
            task.setUri(map.get("uri"));
            task.setHttpType(map.get("httpType"));
            task.setLocalPath(map.get("localPath"));
            task.setLocalFileName(map.get("localFileName"));
            task.setRemotePath(map.get("remotePath"));
            task.setRemoteFileName("remoteFileName");
            task.setMumThread(Boolean.parseBoolean(map.get("isMumThread")));
            task.setCover(Boolean.parseBoolean(map.get("cover")));
            task.setMaxThread(Integer.parseInt(map.get("maxThread")));
            task.setParams(StringUtil.string2map(map.get("params")));
            task.setText(Boolean.parseBoolean(map.get("isText")));
            task.setTmpfile(map.get("tmp"));
            task.setDconfig(task.getLocalFileName()+ TaskFactory.CONF);
            Task.Type type = Task.Type.valueOf(map.get("type"));
            task.setType(type);
            if (type.equals(Task.Type.FTP_DOWN) || type.equals(Task.Type.FTP_UP)){
                task.setFtpInfo(TaskFactory.getFtpInfo(TaskFactory.parseFtpUrl(task.getUri())));
            }
            return task;
        } catch (Exception e) {
        }
        return null;
    }

    public void setRecode(boolean recode) {
        this.isRecode = recode;
    }
}
