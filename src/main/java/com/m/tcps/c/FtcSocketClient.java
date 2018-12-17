package com.m.tcps.c;

import com.m.tcps.p.*;
import com.winone.ftc.mtools.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.List;
import java.util.concurrent.Executors;
/**
 * Created by user on 2017/7/8.
 * 1 发送字符串
 * 2 接受字符串
 * 3 接受数据
 *
 */
public class FtcSocketClient implements SocketImp, CompletionHandler<Void, Void>,FtcTcpAioManager{
    //连接等待时间
    private final int connectingTime;
    public AsynchronousSocketChannel socket;
    private InetSocketAddress localAddress;
    private AsynchronousChannelGroup asynchronousChannelGroup;
    private boolean isConnected;
    private InetSocketAddress serverAddress;
    private FtcTcpActions communicationAction;
    private final ServerSession session = new ServerSession(this);//读取写入
    public FtcSocketClient(InetSocketAddress serverAddress, FtcTcpActions communicationAction) {
        this(null,serverAddress,communicationAction,3000);
    }
    public FtcSocketClient(InetSocketAddress localAddress, InetSocketAddress serverAddress, FtcTcpActions communicationAction, int reTime) {
        this.localAddress = localAddress;
        this.connectingTime = reTime;
        this.serverAddress = serverAddress;
        this.communicationAction = communicationAction;

    }
    //连接服务器
    public void connectServer() throws IOException{

        if (isAlive()) return;

        if (asynchronousChannelGroup==null){
            asynchronousChannelGroup =AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
        }

        socket = AsynchronousSocketChannel.open(asynchronousChannelGroup);

        if (localAddress!=null){
            socket.bind(localAddress);
        }

        socket.setOption(StandardSocketOptions.SO_KEEPALIVE,true);//保持连接

        socket.setOption(StandardSocketOptions.TCP_NODELAY,true);

        socket.connect(serverAddress,null,this);

        String localAddress = socket.getLocalAddress().toString();
        //等待连接
        synchronized (this){
            try {
                wait(connectingTime);
            } catch (InterruptedException e) {
            }
        }
        //如果连接不上服务器 - 报异常
        if (!isAlive()){
            throw new SocketException("local client( "+ localAddress + " ) connect remote server( " + serverAddress + " ) fail.");
        }
    }


    @Override
    public void completed(Void aVoid, Void aVoid2) {
        isConnected = true;
        synchronized (this){
            notify();
        }
//        Log.i("成功连接 - "+serverAddress,",启动数据读取...");
        communicationAction.connectSucceed(session);
        session.read();
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        //连接失败异常,关闭连接
        synchronized (this){
            notify();
        }
        communicationAction.error(session,throwable,null);
        communicationAction.connectFail(session);
        communicationAction.connectClosed(session);
    }

    /**
     * 关闭连接
     */
    private void closeConnect() {
        if (socket == null) return;
//        Log.e("socket 客户端关闭连接");
        session.clear();//清理会话
        try {
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (Exception e) {
            communicationAction.error(session,null,e);
        }finally {
            isConnected = false;
            socket = null;
        }
    }



    @Override
    public AsynchronousSocketChannel getSocket() {
        return socket;
    }

    @Override
    public boolean isAlive() {
        return socket!=null && socket.isOpen()&&isConnected;
    }

    @Override
    public FtcTcpActions getAction() {
        return communicationAction;
    }

    @Override
    public void setAction(FtcTcpActions action) {
        this.communicationAction = action;
    }

    @Override
    public void close() {
        closeConnect();
    }

    @Override
    public Session getSession() {
        return session;
    }


    @Override
    public FtcTcpAioManager getFtcTcpManager() {
        return null; //不实现
    }

    @Override
    public List<SocketImp> getCurrentClientList() {
        return null;//不实现
    }

    @Override
    public int getCurrentClientSize() {
        return 0;//不实现
    }
}
