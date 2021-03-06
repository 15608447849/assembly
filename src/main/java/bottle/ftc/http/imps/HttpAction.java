package bottle.ftc.http.imps;

import bottle.ftc.tools.FileUtil;
import bottle.ftc.http.itface.ContrailThread;
import bottle.ftc.entity.mbean.entity.State;
import bottle.ftc.entity.mbean.entity.Task;
import bottle.ftc.tools.TaskUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lzp on 2017/5/10.
 * 多线程下载使用
 */
public class HttpAction implements ContrailThread.onAction
{

    private State state;
    private long start,end;
    private Task task;
    private boolean isTag;
    private long progress = 0;//进度
    private ContrailThread contrailThread;
    private String key;
    private int tryRemake = 0;
    private boolean isWorking = false;
    public HttpAction(State state, long start, long end, long progress) {
        this.state = state;
        this.start = start;
        this.end = end;
        this.progress = progress;
        task = state.getTask();
    }

    @Override
    public void build(ContrailThread thread) {
        this.contrailThread = thread;
        key = thread.getId()+thread.getName()+hashCode();
        state.putThreadMapValue(key,progress);//放入的是进度
        isTag = true;
        state.getThreadList().add(thread);
        this.contrailThread.start(); //开启
    }

    @Override
    public void action() throws Exception {
        //下载
       progress = 0L;
        if ((start+progress) < end){
            //开始下载
//            Log.e(task.getUri()+" 开始:"+ start+"-> "+end+" ,当前: "+ progress);
            //http 连接 下载
            HttpURLConnection httpConnection = null;
            InputStream input = null;
            RandomAccessFile out = null;
            try {
                httpConnection = (HttpURLConnection) new URL(task.getUri()).openConnection();

                httpConnection.setRequestProperty("Connection", "Keep-Alive");
                httpConnection.setRequestProperty("Content-Type", "application/octet-stream");
                httpConnection.setRequestProperty("Charset", "UTF-8");
                TaskUtils.connectParam(httpConnection,task.getParams());
                httpConnection.setDefaultUseCaches(false);
                httpConnection .setUseCaches(false);
                httpConnection.setRequestProperty("Range", "bytes=" + (start+progress) +"-"+ end); //范围
                httpConnection.setRequestMethod(task.getHttpType());
                httpConnection.setDoInput(true);
                httpConnection.setConnectTimeout(10*1000); //连接超时
                httpConnection.setReadTimeout(30*1000);//读取超时
                httpConnection.connect();

                int code = httpConnection.getResponseCode();

                if ( code==206 || code==200) {
                    input = httpConnection.getInputStream();
                    out = new RandomAccessFile(TaskUtils.getTmpFile(task), "rw");
                    out.seek(start+progress);

                    byte[] b = new byte[task.getDownloadBufferSize()]; // 8k 缓存
                    int len ;
                    ByteBuffer buff = ByteBuffer.allocate(b.length);//缓冲区
                    FileChannel outChannel =  out.getChannel();
                    isWorking = true;
                    long cTime = System.currentTimeMillis();
                    long sleepTime = 0L;
                     while (isTag && (len = input.read(b)) != -1) {
                         buff.clear();
                         buff.put(b,0,len);
                         buff.flip();
                         outChannel.write(buff);
                         progress += len;
                         //更新状态
                         state.putThreadMapValue(key,progress);//放入的是进度
//                       假设下载或者上传速度上限是500 (KB/s)
                         //那么发送一个固定的字节数据(假设是1024*8字节)的时间花费是：(1024*8)/500
                         //而在实际情况下，发送n字节的数据只花费了t秒，那么发送该发送线程就应该睡眠(1024*8)/500-t秒

                         if(task.getDownloadLimitMax()>0){
                             sleepTime = (System.currentTimeMillis() - cTime);
                             sleepTime = (task.getDownloadBufferSize() / task.getDownloadLimitMax() - sleepTime ) * task.getMaxThread();
                             if (sleepTime > 0){
                                 Thread.sleep(sleepTime);
                             }
                             cTime = System.currentTimeMillis();
                         }

                    }

                    isWorking = false;
                    outChannel.close();
                    input.close();
                    out.close();
                    while (state.getThreadMap().get(key)!=progress){
                        state.putThreadMapValue(key,progress);//放入的是进度
                    }
                    if ( (start+progress) < end){
                        //下载不完整
                        if (tryRemake<3){
                            tryRemake++;
                            action();
                        }else{
                            state.setError(State.ErrorCode.ERROR_BY_TRANSLATE,"file download error:"+ start+" added to "+progress+" not is "+ end);
                            state.setState(-1);
                        }
                    }
                } else {
                        throw new IllegalStateException("http download action entity get server resp code:" + code);
                }

            } catch (Exception e) {
                if (e instanceof SocketTimeoutException){
                    state.setError( State.ErrorCode.DWONLOAD_TIMEOUT,"下载超时错误 : " +e);
                }else{
                    state.setError( State.ErrorCode.ERROR_BY_REMOTE_SERVER,"远程服务器错误 : " +e);
                }

                state.setState(-1);
            } finally {
                //关闭流
                FileUtil.closeStream(input,null,out,httpConnection);
            }
        }


    }

    @Override
    public void stop() {
        //停止
        isTag = false;
    }

    @Override
    public void error(Exception e) {
        stop();
        StringBuffer stringBuffer = new StringBuffer();
        for (StackTraceElement throwable : e.getStackTrace()){
            stringBuffer.append(throwable);
        }
        state.setError(State.ErrorCode.ERROR_BY_TRANSLATE,stringBuffer.toString());
        state.setState(-1); // 多线程的时候, 有一个出问题 - > 所有任务停止 (有专门的线程循环检测状态值)
    }

    @Override
    public boolean isWork() {
        return isWorking;
    }
}
