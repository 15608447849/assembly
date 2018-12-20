package bottle.ftc.ftp.itface;

import bottle.ftc.entity.mbean.entity.Task;

/**
 * Created by lzp on 2017/5/11.
 * ftp 管理器
 */
public interface FtpManager {
    //获取客户端
    FtpClientIntface getClient(Task task);
    //返回客户端
    void backClient(FtpClientIntface client);
    void backClienOnError(FtpClientIntface client);
}
