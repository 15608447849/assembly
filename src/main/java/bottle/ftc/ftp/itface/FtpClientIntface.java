package bottle.ftc.ftp.itface;

import bottle.ftc.entity.mbean.entity.FtpInfo;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by lzp on 2017/5/11.
 *
 */
public interface FtpClientIntface {
    void setFtpInfo(FtpInfo ftpUser);//设置ftp信息
    boolean check(FtpInfo ftpUser);//检查是否是指定ftp服务器
    boolean connect();//连接
    boolean login();//登陆
    void logout();//登出
    void disconnect();//断开
    void inUse();
    void notUsed();
    void isError();
    boolean isUsable();//是否可用
    boolean isNotUsed();//未被使用
    long getFtpFileSize(String ftpAbsulutePath);
    void downloadFile(String remotePath, FileOutputStream out, long startPoint, FTPDataTransferListener listener,long downLimit);
    void uploadFile(String remoteDir,String remoteFileName, File localFile, FTPDataTransferListener listener);
}
