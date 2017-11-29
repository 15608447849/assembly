import com.m.backup.server.FtcBackupServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_server {
    public static void main(String[] args) throws IOException {
        FtcBackupServer ftcBackupServer = new FtcBackupServer("C:\\FileServerDirs\\tests\\DIR_DES",new InetSocketAddress("127.0.0.1",5200));
        while (true);
    }
}
