package m.backup;

import com.winone.ftc.mtools.Log;

import m.backup.backup.server.FtcBackupServer;
import m.tcps.p.Session;
import m.tcps.p.SocketImp;
import m.tcps.s.FtcSocketServer;
import m.tcps.p.FtcTcpActionsAdapter;
import m.tcps.s.FtcTcpServerActions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_server {
    public static void main(String[] args) throws IOException {
        FtcBackupServer ftcBackupServer = new FtcBackupServer("D:\\backup\\B",new InetSocketAddress("127.0.0.1",5200));
        while (true);
    }
}
