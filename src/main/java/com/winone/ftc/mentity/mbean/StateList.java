package com.winone.ftc.mentity.mbean;

import com.winone.ftc.mcore.imps.ManagerImp;
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
public class StateList extends Thread{

    private static final String TAG = "保存下载中信息";
    private static final String FTC_DOWN_LIST = "ftc";
    private ReentrantLock lock = new ReentrantLock();
    private String systemPath ;
    private File dir;
    private ManagerImp manage;
    private boolean isRecode = false;//默认不记录 - 请手动开启
    public StateList(ManagerImp manage) {
        //取得根目录路径
        this.manage = manage;
        start();
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
            File f = null;
            for (File file : array){
                if (file.getName().equals(String.valueOf(task.getTid()))) {
                    f = file;
                    break;
                }
            }
            if (f!=null){
                f.delete();
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
                map.put("localPath",task.getLocalPath());//local path - file
                map.put("localFileName",task.getLocalFileName());
                //http
                map.put("httpType",task.getHttpType());// http type
                map.put("isMumThread",String.valueOf(task.isMumThread()));//是否多线程下载
                map.put("maxThread",String.valueOf(task.getMaxThread())); //多线程数量
                //远程文件路径
                map.put("remotePath",task.getRemotePath());
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
               sleep(1000 * 60);
            } catch (InterruptedException e) {
            }
            checkTask();
        }
    }



    public void checkTask(){

        try{
            lock.lock();
            if (manage == null || !isRecode) return;
            createDirs();
            //查询 任务列表
            File[] files = dir.listFiles();
            if (files.length>0){
                long taskTid;
                Map<String,String> map;
                String configPath;
                String uri;

                for (int i = 0; i < files.length;i++){

                   map =  FileUtil.readFileToMap(files[0].getAbsolutePath());
                    if (map!=null){

                        try{
                            taskTid = Long.parseLong(files[i].getName());
                        }catch (NumberFormatException ex){
                            continue;
                        }
                        uri = map.get("uri");
                        configPath = map.get("config");
                        //去进行中的任务队列查询是否存在 (tid+url)
                        if (StringUtil.isEntry(uri) || StringUtil.isEntry(configPath)) continue;
                        //添加任务
                        //删除此文件
                        Task task = sendTask(map);
                        if (task!=null){
                            FileUtil.deleteFile(files[i].getAbsolutePath());
                            Task.Type type = task.getType();
                            //正在下载中的任务
                            if (type.equals(Task.Type.FTP_DOWN) || type.equals(Task.Type.HTTP_DOWN)){
                                manage.load(task);
                            }
                            if (type.equals(Task.Type.FTP_UP) || type.equals(Task.Type.HTTP_UP)){
                                manage.upload(task);
                            }
                        }
                    }
                }
            }
        }finally {
            lock.unlock();
        }
    }

    private Task sendTask(Map<String, String> map) {
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
        task.setDconfig(task.getLocalFileName()+TaskFactory.CONF);
        Task.Type type = Task.Type.valueOf(map.get("type"));
        task.setType(type);
        if (type.equals(Task.Type.FTP_DOWN) || type.equals(Task.Type.FTP_UP)){
            task.setFtpInfo(TaskFactory.getFtpInfo(TaskFactory.parseFtpUrl(task.getUri())));
        }
        if (manage.getCurrentTaskQueue().isExistTask(task)) return null;//如果存在任务 - 取消
        return task;
    }


    public void setRecode(boolean recode) {
        this.isRecode = recode;
    }
}
