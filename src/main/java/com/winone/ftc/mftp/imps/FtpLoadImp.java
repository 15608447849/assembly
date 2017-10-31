package com.winone.ftc.mftp.imps;

import com.winone.ftc.mcore.itface.Excute;
import com.winone.ftc.mentity.itface.Mftcs;
import com.winone.ftc.mentity.mbean.entity.State;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.TaskUtils;
import com.winone.ftc.mftp.ftps.MftpManager;
import com.winone.ftc.mftp.itface.FtpClientIntface;
import com.winone.ftc.mftp.itface.FtpManager;
import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lzp on 2017/5/11.
 * FTP 下载 实现
 */
public class FtpLoadImp extends Excute {
    public FtpLoadImp(Mftcs manager) {
        super(manager);
    }
    @Override
    public Task load(Task task) {
       
        State state = task.getProgressStateAndAddNotify();

        FtpManager manager = MftpManager.getInstants();
        //获取ftp客户端
        FtpClientIntface client = manager.getClient(task.getFtpInfo());
        if (client==null) {
            state.setError(State.ErrorCode.ERROR_BY_FTP_CLIENT,"FTP客户端获取失败.无法下载文件");
            state.setState(-1);
            state.setRecord(true);
            finish(task);
            return null;
        }
        String remote = TaskUtils.getRemoteFile(task);
        String local = TaskUtils.getLocalFile(task);//本地路径
        String tmp = TaskUtils.getTmpFile(task); // 临时文件路径

        long length = client.getFtpFileSize(remote);
        //获取文件大小
        state.setTotalSize(length);

        if (state.getTotalSize() == 0){

            state.setError(State.ErrorCode.ERROR_BY_FILE_NO_EXIST,"FTP客户端找不到远程文件:"+remote);
            state.setState(-1);
            state.setRecord(true);
            finish(task);
            manager.backClienOnError(client);
            return null;
        }

        //查看本地是否存在文件 判断是否覆盖下载 true 继续下载 false 本地存在 不覆盖
        if (!TaskUtils.judgeCover(task)){
            finish(task);
            manager.backClient(client);
            return null;
        }
        //查看临时文件
        File tmpFile = new File(tmp);
        long startPoint = 0;
        if (tmpFile.exists()){
            startPoint = tmpFile.length();
        }else{
            try {
                tmpFile.createNewFile();//创建临时文件
            } catch (IOException e) {
                state.setError(State.ErrorCode.ERROR_BY_FILE_CREATE_FAIL,"本地创建文件失败:"+tmp);
                state.setState(-1);
                state.setRecord(true);
                finish(task);
                manager.backClient(client);
                return null;
            }
        }

        if (startPoint == state.getTotalSize()){
            //改名字
            TaskUtils.renameTo(task);
            state.setCurrentSize(state.getTotalSize());
            state.setState(1);
            state.setRecord(true);
            finish(task);
            manager.backClient(client);
            return null;
        }
        if (startPoint>state.getTotalSize()){
            //删除临时文件
            startPoint = 0;
            FileUtil.deleteFile(tmp);
            //创建
            try {
                tmpFile.createNewFile();//创建临时文件
            } catch (IOException e) {}
        }

        state.setRecord(true); //开始记录
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmpFile);

            //开始下载
            client.downloadFile(remote,out,startPoint, new FTPDataTransferListener() {
                @Override
                public void started() {
                    //开始
                    Log.e("FTP开始下载: "+remote);
                }
                @Override
                public void transferred(int length) {
                    //传输中
                    state.setCurrentSize(state.getCurrentSize()+length);
                }
                @Override
                public void completed() {
                    state.setCurrentSize(state.getTotalSize());
                    state.setState(1);
                }
                @Override
                public void aborted() {
                    //中断
                    state.setError(State.ErrorCode.ERROR_BY_FTP_CLIENT,"FTP中断异常");
                    state.setState(-1);
                }

                @Override
                public void failed() {
                    //失败
                    state.setError(State.ErrorCode.ERROR_BY_FTP_CLIENT,"FTP下载失败");
                    state.setState(-1);
                }
            }, task.getDownloadLimitMax()
            );
        } catch (FileNotFoundException e) {
            state.setError(State.ErrorCode.ERROR_BY_TRANSLATE,e.getMessage());
            state.setState(-1);
        }finally {
            FileUtil.closeStream(null,out,null,null);
            finish(task);//结束任务
            manager.backClient(client);//返回客户端
        }
        return null;
    }




    @Override
    public Task upload(Task task) {
        return null;
    }
}
