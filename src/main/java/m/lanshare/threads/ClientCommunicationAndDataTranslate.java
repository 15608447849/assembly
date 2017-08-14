package m.lanshare.threads;

import m.lanshare.beads.PThread;
import m.lanshare.beads.Prog;
import m.lanshare.beads.TranslateClient;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.StandardOpenOption;

/**
 * Created by user on 2017/6/19.
 */
public class ClientCommunicationAndDataTranslate extends PThread  implements CompletionHandler<Integer,Integer>{
    TranslateClient translate;

    public ClientCommunicationAndDataTranslate(TranslateClient translate) {
        this.translate = translate;
        start();
    }

    @Override
    public void run() {
        //通知服务器打开端口
        notifyServer();
        //传递数据信息
        translateDataInfo();
        //等待MTU检测
        waitMTU();
        //数据分片
        if (state == 3){
            dataSlice(translate);
            //开始接受数据.
            receiveData();
        }

        closeChannal(translate.getChannelCommunication(),translate.getLANManager());
        closeChannal(translate.getChannelData(),translate.getLANManager());
        translate.getManager().removeChildTask(translate);
    }

    private void notifyServer() {
//        Log.i("通知服务器打开端口.");
        resetTime();
        ByteBuffer buffer = ByteBuffer.allocate(1+16);
        SocketAddress toAdd ;
        byte command = 0;

        buffer.clear();
        buffer.put(Prog.notifyServerCreateConnect);
        buffer.put(translate.fileAllMd5());
        buffer.flip();
        //发送
        send(translate.getChannelCommunication(),buffer,translate.getServerSocketAddress());

        while (isNotOuttime()){
            buffer.clear();
            //接受
            toAdd = receive(translate.getChannelCommunication(),buffer);
            if (toAdd!=null){
                buffer.flip();
                if (!buffer.hasRemaining()) continue;
                    command = buffer.get();
                    if (command == Prog.notifyServerCreateConnectResp){

                        //记录通讯端口地址
                        InetSocketAddress address = new InetSocketAddress(((InetSocketAddress)toAdd).getAddress(),buffer.getInt());
                        translate.setServerCommunicationSocketaddress(address);
//                        Log.i(this+" 记录服务器 通讯地址: "+ address);
                        //记录数据端口地址
                        address = new InetSocketAddress(((InetSocketAddress)toAdd).getAddress(),buffer.getInt());
                        translate.setServerDataSocketaddress(address);
//                        Log.i(this+" 记录服务器 数据地址: "+ address);

                        state = 1;
                        break;
                    }
            }
        }
    }

    private void translateDataInfo() {
        if (state!=1) return;
        waitTime();
        ByteBuffer buffer = ByteBuffer.allocate(1+8+8+8+translate.filePathBytes.length);
        buffer.clear();
        //发送数据长度,起始位置,结束位置,文件路径字节码. 返回数据片段MD5值
        buffer.put(Prog.sendDataInfo);
        buffer.putLong(translate.dataLength);
        buffer.putLong(translate.startPos);
        buffer.putLong(translate.endPos);
        buffer.put(translate.filePathBytes);
        buffer.flip();
        send(translate.getChannelCommunication(),buffer,translate.getServerCommunicationSocketaddress());
//        Log.i("已发送数据信息. - > "+ translate.getServerCommunicationSocketaddress());

        resetTime();
        SocketAddress address;
        byte command;
        while (isNotOuttime()){

           buffer.clear();
           address = receive(translate.getChannelCommunication(),buffer);
           if (address!=null){
           buffer.flip();
           command = buffer.get();
               if (command == Prog.sendDataInfoResp){
                   byte[] dataMd5 = new byte[16];
                   buffer.get(dataMd5);
                   Log.i("收到服务器 数据片段 MD5: "+ MD5Util.bytesGetMD5String(dataMd5)+" 数据范围: "+ translate.dataLength+" , "+translate.startPos+"->"+translate.endPos);
                   translate.dataSliceMD5 = dataMd5;
                   state = 2;//等待MTU检测
                   buffer.rewind();
                   send(translate.getChannelCommunication(),buffer,translate.getServerCommunicationSocketaddress());
                   break;
               }
           }
        }
    }

