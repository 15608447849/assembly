package com.winone.ftc.mentity.mbean.entity;

import com.winone.ftc.mentity.mbean.singer.StateNotification;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.StringUtil;
import com.winone.ftc.mtools.TaskUtils;

import java.io.InputStream;
import java.util.*;

/**
 * Created by lzp on 2017/5/8.
 *  任务对象
 */
public class Task {
    // 任务类型
    public enum Type{
        HTTP_UP,HTTP_DOWN,FTP_UP, FTP_DOWN,STREAM_UP,NOME
    }
    //当前状态
    public enum State{
        FINISH,NEW,RUNNING
    }
    /**
     *  http://host:port/path
     *  ftp://username:password@host:port/path
     */
    private long tid;//任务编号
    private String uri;// 1
    private String httpType;
    private String localPath;//本地路径
    private String remotePath;//远程路径
    private Type type;//类型
    private State state;//状态
    private boolean isCover;//是否覆盖本地文件下载
    private String localFileName;//本地文件名
    private String remoteFileName;//远程文件名
    private FtpInfo ftpInfo;
    private String dconfig;// 日志文件
    private String tmpfile;//临时文件
    private final List<onResult> resultList = Collections.synchronizedList(new ArrayList<onResult>());//回调接口列表
    private boolean isMumThread;//允许多线程
    private int maxThread; // 多线程数
    private HashMap<String,String> params;//参数
    private InputStream inStream;
    private boolean isText = false;
    //是否可重新下载
    private boolean isRestart = false;
    //是否直接下载,不检测任何状态和是否覆盖
    private boolean isDirectDown;
    //下载成功是否记录数据库
    private boolean isRecodeSql = true;
    //进度状态
    private com.winone.ftc.mentity.mbean.entity.State stateing;
    //下载的文件MD5
    private String downFileMd5;

