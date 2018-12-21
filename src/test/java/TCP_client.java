import bottle.backup.client.FtcBackupClient;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_client {

    public static void main(String[] args) throws IOException {

        FtcBackupClient client  = new FtcBackupClient("D:\\ftcServer\\c",
                64,
                2000);

        client.addServerAddress(new InetSocketAddress("192.168.1.144",7777));
        client.ergodicDirectory();
//        client.watchDirectory(true);
//        String  json = "['10:50:20']";
//        String  json = "['10:50:20','10:55:00','10:59:00','11:03:00']";
//        client.setTime(json);

        try {
            Thread.sleep(1000 * 60 * 60 * 24);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
