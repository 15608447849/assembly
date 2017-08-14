package m.lanshare.threads;

import m.lanshare.FileQuery;
import m.lanshare.LANManage;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/**
 * Created by user on 2017/6/19.
 */
public class FileQueryThread extends Thread{
    private LANManage manage;
    private byte[] fileMd5;
    private long fileSzie;
    private SocketAddress toAddress;
    public FileQueryThread(byte[] fileMd5,long fileSize, SocketAddress address, LANManage manager) {
        this.manage = manager;
        this.fileMd5 = fileMd5;
        this.fileSzie = fileSize;
        this.toAddress = address;
        this.start();
    }

    @Override
    public void run() {
        try {
            Path filePath = new FileQuery(fileMd5,fileSzie).queryFile();
            if (filePath!=null){
                //通知目标 文件源在本地的路径.
                long fileSize = filePath.toFile().length();
                String path = filePath.toFile().getAbsolutePath();
                byte[] pathBytes = path.getBytes("UTF-8");
                ByteBuffer buffer = ByteBuffer.allocate(1+16+pathBytes.length);
                buffer.clear();
                buffer.put((byte)30);
                buffer.put(fileMd5);
                buffer.put(pathBytes);//文件源路径
                buffer.flip();
                manage.getChannelSend().send(buffer,toAddress);
            }
        } catch (IOException e) {
            ;
        }
    }
}
