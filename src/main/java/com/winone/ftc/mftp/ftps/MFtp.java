package com.winone.ftc.mftp.ftps;

import com.winone.ftc.mentity.mbean.entity.FtpInfo;
import com.winone.ftc.mftp.itface.FtpClientIntface;
import com.winone.ftc.mtools.Log;
import it.sauronsoftware.ftp4j.*;

import java.io.*;

/**
 * Created by lzp on 2017/5/11.
 * ftp客户端程序
 */
public class MFtp extends it.sauronsoftware.ftp4j.FTPClient implements FtpClientIntface {

    private static final String TAG = "FTP客户端";
    private FtpInfo info;
    private int state = 0;//0未使用  1使用中 2不可用
    public MFtp(FtpInfo info) {
        this.setFtpInfo(info);
        this.setPassive(true);
        this.setType(FTPClient.TYPE_BINARY);
    }

    @Override
    public void setFtpInfo(FtpInfo ftpUser) {
        if (ftpUser == null) throw new IllegalStateException("ftp信息错误");
        this.info = ftpUser;
    }

    @Override
    public boolean check(FtpInfo ftpUser) {
        return this.info.equals(ftpUser);
    }

    @Override
    public boolean connect() {
        if (isConnected()) return true;
        try {
           String [] welcomeInfo = connect(info.getHost(),info.getPort());
            for (String str : welcomeInfo){
                Log.i(TAG,str);
            }
            Log.i(TAG,"连接> "+ getHost()+ ":"+ getPort());
        return true;
        } catch (IOException | FTPException | FTPIllegalReplyException e) {

        }
        return false;
    }

    @Override
    public boolean login() {
        if (isAuthenticated()) return true;
        try {
            login(info.getUserName(),info.getPassword());
            setAutoNoopTimeout(20 * 1000);
            if (isCompressionSupported()){
                setCompressionEnabled(true);
            }
            Log.i(TAG,"登录> "+ getUsername()+ " - "+ getPassword());
            return true;
        } catch (IOException | FTPException | FTPIllegalReplyException e) {

        }
        return false;
    }

    @Override
    public void logout() {
        if (isAuthenticated()){
            try {
                super.logout();
            } catch (IOException | FTPException | FTPIllegalReplyException e) {

            }
            Log.i(TAG,"登出> "+ getUsername()+ " - "+ getPassword());
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()){
            try {
                super.disconnect(true);
            } catch (IOException | FTPException | FTPIllegalReplyException e) {

            }finally {
                try {
                    super.disconnect(false);
                } catch (Exception e1) {
                }
            }
            Log.i(TAG,"断开连接> "+ getHost()+ ":"+ getPort());
        }
    }

    @Override
    public void inUse() {
        this.state = 1;
    }

    @Override
    public void notUsed() {
        this.state = 0;
    }

    @Override
    public void isError() {
        this.state = 2;
    }

    @Override
    public boolean isUsable() {
        return state==0; //可用
    }

    @Override
    public boolean isNotUsed() {
        return state==0 || state == 2; //未使用或者错误
    }

    @Override
    public long getFtpFileSize(String ftpAbsulutePath) {
        long size = 0;
        try {
           size =  fileSize(ftpAbsulutePath);
        } catch (IOException | FTPException | FTPIllegalReplyException e) {

            size = 0;
        }
        return size;
    }

    @Override
    public void downloadFile(String remotePath, FileOutputStream out, long startPoint, FTPDataTransferListener listener,long downLimit) {
        try {
            download(remotePath,out,startPoint,listener,downLimit);
        } catch (IOException | FTPIllegalReplyException | FTPException | FTPAbortedException | FTPDataTransferException e) {
            listener.failed();
        }
    }

    @Override
    public void uploadFile(String remoteDir,String remoteFileName,File localFile,FTPDataTransferListener listener) {
        if (remoteDir!=null){

            try {
                //创建目录
                createDirectory(remoteDir);
            } catch (IOException | FTPIllegalReplyException  | FTPException  e) {
            }
            try {
                //改变当前目录
                changeDirectory(remoteDir);
            } catch (IOException | FTPIllegalReplyException  | FTPException  e) {
            }

            try {
                //改变当前目录
                changeDirectory(remoteDir);

                if (!localFile.exists()) {
                    Log.e("FTP 上传错误: "+localFile.getAbsolutePath()+"不存在");
                    listener.aborted();
                }
                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(localFile);
                    if (remoteFileName==null || remoteFileName.equals("")) remoteFileName = localFile.getName();
                    upload(remoteFileName,inputStream,0,0,listener);
                } catch (IOException e) {
                    Log.e("FTP 上传错误: "+ e.toString());
                    listener.aborted();
                }finally {
                    if (inputStream!=null){
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                        }
                    }
                }

            } catch (IOException | FTPIllegalReplyException | FTPDataTransferException | FTPException | FTPAbortedException e) {
                listener.failed();
            }
        }
    }

    @Override
    public String toString() {
        return "["+hashCode()+"]";
    }
}
