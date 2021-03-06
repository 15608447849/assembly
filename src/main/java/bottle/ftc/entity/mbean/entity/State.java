package bottle.ftc.entity.mbean.entity;

import bottle.ftc.http.itface.ContrailThread;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lzp on 2017/5/8.
 * 下载上传状态
 */
public class State{

    public interface ErrorCode{

        int ERROR_BY_FTP_CLIENT = 1;
        int ERROR_BY_FILE_NO_EXIST = 2;
        int ERROR_BY_FILE_CREATE_FAIL = 3;
        int ERROR_BY_TRANSLATE = 4;
        int ERROR_BY_REMOTE_SERVER = 5;
        int WARING = 0;
        int SOURCE_FILE_NO_EXIST = 101;
        int DWONLOAD_TIMEOUT = 102;
        int PERMISSION_DENIED = 103;
        int CONNECTED_TIMEOUT = 105;


    }

    public static final int STATE_POINT = 0;
    public static final int THREAD_NUMBER_POINT = 5;
    public static final int TOTAL_SIZE_POINT= 10;
    public static final int CURRENT_SIZE_POINT= 43;
    public static final int SUB_THREAD_SOURCE_POINT= 76;
    public static final int ERROR_POINT= 4096;


    private boolean isInitWrite = false;
    // 0进行中 1完成 -1失败
    private volatile int state;
    private long currentSize;//
    private long totalSize;//
    private String error;//错误信息
    private int threadNumber;//线程数
    private LinkedHashMap<String,Long> threadMap; //有序的map
    private final ReentrantLock lock = new ReentrantLock();
    private ArrayList<ContrailThread> threadList;//存子线程
    private Task task;
    private String result;
    private boolean isRecord = false;

    private long forTime = System.currentTimeMillis();//总耗时,初始化当前时间
    private long preCurSize = 0L;//上一次大小
    private long recordSecSize;
    protected State(Task task) {
        this.task = task;
        this.state = 0;
    }


    public boolean isInitWrite() {
        return isInitWrite;
    }

    public void setInitWrite(boolean initWrite) {
        isInitWrite = initWrite;
    }

    public int getState() {
        return state;
    }

    public String getStateString(){
        if (state==0) return "传输中";
        if (state==1) return "成功";
        if (state==-1) return "失败";
        return "未知状态";
    }
    public synchronized void setState(int state) {
        this.state = state;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getError() {
        return error==null?null:"["+error+"]";
    }

    public void setError(int code,String error) {
        this.error = (this.error==null?gnError(code,error) : this.error+","+gnError(code,error));
    }
    private String gnError(int code ,String error){
        return "{\""+code+"\":\""+error+"\"}";
    }


    public int getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(int threadNumber) {
        this.threadNumber = threadNumber;
    }

    public LinkedHashMap<String, Long> getThreadMap() {
        return threadMap;
    }

    public void setThreadMap(LinkedHashMap<String, Long> threadMap) {
        this.threadMap = threadMap;
    }

    //赋值
    public synchronized void putThreadMapValue(String key,long value){
        if (threadMap==null) return;
        try{
            lock.lock();
            threadMap.put(key,value);
        }finally {
            lock.unlock();
        }
    }
    //获取数组
    public synchronized ArrayList<Long> getThreadMapValueList(){
        if (threadMap==null) return null;
        try{
            lock.lock();
            ArrayList<Long> list = new ArrayList<>();
            Iterator<Long> progress = threadMap.values().iterator();
                while (progress.hasNext()){
                    list.add(progress.next());
                }
            return  list;
        }finally {
            lock.unlock();
        }

    }


    public ArrayList<ContrailThread> getThreadList() {

        return threadList;
    }

    public void setThreadList(ArrayList<ContrailThread> threadList) {
        this.threadList = threadList;
    }

    public synchronized void removeThreadListAll(){
        if (threadList==null){
            return;
        }
        Iterator<ContrailThread> itr = threadList.iterator();
        ContrailThread t;
        if (itr.hasNext()){
            t = itr.next();
            //while (t.isWork());
            t.mStop();
            itr.remove();
        }
    }
    public boolean isRecord() {
        return isRecord;
    }

    public synchronized void setRecord(boolean record) {
        isRecord = record;
    }

    public long getForTime(){
        return forTime;
    }
    public void setForTime(long forTime){
           this.forTime =  forTime - this.forTime;
    }

    public void setPreCurSizeByTime(int sec){
        recordSecSize = ((currentSize - preCurSize)/ 1024L) / sec;
       preCurSize = currentSize;
    }

    public double getProgressPercent(){
        return  ((double)currentSize / (double)totalSize) * 100 ;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("[");
        stringBuffer.append(" task : "+task.getTypeString()+" "+task.getUri());

        stringBuffer.append("; state: "+getStateString());
            stringBuffer.append("; currentSize: "+currentSize);

        if (totalSize>0){
            stringBuffer.append("; totalSize: "+totalSize);
        }else{
            stringBuffer.append("; totalSize: "+totalSize);
        }
        if (currentSize>0 && totalSize>0){
            stringBuffer.append("; percent: " +  String.format("%.2f", getProgressPercent()) + "%");
        }

        if (state==1){
            stringBuffer.append("; time: "+String.format("%.2f", ((double)forTime/1000)) + "s");//new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:ms]").format(new Date())
        }else{
            if (recordSecSize>0){
                stringBuffer.append("; progress: "+  String.format("%d Kb/s", recordSecSize));
            }
        }
        if (error!=null){
            stringBuffer.append("; error: "+error);
        }
        if (result!=null){
            stringBuffer.append("; result: "+result);
        }
        stringBuffer.append(" ]");


        /*if (threadNumber>0){
                List<Long> list = getThreadMapValueList();
            stringBuffer.append("\n[ ");
                //记录 每段线程的进度
                for (int i = 0; i < list.size(); i++) {

                    stringBuffer.append(i+" > "+list.get(i)+" ;");
                }
            stringBuffer.append(" ]");
        }*/
        return stringBuffer.toString();
    }
}
