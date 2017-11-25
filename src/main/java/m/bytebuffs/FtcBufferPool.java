package m.bytebuffs;

import m.tcps.p.Protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/7/10.
 */
public class FtcBufferPool extends Thread{
    private final int CHECK_TIME = 10 * 1000;
    private final ReentrantLock lock = new ReentrantLock();
    private FtcBufferPool(){
        start();
    }



    private static class Holder{
        private static FtcBufferPool pool = new FtcBufferPool();
    }
    public static FtcBufferPool get(){
        return Holder.pool;

    }

    /**
     * buf 池
     */
    private final ArrayList<FtcBuffer> tempList = new ArrayList<>();

    private final ArrayList<FtcBuffer> onlyList = new ArrayList<>();

    @Override
    public void run() {
      while (true){
          try {
              Thread.sleep(CHECK_TIME);
          } catch (InterruptedException e) {
          }
          checkBufferList();
      }
    }

    /**
     * 检测超时
     */
    private void checkBufferList() {
        try {
            lock.lock();
            Iterator<FtcBuffer> iterator = tempList.iterator();//临时队列
            FtcBuffer buffer;
            while (iterator.hasNext()){
                buffer = iterator.next();
                if (buffer.isNotClearPower()){//是否被允许清理
                    iterator.remove();
                    onlyList.add(buffer);//添加到永存列表
                }else if (buffer.isOutOfDate()){//已经长时间未使用
                    iterator.remove();//移除
                    buffer.clear();//清理
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public FtcBuffer getBuffer(int length){
            try{
                lock.lock();
                FtcBuffer buffer = null;
                Iterator<FtcBuffer> iterator = tempList.iterator();//查看临时队列中
                while (iterator.hasNext()){
                    buffer = iterator.next();
                    if (buffer.getLength() == length && buffer.isOutOfDate()){//如果容量相同,并且已经超时
                        iterator.remove();
                    }
                }
                if (buffer==null){
                    buffer = new FtcBuffer(length);//新建一个buffer
                }else{
                    buffer.clearBuf();//从池中获取到一个可以使用的, 清理.
                }
                tempList.add(buffer);//再次添加到临时队列中
                return buffer;
            }finally {
                lock.unlock();
            }
    }

    /**
     * 清理buffer
     * @param buffer
     */
    public void clear(FtcBuffer buffer) {

        try {
            if (buffer.isNotClearPower()){
                onlyList.remove(buffer);
            }
        } finally {

        }
    }
}
