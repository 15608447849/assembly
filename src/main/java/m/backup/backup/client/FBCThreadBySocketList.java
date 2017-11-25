package m.backup.backup.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/11/24.
 */
class FBCThreadBySocketList extends FBCThread {

    private int cId = 0;
    private final int max;
    private final ArrayList<FileUpdateSocketClient> list;
    private final ReentrantLock lock = new ReentrantLock();
    public FBCThreadBySocketList(FtcBackupClient ftcBackupClient,int max) {
        super(ftcBackupClient);
        this.max = max;
        list = new ArrayList<>(max);
    }

    @Override
    public void run() {
        //间隔三十秒 ,检测队列中的socket连接, 使用时间>30秒, 移除连接
        while (isRunning){
            try {
                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            check();
        }
    }

    private void check() {
        try{
            lock.lock();
            if (list!=null && list.size()>0){
                FileUpdateSocketClient socket;
                Iterator<FileUpdateSocketClient> iterator = list.iterator();
                while (iterator.hasNext()){
                    socket = iterator.next();
                    //未使用并且闲置时间 >60秒. 停止连接并移除.
                    if ( !socket.isUsing() && socket.isIdle(60*1000)){
                        socket.close();
                        iterator.remove();
                    }
                }
            }

        }finally {
            lock.unlock();
        }
    }

    public FileUpdateSocketClient get() {
        try{
            lock.lock();
            FileUpdateSocketClient socket;
            if (list.size()<max){
                socket = new FileUpdateSocketClient(cId,ftcBackupClient.getSocketServerAddress());
                list.add(socket);
                cId++;
                return socket;
            }
            Iterator<FileUpdateSocketClient> iterator = list.iterator();
            while (iterator.hasNext()){
                socket = iterator.next();
                //未使用,并且连接成功
                if ( !socket.isUsing()){
                    return socket;
                }
            }
            return null;
        }finally {
            lock.unlock();
        }
    }
}
