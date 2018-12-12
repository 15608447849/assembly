package com.m.backup.server;

import com.m.backup.client.FtcBackupClient;
import com.m.tcps.p.Session;
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
    private final FtcBackupClient client;
    private final  FtcTcpServerActions ftcTcpServerActions = new FtcTcpServerActions(this);

    public FtcBackupServer(String directory, InetSocketAddress socketServerAddress) throws IOException {
        super(directory);
        //socket 服务端
        sockSer = new FtcSocketServer(socketServerAddress,ftcTcpServerActions);
        //启动服务
        sockSer.openListener().launchAccept();
        //创建一个同步功能的客户端
        client = new FtcBackupClient(directory,100,10000);
    }

    public FtcSocketServer getSockSer() {
        return sockSer;
    }

    public FtcBackupClient getClient() {
        return client;
    }
}