    private TaskRecord mResult = new TaskRecord();
    //返回记录文件的回调
    public Task.onResult getRecordResult() {
        return mResult;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLocalPath() {
        return StringUtil.isEntry(localPath)?"":localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return StringUtil.isEntry(remotePath)?"":remotePath ;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getHttpType() {
        return StringUtil.isEntry(httpType)?"":httpType;
    }

    public void setHttpType(String httpType) {
        this.httpType = httpType;
    }

    public Type getType() {
        return type;
    }
    public String getTypeString() {
        if(type.equals(Type.FTP_DOWN)) return "FTP下载";
        if(type.equals(Type.FTP_UP)) return "FTP上传";
        if(type.equals(Type.HTTP_DOWN)) return "HTTP下载";
        if(type.equals(Type.HTTP_UP)) return "HTTP上传";
        if (type.equals(Type.STREAM_UP)) return "文件流上传";
        if(type.equals(Type.NOME)) return "不匹配";
        return "错误类型";
    }

    public void setType(Type type) {
        this.type = type;
    }

    public State getState() {
        return state;
    }

    public String getStateString() {
        if (state.equals(Task.State.FINISH)) return "结束";
        if (state.equals(Task.State.NEW)) return "未开始";
        if (state.equals(Task.State.RUNNING)) return "下载中";
        return "错误状态";
    }
    public void setState(State state) {
        this.state = state;
    }

    public boolean isCover() {
        return isCover;
    }

    public void setCover(boolean cover) {
        isCover = cover;
    }

    public String getLocalFileName() {
        return StringUtil.isEntry(localFileName)?"":localFileName;
    }

    public void setLocalFileName(String localFileName) {
        this.localFileName = localFileName;
    }

    public String getRemoteFileName() {
        return StringUtil.isEntry(remoteFileName)?"":remoteFileName ;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    public FtpInfo getFtpInfo() {
        return ftpInfo;
    }

    public void setFtpInfo(FtpInfo ftpInfo) {
        this.ftpInfo = ftpInfo;
    }

    public String getDconfig() {
        return dconfig;
    }

    public void setDconfig(String dconfig) {
        this.dconfig = dconfig;
    }

    public boolean isMumThread() {
        return isMumThread;
    }

    public void setMumThread(boolean mumThread) {
        isMumThread = mumThread;
    }

    public int getMaxThread() {
        return maxThread;
    }

    public void setMaxThread(int maxThread) {
        this.maxThread = maxThread;
    }
    public String getTmpfile() {
        return StringUtil.isEntry(tmpfile)?"":tmpfile;
    }

    public void setTmpfile(String tmpfile) {
        this.tmpfile = tmpfile;
    }

    //外界设置的回调
    public synchronized List<onResult> getOnResultList() {
        return resultList;
    }

    //外界设置的回调
    public synchronized Task  setOnResult(onResult onResult) {
        resultList.add(onResult);
        return this;
    }

    /**
     *
     * @return
     */
    public com.winone.ftc.mentity.mbean.entity.State getExistState(){
        return stateing;
    }
    //获取状态值
    public com.winone.ftc.mentity.mbean.entity.State getProgressState(){
        if (stateing == null) stateing = new com.winone.ftc.mentity.mbean.entity.State(this);
        return stateing;
    }
    //获取状态值
    public com.winone.ftc.mentity.mbean.entity.State getProgressStateAndAddNotify(){
        if (stateing == null) stateing = new com.winone.ftc.mentity.mbean.entity.State(this);
        StateNotification.getInstant().putState(stateing);
        return stateing;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public InputStream getInStream() {
        return inStream;
    }

    public void setInStream(InputStream inStream) {
        this.inStream = inStream;
    }

    public boolean isText() {
        return isText;
    }

    public void setText(boolean text) {
        isText = text;
    }

    public boolean isRestart() {
        return isRestart;
    }

    public void setRestart(boolean restart) {
        isRestart = restart;
    }

    public boolean isDirectDown() {
        return isDirectDown;
    }

    public void setDirectDown(boolean directDown) {
        isDirectDown = directDown;
    }

    public boolean isRecodeSql() {
        return isRecodeSql;
    }

    public void setRecodeSql(boolean recodeSql) {
        isRecodeSql = recodeSql;
    }

    public void setOtherTaskOnResult(Task otherTask) {
        try {
            Iterator<Task.onResult> iterator = otherTask.getOnResultList().iterator();
            Task.onResult onResult;
            while (iterator.hasNext()){
               onResult = iterator.next();
               iterator.remove();
               this.setOnResult(onResult);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public String toString() {
        StringBuffer sbff = new StringBuffer();
            sbff.append("[ id = " + tid);
            sbff.append("; uri = "+uri);
            sbff.append("; type = "+getTypeString());
            sbff.append("; state = "+ getStateString());
        if (type==Type.FTP_UP || type==Type.HTTP_UP){
            sbff.append("; remote = "+ TaskUtils.getRemoteFile(this));
        }
        if (type==Type.FTP_DOWN || type==Type.HTTP_DOWN){
            sbff.append("; local = "+ TaskUtils.getLocalFile(this));
        }
        if (resultList!=null && resultList.size()>0){
            sbff.append("; call size = "+ resultList.size());
        }
            sbff.append(" ]");
        return sbff.toString();
    }



    @Override
    public boolean equals(Object obj) {
        if (obj !=null && obj instanceof Task){
            Task t = (Task) obj;
//            if (t.getTid() == getTid()) return true;
            if (t.getType() == getType()){

                if (t.getType() == Type.HTTP_DOWN || t.getType()== Type.FTP_DOWN) {
                    // 远程url, 比较本地文件路径
                    return t.getUri().equals(getUri()) &&  TaskUtils.getLocalFile(t).equals(TaskUtils.getLocalFile(this)) ;
                }
                else if (t.getType() == Type.HTTP_UP || t.getType()== Type.FTP_UP){
                    //上传的地址, 本地地址, 比较远程地址
                    return  t.getUri().equals(getUri()) &&  TaskUtils.getLocalFile(t).equals(TaskUtils.getLocalFile(this)) && TaskUtils.getRemoteFile(t).equals(TaskUtils.getRemoteFile(this));
                }
            }
        }
        return false;
    }

    public String getDownFileMd5() {
        return downFileMd5;
    }

    public void setDownFileMd5(String downFileMd5) {
        this.downFileMd5 = downFileMd5;
    }

    @Override
    public int hashCode() {
        return uri.hashCode()+TaskUtils.getLocalFile(this).hashCode()+TaskUtils.getRemoteFile(this).hashCode();
    }

    //下载结果回调
    public interface onResult{
        void onSuccess(com.winone.ftc.mentity.mbean.entity.State state);
        void onFail(com.winone.ftc.mentity.mbean.entity.State state);
        void onLoading(com.winone.ftc.mentity.mbean.entity.State state);
    }
    public static abstract class onResultAdapter implements onResult{
        public void onSuccess(com.winone.ftc.mentity.mbean.entity.State state){}
        public void onFail(com.winone.ftc.mentity.mbean.entity.State state){}
        public void onLoading(com.winone.ftc.mentity.mbean.entity.State state){}
    }

}
