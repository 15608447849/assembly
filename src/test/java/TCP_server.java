import com.m.backup.server.FtcBackupServer;
import com.winone.ftc.mtools.Log;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_server {
    public static void main(String[] args) throws IOException {
        FtcBackupServer ftcBackupServer = new FtcBackupServer(
                "C:\\ftcServer",
                new InetSocketAddress("192.168.1.45",7777));
//        while (true);
        Log.println("- - - - - - - - - - -");
    }
}
