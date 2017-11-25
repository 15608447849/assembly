package m.lanshare.threads;

import m.lanshare.LANManage;
import m.lanshare.beads.PThread;
import m.lanshare.beads.Prog;
import m.lanshare.beads.TranslateServer;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Paths;

/**
 * Created by user on 2017/6/20.
 */
public class ServerCommunication extends PThread{
    private TranslateServer translate;
    private ServerDataTranslate dataTranslate;
    private DatagramChannel communicationChannel;
    public ServerCommunication(SocketAddress communicationSocket, byte[] fileMd5, DatagramChannel channel, LANManage manager) {
        translate = new TranslateServer();
        translate.manage = manager;
        translate.communicationPort =  translate.manage.getPort();
        translate.dataPort =  translate.manage.getPort();
        try {
            translate.communicationChannel = DatagramChannel.open().bind(new InetSocketAddress(translate.communicationPort));
            translate.communicationChannel.configureBlocking(false);

            translate.dataChannel = DatagramChannel.open().bind(new InetSocketAddress(translate.dataPort));
            translate.dataChannel.configureBlocking(false);

            this.communicationChannel = translate.communicationChannel;
            this.start();

            ByteBuffer buffer = ByteBuffer.allocate(1+4+4);
            buffer.clear();
            buffer.put(Prog.notifyServerCreateConnectResp);
            buffer.putInt(translate.communicationPort);
            buffer.putInt(translate.dataPort);
            buffer.flip();
            channel.send(buffer,communicationSocket);

        } catch (IOException e) {
            ;
        }
    }


    @Override
    public void run() {
        //接受文件信息
        receiveDataInfo();
        //检测MTU
        checkMTU();
        if (state == 2){
            dataSlice(translate);
            //打开数据传输线程
            dataTranslate = new ServerDataTranslate(translate);
            receiveSucceedSliceInfo();
        }
        //返回端口,结束
        closeChannal(communicationChannel,translate.manage);

    }




    private void receiveDataInfo() {
        Log.i("等待客户端发送数据信息...");
        ByteBuffer buffer = ByteBuffer.allocate(Prog.UDP_DATA_MIN_BUFFER_ZONE);
        SocketAddress address;
        byte command;
        resetTime();
        while (isNotOuttime()){
            buffer.clear();
            address = receive(communicationChannel,buffer);
            if (address!=null){
                //接受到数据信息 ; 长度,起点,终点,文件路径
                buffer.flip();
                command = buffer.get();
                if (command==Prog.sendDataInfo){

                    translate.clientCommunicationAddress = address;
                    translate.dataLength = buffer.getLong();
                    translate.startPos = buffer.getLong();
                    translate.endPos = buffer.getLong();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    try {
                        String s = new String(data,"UTF-8");
                        translate.filePath = Paths.get(s);
                        byte[] dataMd5 = MD5Util.getFileMd5( translate.filePath.toFile(),translate.startPos,translate.endPos);
                        Log.i("获取数据片段MD5: "+MD5Util.byteToHexString(dataMd5)+" fileSize:"+ translate.filePath.toFile().length());
                        buffer.clear();
                        buffer.put(Prog.sendDataInfoResp);
                        buffer.put(dataMd5);
                        buffer.flip();
                        send(communicationChannel,buffer,address);
                    } catch (UnsupportedEncodingException e) {
                        ;
                    }
                }
                if (command==Prog.sendDataInfoResp){
                    state = 1; //进入MTU检测.
                    break;
                }
            }
        }
    }


    private void checkMTU() {
        if (state!=1) return;
        int count = Prog.DATA_BUFFER_MAX_ZONE;
        ByteBuffer buffer = ByteBuffer.allocate(count);
        SocketAddress address;
        while (isNotOuttime()){
            if (count>Prog.UDP_DATA_MIN_BUFFER_ZONE){
                buffer.clear();
                for (int i = 0;i<count;i++){
                    buffer.put(Prog.mtuCheck);
                }
                buffer.flip();
                send(communicationChannel,buffer,translate.clientCommunicationAddress);
                count--;
                waitTime();
            }
            //接收
            buffer.clear();
            address = receive(communicationChannel,buffer);
            if (address!=null){
                buffer.flip();
                //收到MTU确定
                if (buffer.get(0) == Prog.mtuCheck){
                    translate.mtu = buffer.limit();
                    buffer.clear();
                    buffer.put(Prog.mtuSure);
                    buffer.putInt(translate.mtu);
                    buffer.flip();
                    send(communicationChannel,buffer,translate.clientCommunicationAddress);
                    count = 0;
                    Log.i("MTU 检测 : "+ translate.mtu);
                }
                if (buffer.get(0) == Prog.mtuSure){
                    state = 2;//开始分片数据.
                    break;
                }
            }
        }
    }


    /**
     * 接受成功的分片单元
     */
    private void receiveSucceedSliceInfo() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        SocketAddress address;
        int index;
        while (isNotOuttime()){
            buffer.clear();
            address = receive(communicationChannel,buffer);
            if (address!=null){
                buffer.flip();
                if (buffer.get(0) == Prog.dataComplete){
                    break;
                }
                index = buffer.getInt();
                //Log.i(this + "成功接受数据片段下标:"+index);
                dataTranslate.notifySuccessSlice(index);
                resetTime();
            }
        }
    }






}
