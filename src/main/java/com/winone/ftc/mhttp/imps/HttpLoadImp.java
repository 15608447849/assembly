package com.winone.ftc.mhttp.imps;

import com.winone.ftc.mcore.itface.Excute;
import com.winone.ftc.mentity.itface.Mftcs;
import com.winone.ftc.mentity.mbean.entity.TaskFactory;
import com.winone.ftc.mhttp.itface.ContrailThread;
import com.winone.ftc.mentity.mbean.entity.State;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lzp on 2017/5/9.
 * load imps
 */
public class HttpLoadImp extends Excute{
    private static final  long limit_150M = (1024L  * 1024 * 150); //150M
    private static final  long limit_700M = (1024L  * 1024 * 700); //700M
    private static final long limit_2G = (1024L  * 1024 * 1024 * 2); //2G
    private static final long limit_5G = (1024L  * 1024 * 1024 * 5);

    public HttpLoadImp(Mftcs manager) {
        super(manager);
    }

    //获取文件大小
    public long getRemoteFileSize(Task task,int tag,boolean failTry) {
        long fileLength = -1;
        tag++;

        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(task.getUri()).openConnection();

            if (failTry){
                httpURLConnection .setRequestMethod(task.getHttpType());
            }else{
                httpURLConnection .setRequestMethod("HEAD");
            }
            httpURLConnection.setRequestProperty("Connection", "Close");//close
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-type", "application/octet-stream");
            httpURLConnection.setRequestProperty("Range", "bytes=" +0 + "-");
            httpURLConnection.setRequestProperty("Accept-Encoding", "identity");
            TaskUtils.connectParam(httpURLConnection,task.getParams());

            httpURLConnection.setDefaultUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setConnectTimeout(10*1000); //连接超时
            httpURLConnection.setReadTimeout(10*1000);//读取超时
            httpURLConnection.connect();
            int code = httpURLConnection.getResponseCode();
            if( code == 206 || code == 200){
                fileLength = httpURLConnection.getContentLength();
                if (fileLength<=0) fileLength=httpURLConnection.getContentLengthLong();
                if (code == 206){
                    task.setMaxThread(1);
                   if (fileLength >=limit_150M){
                       task.setMaxThread(2);
                   }
                    if (fileLength >=limit_700M){
                        task.setMaxThread(3);
                    }
                    if (fileLength >=limit_2G){ // 2G
                        task.setMaxThread(4);
                    }
                    if (fileLength >= limit_5G){ //5G
                        task.setMaxThread(5);
                    }
                    task.setMumThread(true);
                }

                if (code == 200) task.setMumThread(false);

                if (failTry){
                    //尝试下载
                    State state = task.getExistState();
                    state.setResult("100");
                    state.setRecord(true);
                    InputStream inputStream = null;
                    OutputStream fileOutputStream  = null;
                    try {
                        inputStream = httpURLConnection.getInputStream();
                        fileOutputStream = new FileOutputStream(TaskUtils.getLocalFile(task));
                        int index;
                        byte[] b = new byte[1024*2];
                        while ( (index=inputStream.read(b)) >0){
                            fileOutputStream.write(b,0,index);
                            state.setCurrentSize(state.getCurrentSize() + index);
                        }
                        state.setResult("200");
                    } catch (Exception e) {
                        state.setResult(state.getResult()+"\n"+e.toString());
                    }finally {
                        FileUtil.closeStream(inputStream,fileOutputStream,null,httpURLConnection);
                    }
                    state.setError(State.ErrorCode.WARING,"获取文件大小:"+ fileLength+", 尝试下载完成.未检测结果是否正确.");
                    fileLength = -2;
                }else{
                    if (fileLength<=0 && (code==200 || code==206)){
                        return getRemoteFileSize(task,tag,true);
                    }
                }
                // Log.i(uri+" ,返回值:"+ code+" , 文件大小:"+ fileLength+" byte. 是否可以多线程下载 : "+ task.isMumThread()+ (task.isMumThread()?",最大线程数:"+task.getMaxThread():""));

            }else if (code==-1) {
                return getRemoteFileSize(task,tag,failTry);
            }
            else
            {
                throw new Exception( task.getUri() +" http response code: "+ code);
            }

        } catch (Exception e) {
            if (e instanceof UnknownHostException || e instanceof SocketTimeoutException){
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e1) {
                }
                if (tag<10){
                    return getRemoteFileSize(task,tag,failTry);
                }
            }
            task.getExistState().setError(State.ErrorCode.ERROR_BY_REMOTE_SERVER,"远程文件错误:\n "+ e.toString());

        }finally {
            if (httpURLConnection!=null){
                try {
                    httpURLConnection.disconnect();
                } catch (Exception e) {
                }
            }
        }
        return fileLength;
    }

    @Override
    public Task load(Task task) {

        State state = task.getProgressStateAndAddNotify();
        //获取文件大小 -2>不检测
        long remoteFileSize = getRemoteFileSize(task,0,task.isDirectDown());

        state.setTotalSize(remoteFileSize); //设置文件总大小
        if (remoteFileSize<=0){
            state.setState(-1);
            state.setError(State.ErrorCode.ERROR_BY_TRANSLATE,task.getUri()+" -> "+ state.getError());
            state.setRecord(true);
            if (remoteFileSize==-2 && state.getResult().equals("200")) {
                task.setCover(false);//避免临时文件覆盖
                state.setState(1); //成功
            }
            finish(task);
            return null;
        }
        if (!TaskUtils.judgeCover(task)){
            finish(task);
            return null;
        }



        state.setThreadMap(new LinkedHashMap<>());
        state.setThreadList(new ArrayList<>());
        //保存线程的起点和终点
        ArrayList<Long[]> list = new ArrayList<>();
        if (task.isMumThread() && task.getMaxThread()>1){
            int tnum = task.getMaxThread();//获取任务指定的线程数量
            state.setThreadNumber(tnum);
            //每段的范围 : range*0 -- range*1-1,  range*1 -- rang*2-1 , range*2 -- totlsize;
            long range = state.getTotalSize() / tnum;

            // 设置每段的起点和终点 当前进度
            long start;
            long end;
            for (int i = 0;i<tnum;i++){
                start = range*i;
                end = (i == tnum-1)? state.getTotalSize():range*(i+1) - 1;
                list.add(new Long[]{start,end,0L});
            }

            //获取本地是否存在配置文件
            Map<String,String> map = TaskUtils.configFileToMap(TaskUtils.getConfigFile(task));
            if (map!=null){
                // Log.i("多线程下载 已存在数据 :"+map);
                int cnun = 0; // 线程数
                try {
                    cnun = Integer.parseInt(map.get("threadNumber"));
                } catch (NumberFormatException e) {
                    cnun = -1;
                }
                if (cnun == tnum){ //配置文件大小和配置文件记录线程数相同
                    // 改变起点
                    String var;
                    for (int i=0;i<cnun;i++){
                        var = map.get(String.valueOf(i));//每段的进度
                        if (var!=null){
                            try {
                                list.get(i)[2] = Long.parseLong(var);
                            } catch (NumberFormatException e) {
                                list.get(i)[2] = 0L;
                            }
                        }
                    }
                    try {
                        Long currentSize =  Long.parseLong(map.get("currentSize"));
                        state.setCurrentSize(currentSize);//已下载量
                    } catch (NumberFormatException e) {
                    }
                }
            }
            //RandomAccessFile - 设置临时文件的大小
            File tmpFile = new File(TaskUtils.getTmpFile(task));
            if(!tmpFile.exists() || tmpFile.length() != state.getTotalSize()){
                RandomAccessFile raf = null;
                try {
                    //创建文件
                    raf = new RandomAccessFile(tmpFile,"rw");
                    raf.setLength(state.getTotalSize());
                    raf.close();
                } catch (Exception e) {
                    state.setError(State.ErrorCode.ERROR_BY_FILE_CREATE_FAIL,"创建临时文件错误:"+e.getMessage());
                    state.setState(-1);
                } finally {
                    FileUtil.closeStream(null,null,raf,null);
                }
            }

        }else{
            //单线程
            //临时文件
            File tmp = new File(TaskUtils.getTmpFile(task));
            if (tmp.exists() && tmp.length()<=state.getTotalSize() ) {
                //临时文件存在
                state.setCurrentSize(tmp.length());//临时文件大小
            }else{

                //创建
                try {
                    FileUtil.deleteFile(TaskUtils.getTmpFile(task));
                    tmp.createNewFile();
                } catch (IOException e) {
                    state.setError(State.ErrorCode.ERROR_BY_FILE_CREATE_FAIL,"创建临时文件错误:"+e.getMessage());
                    state.setState(-1);
                }
            }
            list.add(new Long[]{0L,state.getTotalSize(),state.getCurrentSize()});
        }
        state.setRecord(true); //开始记录
        if (state.getState()==0){
            //代表可以下载这个任务,本地准备就绪
            CountDownLatch cdl = new CountDownLatch(list.size());//这里的数字，开启几个线程就写几
            //创建这个多线程下载任务
                for (int i=0;i<list.size();i++){
                    new ContrailThread(new HttpAction(state,list.get(i)[0],list.get(i)[1],list.get(i)[2]),cdl);
                }

//            Log.i("当前任务 :" + task.getUri()+" 已创建 执行 子线程 , 开始.");
            List<Long> progressList;//进度列表
            long current_progress;//当前进度
            while (state.getState() == 0){//下载中

                //设置下载进度
                progressList = state.getThreadMapValueList();
                if (progressList ==null) continue;
                current_progress = 0L;
                for (Long progress : progressList){
                    current_progress+=progress;
                }
                state.setCurrentSize(current_progress);
                if (state.getCurrentSize() == state.getTotalSize()){
                    state.setState(1); // 下载成功
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            //停止所有子线程并且移除队列
            state.removeThreadListAll();
            try {
                cdl.await();//等待所有子线程结束
            } catch (InterruptedException e) {
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            //再次检测文件完整性 检测本地文件大小,不存在检测临时文件大小
            if (!FileUtil.checkFileLength(TaskUtils.getLocalFile(task),state.getTotalSize())){
               if (!FileUtil.checkFileLength(TaskUtils.getTmpFile(task),state.getTotalSize())){
                    state.setState(-1);
                    state.setError(State.ErrorCode.ERROR_BY_TRANSLATE,"检测文件长度错误,大小不正确或文件不存在.");
                }
            }
        }
        finish(task);//任务完成
        return null;
    }




    @Override
    public Task upload(Task task) {
        return null;
    }






}
