package com.winone.ftc.mftp.imps;

import com.winone.ftc.mcore.itface.Excute;
import com.winone.ftc.mentity.itface.Mftcs;
import com.winone.ftc.mentity.mbean.entity.State;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.TaskUtils;
import com.winone.ftc.mftp.ftps.MftpManager;
import com.winone.ftc.mftp.itface.FtpClientIntface;
import com.winone.ftc.mftp.itface.FtpManager;
import com.winone.ftc.mtools.Log;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

/**
 * Created by lzp on 2017/5/12.
 * ftp上传
 *
 */
public class FtpUpdateImp extends Excute {
    public FtpUpdateImp(Mftcs manager) {
        super(manager);
    }

    @Override
    public Task load(Task task) {
        return null;
    }

    @Override
    public Task upload(Task task) {
        //创建状态
        State state = task.getProgressStateAndAddNotify();

        //获取ftp客户端
        FtpManager manager = MftpManager.getInstants();
        FtpClientIntface client = manager.getClient(task.getFtpInfo());
        if (client==null){
            state.setError("FTP客户端获取失败.上传文件失败");
            state.setState(-1);
            state.setRecord(true);
            finish(task);
            return null;
        }

        //获取本地任务
        String remote = task.getRemotePath();//远程目录
        String remoteFileName = task.getRemoteFileName();
        String local = TaskUtils.getLocalFile(task);//本地文件
        File localFile = new File(local);
        if (!localFile.exists()){
            //本地文件不存在
            state.setError("本地文件不存在:"+local);
            state.setState(-1);
            state.setRecord(true);
            finish(task);
            manager.backClient(client);
            return null;
        }
        state.setTotalSize(localFile.length());
        state.setRecord(true);

        //开始上传
        client.uploadFile(remote, remoteFileName,localFile, new FTPDataTransferListener() {
            long time;
            @Override
            public void started() {
                Log.e("时间","开始上传时间:"+ (time = System.currentTimeMillis()) );
            }

            @Override
            public void transferred(int length) {
                state.setCurrentSize(state.getCurrentSize() + length);
            }

            @Override
            public void completed() {
                Log.e("时间","上传成功耗时:"+  (System.currentTimeMillis() - time) );
                state.setState(1);
                finish(task);
                manager.backClient(client);
            }

            @Override
            public void aborted() {
                state.setError("FTP中断异常");
                state.setState(1);
                finish(task);
                manager.backClienOnError(client);
            }

            @Override
            public void failed() {
                state.setError("FTP上传失败");
                state.setState(1);
                finish(task);
                manager.backClienOnError(client);
            }
        });

        return null;
    }
}
