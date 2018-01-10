package com.winone.ftc.mentity.mbean.entity;

import com.winone.ftc.mcore.imps.FtcManager;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.StringUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lzp on 2017/5/8.
 * create task factory
 */
public class TaskFactory {


    public static final String CONF = ".conf";
    public static final String TMP = ".tmp";

    public static synchronized final  long getCurrentDate(){
        return System.nanoTime();
    }


    //创建Http任务
    public static Task httpTaskDown(String url,String httpType,String localDir,String fileName,boolean isCover){
        return httpTaskDown(url,httpType,localDir,fileName,null,isCover,null);
    }
    //创建Http任务
    public static Task httpTaskDown(String url,String httpType,String localDir,String fileName,boolean isCover,String sourceMd5){
        return httpTaskDown(url,httpType,localDir,fileName,null,isCover,sourceMd5);
    }
    //创建Http任务
    public static Task httpTaskDown(String url,String httpType,String localDir,String fileName,HashMap<String,String> headerMap,boolean isCover){
        return httpTaskDown(url,httpType,localDir,fileName,headerMap,isCover,null);
    }
    //创建Http任务
    public static Task httpTaskDown(String url,String httpType,String localDir,String fileName,HashMap<String,String> headerMap,boolean isCover,String sourceMd5){
        Task task = new Task();
        task.setTid(getCurrentDate());
        task.setType(Task.Type.HTTP_DOWN);
        task.setState(Task.State.NEW);
        task.setUri(StringUtil.encodeUrl(StringUtil.filter(url)));
        task.setHttpType(httpType);
        task.setLocalPath(localDir);
        task.setLocalFileName(fileName);
        task.setTmpfile(fileName+TMP);
        task.setDconfig(fileName+CONF);
        task.setCover(isCover);
        task.setMumThread(false);
        task.setMaxThread(0);
        task.setParams(headerMap);
        task.setDownFileMd5(sourceMd5);
        return task;
    }

    //创建Http任务
    public static Task httpTaskDown(String url,String httpType){
        Task task = httpTaskDown(url,httpType,"./",getCurrentDate()+"",true);
        task.setText(true);
        return task;
    }
    //切割ftp字符串 -
    public static HashMap<String,String> parseFtpUrl(String uri){
        //ftp://user:pass@host:port/path/file
        uri = StringUtil.filter(uri);
        String str = uri.substring(uri.indexOf("//") + 2);
        String[] arr = str.split("@");
        String user = arr[0].substring(0,arr[0].indexOf(":"));
        String pass = arr[0].substring(arr[0].indexOf(":")+1);
        str = arr[1];
        String host = str.substring(0,str.indexOf(":"));
        String port = str.substring(str.indexOf(":")+1,str.indexOf("/"));
        str = str.substring(str.indexOf("/"));
        String path = str.substring(0,str.lastIndexOf("/"));
        String file = str.substring(str.lastIndexOf("/")+1);
        Log.e("切割ftp路径",uri+"->\n"+host+" "+port+" "+user+" "+pass+" "+path+" "+file);
        HashMap<String,String> map = new HashMap<>();
        map.put("user",user);
        map.put("pass",pass);
        map.put("host",host);
        map.put("port",port);
        map.put("path",path);
        map.put("file",file);
        return map;
    }
    public static FtpInfo getFtpInfo(Map<String,String> map){
        FtpInfo ftpInfo = new FtpInfo();
        ftpInfo.setHost(map.get("host"));
        ftpInfo.setPort(Integer.parseInt(map.get("port")));
        ftpInfo.setUserName(map.get("user"));
        ftpInfo.setPassword(map.get("pass"));
        return ftpInfo;
    }
    //创建 ftp 下载任务
    public static Task ftpTaskDown(String url,String localDir,String fileName,boolean isCove,String sourceMD5){
        url = StringUtil.filter(url);
        //解析协议 -> ftp信息
        Map<String,String> map = parseFtpUrl(url);
        Task task = new Task();
        task.setTid(getCurrentDate());
        task.setUri(url);
        task.setFtpInfo(getFtpInfo(map));
        task.setCover(isCove);
        task.setType(Task.Type.FTP_DOWN);
        task.setState(Task.State.NEW);
        task.setLocalPath(localDir);
        task.setLocalFileName(fileName);
        task.setTmpfile(fileName+TMP);
        task.setDconfig(fileName+CONF);
        task.setRemotePath(map.get("path"));
        task.setRemoteFileName(map.get("file"));
        task.setDownFileMd5(sourceMD5);
        return task;
    }
    //创建 ftp 下载任务
    public static Task ftpTaskDown(String url,String localDir,String fileName,boolean isCove){
        return ftpTaskDown(url,localDir,fileName,isCove,null);
    }

    //http上传本地文件到指定url
    public static Task httpTaskUpdate(String url,String httpType,String localDir,String fileName,String remotePath,String remoteName){
        Task task = new Task();
        task.setTid(getCurrentDate());
        task.setUri(StringUtil.filter(url));
        task.setType(Task.Type.HTTP_UP);
        task.setState(Task.State.NEW);
        task.setLocalPath(localDir);
        task.setLocalFileName(fileName);
        task.setDconfig(fileName+CONF);
        task.setHttpType(httpType);
        task.setRemotePath(remotePath);
        task.setRemoteFileName(remoteName);
        return task;
    }
    //http上传本地文件到指定url
    public static Task httpTaskUpdate(String url, String httpType, InputStream inputStream,String remotePath, String remoteName){
        Task task = new Task();
        task.setTid(getCurrentDate());
        task.setUri(StringUtil.filter(url));
        task.setType(Task.Type.HTTP_UP);
        task.setState(Task.State.NEW);
        task.setHttpType(httpType);
        task.setRemotePath(remotePath);
        task.setRemoteFileName(remoteName);
        task.setInStream(inputStream);
        return task;
    }

    //ftp上传
    public static Task ftpTaskUpdate(String url,String localDir,String fileName){
        url = StringUtil.filter(url);
        //解析协议 -> ftp信息
        Map<String,String> map = parseFtpUrl(url);

        Task task = new Task();
        task.setTid(getCurrentDate());
        task.setUri(url);
        task.setFtpInfo(getFtpInfo(map));
        task.setType(Task.Type.FTP_UP);
        task.setState(Task.State.NEW);
        task.setLocalPath(localDir);
        task.setLocalFileName(fileName);
        task.setTmpfile(fileName+TMP);
        task.setDconfig(fileName+CONF);
        task.setRemotePath(map.get("path"));
        task.setRemoteFileName(map.get("file"));
        return task;
    }






    /**
     * URL list -> string 字符串
     */
    public static List<String> httpUrlTanslateString(List<String> urlList){
        if (urlList==null || urlList.isEmpty()) return null;
        final String type = "POST";
        final List<Task> taskList = new ArrayList<>();
        final List<String> resultList = new ArrayList<>();
        for (String url : urlList){
            taskList.add(httpTaskDown(url,type).setOnResult(new Task.onResultAdapter() {
                @Override
                public void onSuccess(State state) {
                    resultList.add(state.getResult());
                }
            }));
        }
        if (FtcManager.get().listExecuteBlock(taskList)) return resultList;
        return null;
    }





}
