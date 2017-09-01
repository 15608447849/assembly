package com.winone.ftc.mftp.itface;

import com.winone.ftc.mentity.mbean.entity.FtpInfo;

/**
 * Created by lzp on 2017/5/11.
 * ftp 管理器
 */
public interface FtpManager {
    //获取客户端
    FtpClientIntface getClient(FtpInfo info);
    //返回客户端
    void backClient(FtpClientIntface client);
    void backClienOnError(FtpClientIntface client);
}
