package m.backup;

import m.backup.backup.client.FtcBackupClient;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/22.
 */
public class TCP_client {

    public static void main(String[] args) throws IOException {

        FtcBackupClient client  = new FtcBackupClient("D:\\backup\\A",new InetSocketAddress("127.0.0.1",5200),1);

//        new Thread(()->{
//            File file = new File("C:\\FileServerDirs\\tests\\DIR_SRC\\ABC\\lzp");
//            client.addBackupFile(file);//添加一个同步文件
//        }).start();

        client.ergodicDirectory();


    }
}
