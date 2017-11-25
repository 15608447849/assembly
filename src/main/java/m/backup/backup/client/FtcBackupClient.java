package m.backup.backup.client;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;
import m.backup.backup.imps.FtcBackAbs;
import m.backup.backup.beans.BackupFileInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/11/23.
 * 文件同步客户端
 */
public class FtcBackupClient extends FtcBackAbs {

    private final InetSocketAddress socketServerAddress;

    private final FBCThreadBySocketList socketList;

    private final FBCThreadByFileQueue fileQueue;

    private final FileVisitor fileVisitor;
    public FtcBackupClient(String directory, InetSocketAddress socketServerAddress,int socketMax) {
        //服务器地址, 同时传输最大数量
        super(directory);
        this.socketServerAddress = socketServerAddress;
        this.socketList = new FBCThreadBySocketList(this,socketMax);
        this.fileQueue = new FBCThreadByFileQueue(this);
        this.fileVisitor = new FileVisitor(this);

    }

    protected FileUpdateSocketClient getSocketClient() throws InterruptedException{
        FileUpdateSocketClient socketClient=null;

        while (true){
            socketClient = socketList.get();
            if (socketClient!=null && socketClient.isConnected()){
                //成功连接的
                break;
            }
            Thread.sleep(1000); //休眠1秒 ,直到拿到为止
        }
        return socketClient;
    }


    public InetSocketAddress getSocketServerAddress() {
        return socketServerAddress;
    }

    public boolean addBackupFile(File file) throws IOException {
        Log.println(file);
        String path  = FileUtil.replaceFileSeparatorAndCheck(file.getCanonicalPath(),null,null);
        if (path.contains(directory)){
           path = path.substring(directory.length());
           BackupFileInfo fileInfo = new BackupFileInfo(directory,path);
           return fileQueue.putFileInfo(fileInfo);
        }
        return false;
    }


    public void ergodicDirectory(){
        this.fileVisitor.startVisitor();
    }

}
