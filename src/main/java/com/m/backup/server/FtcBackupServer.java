package com.m.backup.server;

import com.winone.ftc.mtools.Log;
import com.m.backup.imps.FtcBackAbs;
import com.m.tcps.p.SocketImp;
import com.m.tcps.s.FtcSocketServer;
import com.m.tcps.s.FtcTcpServerActions;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/23.
 * 文件同步 - 服务端
 */
public class FtcBackupServer extends FtcBackAbs {
    //本地socket 地址
    private final FtcSocketServer sockSer;
    public FtcBackupServer(String directory, InetSocketAddress socketServerAddress) throws IOException {
        super(directory);
        sockSer = new FtcSocketServer(socketServerAddress, new FtcTcpServerActions() {
            @Override
            public void setClientFtcAction(SocketImp socketImp) {

                // 文件同步客户端连接 接入
                socketImp.setAction(new FileUpServerHandle(socketImp,FtcBackupServer.this));
            }
        });
        sockSer.openListener().launchAccept();
    }

    public FtcSocketServer getSockSer() {
        return sockSer;
    }
}
