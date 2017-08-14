package m.bytebuffs;

import com.winone.ftc.mtools.Log;
import m.tcps.p.Protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/7/10.
 */
public class MyBufferPool extends Thread{
    private final int CHECK_TIME = 10 * 1000;
    private final ReentrantLock lock = new ReentrantLock();
    private MyBufferPool(){
        start();
    }



    private static class Holder{
        private static MyBufferPool pool = new MyBufferPool();
    }
    public static MyBufferPool get(){
        return Holder.pool;

    }

    /**
     * buf 池
     */
    private final ArrayList<MyBuffer> tempList = new ArrayList<>();

    private final ArrayList<MyBuffer> onlyList = new ArrayList<>();

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
            Iterator<MyBuffer> iterator = tempList.iterator();
            MyBuffer buffer = null;
            while (iterator.hasNext()){
                buffer = iterator.next();
                if (buffer.isNotClearPower()){
                    iterator.remove();
                    onlyList.add(buffer);
                }else if (buffer.isOutOfDate()){
                    iterator.remove();
                    buffer.clear();
                }
            }
//            Log.i("TEMP BUFFER POLL SIZE: "+ tempList.size());
//            Log.i("ONLY BUFFER POLL SIZE: "+ onlyList.size());
        } finally {
            lock.unlock();
        }
    }
    /**
     * 获取一个buffer,没有创建
     */
    public MyBuffer getBuffer(){
        return getBuffer(Protocol.MTU);
    }
    public MyBuffer getBuffer(int length){
            try{
                lock.lock();
                MyBuffer buffer = null;
                Iterator<MyBuffer> iterator = tempList.iterator();
                while (iterator.hasNext()){
                    buffer = iterator.next();
                    if (buffer.getLength() == length && buffer.isOutOfDate()){
                        iterator.remove();
                        break;
                    }
                    buffer = null;
                }
                if (buffer==null){
                    buffer = new MyBuffer(length);
                }
                tempList.add(buffer);
                return buffer;
            }finally {
                lock.unlock();
            }
    }

    /**
     * 清理buffer
     * @param buffer
     */
    public void clear(MyBuffer buffer) {

        try {
            if (buffer.isNotClearPower()){
                onlyList.remove(buffer);
            }
        } finally {

        }
    }
}
