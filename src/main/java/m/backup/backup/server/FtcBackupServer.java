package m.backup.backup.server;

import com.winone.ftc.mtools.Log;
import m.backup.backup.imps.FtcBackAbs;
import m.tcps.p.SocketImp;
import m.tcps.s.FtcSocketServer;
import m.tcps.s.FtcTcpServerActions;

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
                Log.println("接入连接: "+ socketImp.getInfo(),"- 当前客户端数量: ",sockSer.getCurrentClientSize());
                // 文件同步客户端连接 接入
                socketImp.setAction(new FileUpdateServerHandle(FtcBackupServer.this));
            }
        });
        sockSer.openListener().launchAccept();
    }

    public FtcSocketServer getSockSer() {
        return sockSer;
    }
}
