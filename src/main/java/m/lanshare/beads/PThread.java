package m.lanshare.beads;

import m.lanshare.LANManage;
import com.winone.ftc.mtools.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/20.
 */
public class PThread extends Thread{
    protected final ReentrantLock lock = new ReentrantLock();
    protected final int OUT_TIME = 15 * 1000;
    private long currentTime;
    protected int state = 0;
    public static final int OVER_TIME_ONCE = 200;
    public static final int OVER_TIME_ONCE_2 = 98; //单次超时时间 1毫秒(ms)=1 000 000纳秒(ns) 1/10毫秒 1秒10M => 1秒1024*1024*10 -> 1毫秒10240
    protected void resetTime(){
        currentTime = System.currentTimeMillis();
    }
    protected boolean isNotOuttime(){
        return (System.currentTimeMillis() - currentTime)<OUT_TIME;
    }
    protected int overTimeCount = 0;//超时次数
    public static final int OVER_MAX = 10; //超时时间 最大次数
    protected void send(DatagramChannel channel, ByteBuffer buffer, SocketAddress to){
        try {
            channel.send(buffer,to);
        } catch (IOException e) {
            ;
        }
    }
    protected SocketAddress receive(DatagramChannel channel,ByteBuffer buffer){
        SocketAddress address = null;
        try {
            address = channel.receive(buffer);
        } catch (IOException e) {
//            ;
            Log.e(e.getMessage());
        }
        return address;
    }
    protected void waitTime(){
        try {
            TimeUnit.MICROSECONDS.sleep(OVER_TIME_ONCE);
        } catch (InterruptedException e) {
        }
    }
    //数据分片
    protected void dataSlice(TranslateData data) {
        long len = data.dataLength;
        int mtu = data.mtu;
        long startPos = data.startPos;
        int indexBytesLength = 4;
        int sliceUnit = mtu - indexBytesLength;

        data.dataSliceMap = slice(len,sliceUnit,startPos);
    }

    protected HashMap<Integer,Long> slice(long length, int unit, long startPos){
        long remainder = length% unit;
        int count = (int) ((length-remainder)/ unit);
        HashMap<Integer,Long> map = new HashMap<>();
        for (int index = 0; index < count; index++ ){
            map.put(index, ((long)(index* unit))+startPos);
        }
        if (remainder>0){
            map.put(count, length-remainder+startPos);

        }

        return map;
    }
    protected void closeFileChannel(AsynchronousFileChannel fileChannel){
        if (fileChannel!=null && fileChannel.isOpen()){
            try {
                fileChannel.close();
            } catch (IOException e) {
                ;
            }
        }
    }
    protected void waitTime2(){
        try {
            TimeUnit.MICROSECONDS.sleep(OVER_TIME_ONCE_2);
        } catch (InterruptedException e) {
        }
    }
    protected void closeChannal(DatagramChannel channel, LANManage manage){
        if (channel!=null && channel.isOpen()){
            int port = -1;
            try {
                port = ((InetSocketAddress)channel.getLocalAddress()).getPort();
            } catch (IOException e) {
                ;
            }
            try {
                channel.close();
//                Log.i("关闭管道: "+ channel);
            } catch (IOException e) {
                ;
            }finally {
                if (manage!=null){
                        manage.removePort(port);
                }
            }
        }
    }
}
