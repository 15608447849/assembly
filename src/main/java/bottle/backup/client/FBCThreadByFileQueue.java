package bottle.backup.client;

import bottle.backup.beans.BackupFile;
import bottle.backup.beans.BackupTask;
import bottle.ftc.tools.Log;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by user on 2017/11/24.
 */
public class FBCThreadByFileQueue extends FBCThread {

    private final BlockingQueue<BackupTask> queue ;

    public FBCThreadByFileQueue(FtcBackupClient ftcBackupClient,int listSize) {
        super(ftcBackupClient);
        queue = new ArrayBlockingQueue<>(listSize);
    }


    @Override
    public void run() {
        while (isRunning){
            //在队列中查询,如果存在需要同步的文件 - 打开socket连接 -> socket连接在30秒内未使用,需要自动关闭
            try {
                if (queue!=null){
                    BackupTask task = queue.take();
                    ftcBackupClient.bindSocketSyncUpload(task);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public boolean putTask(BackupTask task) {
        try {
            queue.put(task);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
