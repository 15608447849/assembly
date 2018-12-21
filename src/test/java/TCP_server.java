import bottle.backup.server.Callback;
import bottle.backup.server.FtcBackupServer;
import bottle.ftc.tools.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_server {
    public static void main(String[] args) throws IOException {
        FtcBackupServer ftcBackupServer = new FtcBackupServer("D:\\ftcServer\\s","192.168.1.144",7777,0,0);
//        ftcBackupServer.getClient().watchDirectory(true); //监听目录变化
//        ftcBackupServer.getClient().addServerAddress();
        ftcBackupServer.setCallback(file -> Log.e("收到文件 - "+ file));
        try {
            Thread.sleep(100000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
