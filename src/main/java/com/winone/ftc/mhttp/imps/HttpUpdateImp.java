package com.winone.ftc.mhttp.imps;

import com.winone.ftc.mcore.itface.Excute;
import com.winone.ftc.mentity.itface.Mftcs;
import com.winone.ftc.mentity.mbean.entity.State;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.StringUtil;
import com.winone.ftc.mtools.TaskUtils;
import com.winone.ftc.mtools.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by lzp on 2017/5/9.
 *  http 上传
 */
public class HttpUpdateImp extends Excute {
    public HttpUpdateImp(Mftcs manage) {
        super(manage);
    }
    @Override
    public Task load(Task task) {
        return null;
    }

    @Override
    public Task upload(Task task) {

        State state = task.getProgressStateAndAddNotify();

        //获取上传的地址
        String url = task.getUri();
        String local = TaskUtils.getLocalFile(task);
        File localFile = null;
        String httpType = task.getHttpType();
        String remotePath = StringUtil.encodeUrl(task.getRemotePath());//TaskUtils.getRemoteFile(task); //如果未指定远程文件路径 则只是 文件名
        String remoteFileName = StringUtil.encodeUrl(task.getRemoteFileName());
        try {
            Log.i(remoteFileName +" - "+StringUtil.encodeUrl(remoteFileName) +" - " +URLDecoder.decode(remoteFileName,"utf-8")+" - "+ URLDecoder.decode(URLDecoder.decode(remoteFileName,"utf-8"),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        //获取上传的本地文件路径
        if (task.getInStream()==null){
            localFile = new File(local);
            if (!localFile.exists()){
                state.setError("本地文件不存在:"+local);
                state.setState(-1);
                state.setRecord(true);
                finish(task);
                return null;
            }
            state.setTotalSize(localFile.length());//总大小
        }

        state.setRecord(true); //开始记录
        final String LINEND = "\r\n";
        final String PREFFIX = "--";
        final String BOUNDARY = "*****";

        HttpURLConnection httpURLConnection = null;
        DataOutputStream dos = null;
        InputStream is = null;
        try{

            httpURLConnection  = (HttpURLConnection) new URL(url).openConnection();

//            httpURLConnection .setConnectTimeout(30 * 1000); //连接超时为10秒
            httpURLConnection .setRequestMethod(httpType);//连接方式
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.addRequestProperty("specify-path", remotePath);
            httpURLConnection.addRequestProperty("specify-filename",remoteFileName);
            httpURLConnection.setDefaultUseCaches(false);
            httpURLConnection .setDoOutput(true);

            httpURLConnection.setDoInput(true);
            httpURLConnection.setDefaultUseCaches(false);
            httpURLConnection .setUseCaches(false);
            httpURLConnection.setChunkedStreamingMode(0);//直接将流提交到服务器上。
            dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(PREFFIX + BOUNDARY + LINEND);
            dos.writeBytes("Content-Disposition: form-data;" +  //类型
                    "name=\"ftc\";" +    //域名
                    "filename=\"" + remoteFileName + "\";"
                    + LINEND);
            dos.writeBytes("Content-Type: application/octet-stream"+ LINEND);

            dos.writeBytes(LINEND);
            if (task.getInStream()!=null){
                is = task.getInStream();
            }
            if (localFile!=null){
                is = new FileInputStream(localFile);
            }
            byte[] buff = new byte[1024 * 1024];
            int length = -1;
            while ((length = is.read(buff)) != -1) {
                dos.write(buff, 0, length);
                state.setCurrentSize(state.getCurrentSize() + length);
            }
            is.close();
            dos.writeBytes(LINEND);
            dos.writeBytes(PREFFIX + BOUNDARY + PREFFIX + LINEND);
            dos.flush();
            dos.close(); //关闭流

            httpURLConnection.connect();
            int recode = httpURLConnection.getResponseCode();
            if (recode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader =  new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuffer resultBuffer = new StringBuffer();
                String tempLine = null;
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }
                reader.close();
                state.setState(1);//成功
                state.setResult(resultBuffer.toString());
                Log.i("HTTP文件上传返回结果:"+state.getResult());
                }else{
                state.setError("http错误,返回值code:"+recode);
                state.setState(-1);
                }
            httpURLConnection.disconnect();

        }catch (Exception e){
            ;
            state.setError("http错误:"+e.getMessage());
            state.setState(-1);
        }finally {
            FileUtil.closeStream(is,dos,null,httpURLConnection);
            finish(task);
        }
        return null;
    }
}
