package m.tcps.s;

import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.NetworkUtil;
import m.tcps.p.CommunicationAction;
import m.tcps.p.SockServer;
import m.tcps.p.SocketImp;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by user on 2017/7/8.
 * aio 实现
 */
public class FtcSocketServer implements SockServer,CompletionHandler<AsynchronousSocketChannel, ClientConnect> {
    //监听本地地址信息
    private InetSocketAddress address;
    //异步连接socket
    private AsynchronousServerSocketChannel listener;

    private CommunicationAction action;

    private final LinkedList<SocketImp> clientConnectList = new LinkedList<>();

    public FtcSocketServer(InetSocketAddress address,CommunicationAction action) {
        this.address = address;
        this.action = action;
    }


    //打开监听连接
    public FtcSocketServer openListener() throws IOException{
        listener = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))).bind(address);
        listener.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        Log.i("TCP SERVER LISTEN : "+ listener.getLocalAddress());
        return this;
    }
    //开始接入
    public FtcSocketServer launchAccept(){
        if (listener!=null && listener.isOpen()){
            listener.accept(new ClientConnect(this).setCommunicationAction(action),this); //接受一个连接
        }
        return this;
    }

    @Override
    public void completed(AsynchronousSocketChannel asynchronousSocketChannel, ClientConnect clientConnect) {
        launchAccept();
        //处理当前连接
        clientConnect.setSocket(asynchronousSocketChannel).initial();
    }

    @Override
    public void failed(Throwable throwable, ClientConnect clientConnect) {
        throwable.printStackTrace();
        launchAccept();
    }
    @Override
    public List<SocketImp> getCurrentClientList(){
        return clientConnectList;
    }

    public synchronized void add(ClientConnect clientConnect) {
        clientConnectList.add(clientConnect);
    }

    public synchronized void remove(ClientConnect clientConnect) {
        clientConnectList.remove(clientConnect);
    }

    @Override
    public int getCurrentClientSize() {
        return clientConnectList.size();
    }
}
