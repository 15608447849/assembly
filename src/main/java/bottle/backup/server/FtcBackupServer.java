package bottle.backup.server;

import bottle.backup.client.FtcBackupClient;
import bottle.backup.imps.FtcBackAbs;
import bottle.ftc.tools.IOThreadPool;
import bottle.tcps.s.FtcSocketServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/11/23.
 * 文件同步 - 服务端
 */
public class FtcBackupServer extends FtcBackAbs implements Callback {
    //本地socket 地址
    private final FtcSocketServer sockSer;
    private final FtcBackupClient client;
    private final FtcTcpServerActions ftcTcpServerActions = new FtcTcpServerActions(this);
    private IOThreadPool pool = new IOThreadPool();
    private final ReentrantLock lock = new ReentrantLock();
    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    public FtcBackupServer(String directory, String ip, int port, int socketMax, int fileUoListLimit) throws IOException {
        super(directory);
        //socket 服务端
        sockSer = new FtcSocketServer(new InetSocketAddress(ip,port),ftcTcpServerActions);
        //启动服务
        sockSer.openListener().launchAccept();
        //创建一个同步功能的客户端
        client = new FtcBackupClient(directory,socketMax,fileUoListLimit);
    }

    public FtcSocketServer getSockSer() {
        return sockSer;
    }

    public FtcBackupClient getClient() {
        return client;
    }

    @Override
    public void complete(File file) {
        if (callback==null) return;
        pool.post(() -> {
        try{
            lock.lock();
            callback.complete(file);
        }catch (Exception e){
           e.printStackTrace();
        }finally {
            lock.unlock();
        }
        });
    }


}
