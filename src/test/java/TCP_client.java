import com.m.backup.client.FtcBackupClient;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_client {

    public static void main(String[] args) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress("172.16.0.203",7777);
        InetSocketAddress socketAddress2 = new InetSocketAddress("172.16.0.209",7777);

        FtcBackupClient client  = new FtcBackupClient("C:\\ftcServer\\resource\\defaults\\file",
                2,
                2000);
//        client.setServerAddress();
        new Thread(()->{
            File file = new File("C:\\ftcServer\\resource\\defaults\\file\\x005.jpg");
            try {
                client.addBackupFile(file,socketAddress);//添加一个同步文件
            } catch (IOException e) {
                e.printStackTrace();
            }
            file = new File("C:\\ftcServer\\resource\\defaults\\file\\x006.jpg");
            try {
                client.addBackupFile(file,socketAddress2);//添加一个同步文件
            } catch (IOException e) {
                e.printStackTrace();
            }
            file = new File("C:\\ftcServer\\resource\\defaults\\file\\x001.jpg");
            try {
                client.addBackupFile(file,socketAddress);//添加一个同步文件
            } catch (IOException e) {
                e.printStackTrace();
            }
            file = new File("C:\\ftcServer\\resource\\defaults\\file\\x002.jpg");
            try {
                client.addBackupFile(file,socketAddress2);//添加一个同步文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
//        client.ergodicDirectory();

//        String  json = "['2017-11-28 10:23:20']";
//        client.setTime(json);

        while (true);

    }
}
