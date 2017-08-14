package m.lanshare.threads;

import m.lanshare.beads.PThread;
import m.lanshare.beads.Prog;
import m.lanshare.beads.TranslateServer;
import com.winone.ftc.mtools.Log;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.DatagramChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by user on 2017/6/20.
 */
public class ServerDataTranslate extends PThread implements CompletionHandler<Integer,ByteBuffer> {
    private TranslateServer trans;
    private DatagramChannel dataChannel;
    private ArrayList sendSuccessSliceList = new ArrayList();

    public ServerDataTranslate(TranslateServer trans) {
        this.trans = trans;
        this.dataChannel = this.trans.dataChannel;
        start();
    }

    public void notifySuccessSlice(int index){

        try {
            lock.lock();
            sendSuccessSliceList.add(index);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        Log.i("数据传输 - "+this+" 准备...");
        AsynchronousFileChannel fileChannel = null;
        try {
            fileChannel = AsynchronousFileChannel.open(trans.filePath, StandardOpenOption.READ);
            resetTime();
            state = 1; // 1 等待接入, 2 传输中 3 结束
            while (state!=3){
                if (state == 1){
                   waitAccess();
                }
                if (state == 2){
                    sendData(fileChannel);
                }
            }
            Log.e("数据传输结束.");
        } catch (Exception e) {
            ;
        }finally {
            closeFileChannel(fileChannel);
            //关闭管道.
            closeChannal(dataChannel,trans.manage);
        }
    }

    //等待接入
    private void waitAccess() {
        Log.i("等待客户端信息接入中 ..... ");
        if (!dataChannel.isOpen()){
            state = 13;
        }
        else{
            SocketAddress address;
            ByteBuffer buffer = ByteBuffer.allocate(1);
            while (state == 1 && isNotOuttime()){
                buffer.clear();
                address = receive(dataChannel,buffer);
                if (address!=null){
                    buffer.flip();
                    if (buffer.get(0) == Prog.dataAccess){
                        trans.clientDataAddress = address;
                        state = 2;
                        Log.i("数据传输客户端接入 - "+ address);
                    }
                    if (buffer.get(0) == Prog.dataComplete){
                        state = 3;
                        Log.i("数据传输成功 - "+ address);
                    }
                }
                if (!isNotOuttime()){
                    if (overTimeCount>OVER_MAX){
                        Log.e("等待客户端信息超时.");
                        state = 13;
                    }else{
                        resetTime();
                        overTimeCount++;
                    }
                }
            }



        }

    }



    private void sendData(AsynchronousFileChannel fileChannel) {
        Log.i("发送数据到客户端... ");
        if (fileChannel==null || !fileChannel.isOpen() || !dataChannel.isOpen()){
            state = 3;
        }else{

            try {
                lock.lock();
                if (sendSuccessSliceList.size() > 0) {
                    Iterator<Integer> itr = sendSuccessSliceList.iterator();
                    while (itr.hasNext()) {
                        trans.dataSliceMap.remove(itr.next());
                        itr.remove();
                    }
                }
            }finally {
                    lock.unlock();
            }

            Log.i("当前可发送分片数量: "+trans.dataSliceMap.size());
            ByteBuffer sendBuf = null;
            long pos;
            long length = trans.dataLength+trans.startPos;
            int value;
            for (Integer count:trans.dataSliceMap.keySet()){
                pos = trans.dataSliceMap.get(count);
                value = (int) (length - pos);
                if (value>=(trans.mtu-4)){
                    sendBuf = ByteBuffer.allocate(trans.mtu);
                }else{
                    sendBuf = ByteBuffer.allocate(4+value);
                }

                sendBuf.clear();
                sendBuf.putInt(count);
                fileChannel.read(sendBuf,pos,sendBuf,this);
                waitTime2();
            }

            sendBuf = ByteBuffer.allocate(4);
            //发送完成标识
            sendBuf.clear();
            sendBuf.putInt(-1);
            fileChannel.read(sendBuf,length,sendBuf,this);
            state = 1;
            resetTime();
        }
    }

    @Override
    public void completed(Integer integer, ByteBuffer buffer) {
        buffer.flip();
        //发送.
        send(dataChannel,buffer,trans.clientDataAddress);
    }

    @Override
    public void failed(Throwable throwable, ByteBuffer buffer) {
        throwable.printStackTrace();
    }
}