    private void waitMTU() {
        if (state!=2) return;
        resetTime();
        ByteBuffer buffer = ByteBuffer.allocate(Prog.DATA_BUFFER_MAX_ZONE);
        SocketAddress address;
        while (isNotOuttime()){
            buffer.clear();
            address = receive(translate.getChannelCommunication(),buffer);
            if (address!=null){
                buffer.flip();
                if (buffer.get(0) == Prog.mtuCheck){
                    send(translate.getChannelCommunication(),buffer,translate.getServerCommunicationSocketaddress());
                }
                if (buffer.get(0) == Prog.mtuSure){
                    buffer.position(1);
                    translate.mtu = buffer.getInt();
                    buffer.rewind();
                    send(translate.getChannelCommunication(),buffer,translate.getServerCommunicationSocketaddress());
                    state = 3;//数据分片
//                    Log.i("MTU : "+ translate.mtu);
                    break;
                }
            }
        }
    }

    /**
     * 数据接受
     */
    private void receiveData() {
//        Log.i("数据片段MAP: "+ translate.dataSliceMap);
        //创建文件
        File fileTemp = translate.filePath.toFile();
        if (!fileTemp.exists()){
            try {
                fileTemp.createNewFile();
            } catch (IOException e) {
                ;
            }
        }
        AsynchronousFileChannel fileChannel = null;
        state = 11; // 11 接入对方 12接受 13退出
        try {
            fileChannel = AsynchronousFileChannel.open(translate.filePath, StandardOpenOption.WRITE);
            while (state!=13){

                if (state == 11){
                    access();
                }
                if (state == 12){
                    receiveDataSlice(fileChannel);
                }
                if (state == 13){
                    checkComplete();
                }

            }
        } catch (Exception e) {
            ;
        }finally {
            closeFileChannel(fileChannel);
        }
    }




    private void access() {
        try {
            synchronized (this){
                this.wait(1000);
            }
        } catch (InterruptedException e) {
            ;
        }
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.clear();
        buffer.put(Prog.dataAccess);
        buffer.flip();
        send(translate.getChannelData(),buffer,translate.getServerDataSocketaddress());
        state = 12;
    }

    private void receiveDataSlice(AsynchronousFileChannel fileChannel) {
        if (fileChannel==null || !fileChannel.isOpen()){
            state = 13;
        }else{

            SocketAddress address = null;
            ByteBuffer recBuf = null;

            int count;
            resetTime();

            while (state == 12){
                recBuf = ByteBuffer.allocate(translate.mtu);
                recBuf.clear();
                address = receive(translate.getChannelData(),recBuf);
                if (address!=null){
                    recBuf.flip();
                    count = recBuf.getInt();
                    if (translate.dataSliceMap.containsKey(count)) {
                        fileChannel.write(recBuf,translate.dataSliceMap.get(count), count, this);
                    }else if (count==-1){
                        //结束标识
                        state = 13;
                    }
                }
                if (!isNotOuttime()){
                    if (overTimeCount<OVER_MAX){ //没有超过超时次数
                        overTimeCount++;
                        state = 11;
                    }else{
                        state = 13;
                    }
                }
            }

        }
    }

    long pos = 0;
    @Override
    public void completed(Integer integer, Integer index) {
        try{
            lock.lock();
            pos+=integer;
//            Log.i(index+ " - 当前进度: "+ pos);

            //已写入
            translate.dataSliceMap.remove(index); //移除下标
            if (translate.dataSliceMap.size() == 0){
                state = 13;
            }
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.clear();
            buffer.putInt(index);
            buffer.flip();
            send(translate.getChannelCommunication(),buffer,translate.getServerCommunicationSocketaddress());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void failed(Throwable throwable, Integer integer) {
        throwable.printStackTrace();
    }

    private void checkComplete() {
        if (translate.dataSliceMap.isEmpty()){
            byte[] md5 = MD5Util.getFileMD5Bytes(translate.filePath.toFile(),translate.startPos,translate.endPos);
//          Log.i("接受完成,检测MD5 STRING : "+ MD5Util.bytesGetMD5String(md5));
            boolean flag = MD5Util.isEqualMD5(translate.dataSliceMD5,md5);
            Log.i("接受完成,检测MD5: "+ flag+", 数据范围: "+ translate.dataLength+" , "+translate.startPos+"->"+translate.endPos);

            if (flag){
                waitTime2();
                //通知结束
                ByteBuffer buffer = ByteBuffer.allocate(1);
                buffer.clear();
                buffer.put(Prog.dataComplete);
                buffer.flip();
                send(translate.getChannelData(),buffer,translate.getServerDataSocketaddress());
                buffer.rewind();
                send(translate.getChannelCommunication(),buffer,translate.getServerCommunicationSocketaddress());
                Log.i("结束子任务,通知服务器完成.");
            }
        }else{
            state = 12;
        }
    }
}
