import com.m.backup.client.FtcBackupClient;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_client {

    public static void main(String[] args) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("172.16.0.201",7777);
        FtcBackupClient client  = new FtcBackupClient("C:\\FileServerDirs\\tests\\DIR_SRC",
                5,
                2000);
//        client.setServerAddress();
        new Thread(()->{
            File file = new File("C:\\FileServerDirs\\tests\\DIR_SRC\\ABC\\lzp");
            try {
                client.addBackupFile(file,socketAddress);//添加一个同步文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
//        client.ergodicDirectory();

//        String  json = "['2017-11-28 10:23:20']";
//        client.setTime(json);

    }
}
