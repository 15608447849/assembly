package m.lanshare.beads;

import m.lanshare.LANManage;
import m.lanshare.threads.ClientCommunicationAndDataTranslate;
import m.lanshare.threads.UDPFileTaskThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.file.Path;

/**
 * Created by user on 2017/6/19.
 */
public class TranslateClient extends TranslateData {
    //父节点
    public UDPFileTaskThread manager;
    //服务端的通讯公开地址
    public SocketAddress serverSocketAddress;
    //分配的本地端口号 - 信息端口
    public InetSocketAddress communicationAddress;
    //分配的本地端口 - 数据端口
    public InetSocketAddress dataAddress;
    //这段数据的MD5
    public byte[] dataSliceMD5;
    //数据文件在对端存储路径
    public byte[] filePathBytes;
    //数据文件路径-本地
    public Path filePath;
    public boolean isExcute = false;
    public TranslateClient(UDPFileTaskThread manager) {
        this.manager = manager;
        this.communicationAddress = new InetSocketAddress(
                this.manager.getLanManage().getInfo().getSendSocketAddress().getAddress(),
                this.manager.getLanManage().getPort());
        this.dataAddress = new InetSocketAddress(
                this.manager.getLanManage().getInfo().getSendSocketAddress().getAddress(),
                this.manager.getLanManage().getPort());
        this.manager.addChildTask(this);
        this.filePath = this.manager.getFileTempPath();//本地文件下载位置.
    }
    private ClientCommunicationAndDataTranslate communicationThread;


    /**
     * 设置数据片段
     */
    public void setDataSlice(long fileSize, long start, long end) {
        dataLength = fileSize;
        startPos = start;
        endPos = end;
    }

    //文件总的md5
    public byte[] fileAllMd5(){
        return manager.getFileMD5();
    }


    private DatagramChannel channelCommunication;
    private DatagramChannel channelData;
    /**
     * 初始化管道
     */
    private void initChannel() {
        try {
            channelCommunication = DatagramChannel.open().bind(communicationAddress);
            channelCommunication.configureBlocking(false);

            channelData = DatagramChannel.open().bind(dataAddress);
            channelData.configureBlocking(false);
        } catch (IOException e) {
            ;
        }
    }


    public SocketAddress getServerSocketAddress() {
        return serverSocketAddress;
    }

    public DatagramChannel getChannelCommunication() {
        return channelCommunication;
    }

    public DatagramChannel getChannelData() {
        return channelData;
    }

    public void connect() {
        isExcute = true;
        initChannel();
        communicationThread = new ClientCommunicationAndDataTranslate(this);
    }


    /**
     * 对端打开的地址
     */
    private SocketAddress ServerCommunicationSocketaddress;
    private SocketAddress ServerDataSocketaddress;

    public void setServerCommunicationSocketaddress(SocketAddress serverCommunicationSocketaddress) {
        ServerCommunicationSocketaddress = serverCommunicationSocketaddress;
    }

    public void setServerDataSocketaddress(SocketAddress serverDataSocketaddress) {
        ServerDataSocketaddress = serverDataSocketaddress;
    }

    public SocketAddress getServerCommunicationSocketaddress() {
        return ServerCommunicationSocketaddress;
    }

    public SocketAddress getServerDataSocketaddress() {
        return ServerDataSocketaddress;
    }
    
    //获取LANmanager
    public LANManage getLANManager(){
        return manager.getLanManage();
    }

    public UDPFileTaskThread getManager() {
        return manager;
    }
}
