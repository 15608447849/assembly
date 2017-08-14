package m.lanshare.threads;

import m.lanshare.LANManage;
import m.lanshare.beads.DataSource;
import m.lanshare.beads.Prog;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Created by user on 2017/6/19.
 */
public class LANThread extends Thread{
    public static final int UDP_DATA_MIN_BUFFER_ZONE = 576-20-8;// intenet标准MTU - IP头 -UDP头
    private LANManage manager;

    public LANThread(LANManage manager) {
        this.manager = manager;
        this.start();
    }

    @Override
    public void run() {
        Selector selector = manager.getSelector();
        Iterator iterator;
        SelectionKey key = null;
        try {

            while (selector.isOpen()){
                if (selector.select()==0) continue;
                iterator = selector.selectedKeys().iterator();
                key = (SelectionKey) iterator.next();
                iterator.remove();
                try {
                    if (key.isReadable()){
                        handleChannel(key);
                    }
                } catch (Exception e) {
                    ;
                }
            }
        } catch (IOException e) {
            ;
        }
    }

    private void handleChannel(SelectionKey key) throws Exception {
        //处理接受的消息
        DatagramChannel channel = (DatagramChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(UDP_DATA_MIN_BUFFER_ZONE);
        buffer.clear();
        SocketAddress address = channel.receive(buffer);
        if (address.equals(manager.getChannelMutics().getLocalAddress())) return; //自己发的广播自己就不要接了.
        buffer.flip();
        Log.i(address+" -> "+ buffer);
        byte command = buffer.get();
        Log.i("command: "+ command);
        if (command == -1){
            Log.e("服务器错误信息: "+ new String (buffer.array()));
        }
        //20 服务器返回的文件md5字节码
        if (command == 20){
            Log.i("文件返回值信息:");
            byte[] fileMd5 = new byte[16];//文件MD5
            buffer.get(fileMd5);
            long fileSize = buffer.getLong();
            byte[] filePath = new byte[buffer.remaining()];
            buffer.get(filePath);
            String filePathStr = new String(filePath,"UTF-8");
            Log.i("文件路径: "+ filePathStr+" 文件MD5:"+ MD5Util.bytesGetMD5String(fileMd5)+" 文件大小:"+fileSize);
            //开始一个局域网广播,收集局域网是否存在文件.
            new UDPFileTaskThread(fileMd5,fileSize,filePathStr,manager);
        }
        if (command == 21){
            //其他客户端发来的组播信息
            Log.i("组播信息:");
            byte[] fileMd5 = new byte[16];
            buffer.get(fileMd5);
            long fileSize = buffer.getLong();
            Log.i("本地查询MD5: "+ MD5Util.bytesGetMD5String(fileMd5)+" ,文件大小:"+fileSize);
            new FileQueryThread(fileMd5,fileSize,address,manager);
        }
        if (command == 30){
            Log.i("收到一个文件源:");
            byte[] fileMd5 = new byte[16];
            buffer.get(fileMd5);
            byte[] filePathBytes = new byte[buffer.remaining()];
            buffer.get(filePathBytes);
            Log.i("[ "+address+" ] 存在文件,存储路径: "+ new String(filePathBytes,"UTF-8"));
            //查询任务,设置值
            UDPFileTaskThread task = manager.getFileTask(fileMd5);
            if (task!=null){
                task.putSource(new DataSource(address,filePathBytes));
            }
        }
        if (command == Prog.notifyServerCreateConnect){//客户端通知服务端打开数据传输的端口
            Log.i("收到一个传输请求:");
            byte[] fileMd5 = new byte[16];
            buffer.get(fileMd5);
            Log.i("[ "+address+" ] 任务名: "+ MD5Util.bytesGetMD5String(fileMd5));

            new ServerCommunication(address,fileMd5,channel,manager);
        }






    }
}
