package bottle.backup.client;

import bottle.ftc.tools.Log;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/11/24.
 */
class FBCThreadBySocketList extends FBCThread {

    private final long _TIME = 60 *1000L;
    private final int max;
    private final ArrayList<FileUpClientSocket> list;
    private final ReentrantLock lock =  new ReentrantLock();
    public FBCThreadBySocketList(FtcBackupClient ftcBackupClient,int max) {
        super(ftcBackupClient);
        this.max = max;
        list = new ArrayList<>(max);
    }

    @Override
    public void run() {
        //检测队列中的socket连接, 使用时间>30秒, 移除连接
        while (isRunning){
            try {
                synchronized (this){
                    this.wait(_TIME);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                checkSocketIdle();
            }
        }
    }




    //获取 最快一次的时间
    public void checkSocketIdle() {
        try{
            lock.lock();
            boolean isNotify = false;
            FileUpClientSocket socket;
            Iterator<FileUpClientSocket> iterator = list.iterator();
            while (iterator.hasNext()){
                socket = iterator.next();
                //未使用并且闲置时间 > 300秒. 停止连接并移除.
                if ( !socket.isUsing() &&  socket.isIdle(_TIME)){
                    if (socket.isConnected()) {
                        socket.close();
//                        Log.i(socket.getFlag(),"空闲,关闭连接并移除");
                    }
                    iterator.remove();
                    isNotify = true;
                }else if ( socket.isIdle(_TIME * 5)){
                    socket.close();
                    Log.e(socket.getFlag(),"超时,关闭连接并移除");
                    iterator.remove();
                    isNotify = true;
                }
            }
            if (isNotify) ftcBackupClient.unLockBindSocket();
        }finally {
            lock.unlock();
        }
    }

    public FileUpClientSocket getSocket(InetSocketAddress serverAddress) throws Exception{
        try{
            lock.lock();
            FileUpClientSocket socket;
            if (list.size()>0){
                Iterator<FileUpClientSocket> iterator = list.iterator();
                while (iterator.hasNext()){
                    socket = iterator.next();
                    //未使用
                    if (socket.validServerAddress(serverAddress) && !socket.isUsing() && socket.isConnected()){
                        return socket;
                    }
                }
            }
            if (list.size()<max){
                    //在最大连接限制内 , 创建sok连接
                    socket = new FileUpClientSocket(this,serverAddress);
                    list.add(socket);
                    return socket;
            }else{
                throw new IllegalStateException("current max connected number");
            }
        }finally {
            lock.unlock();
        }
    }

    public int getSocketLimit() {
        return max;
    }

    public int getCurrentSize() {
        return list.size();
    }

    protected void notifyCheckSocketIdle(){
        try {
//            Log.i("连接完成 - 通知");
            synchronized (this){
                notify();
            }
        } catch (Exception e) {
        }
    }
}
