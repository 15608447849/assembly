import com.m.backup.client.FtcBackupClient;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_client {

    public static void main(String[] args) throws IOException {

        FtcBackupClient client  = new FtcBackupClient("C:\\FileServerDirs\\tests\\DIR_SRC",
                new InetSocketAddress("172.16.0.201",7777),
                5,
                2000);
//        new Thread(()->{
//            File file = new File("C:\\FileServerDirs\\tests\\DIR_SRC\\ABC\\lzp");
//            client.addBackupFile(file);//添加一个同步文件
//        }).start();
//        client.ergodicDirectory();

//        String  json = "['2017-11-28 10:23:20']";
//        client.setTime(json);

        client.watchDirectory(true);
    }
}
