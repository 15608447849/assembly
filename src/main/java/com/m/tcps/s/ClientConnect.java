package com.m.tcps.s;

import com.m.tcps.p.*;
import com.winone.ftc.mtools.Log;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by user on 2017/7/8.
 * 读取数据 和 写入数据
 */
public class ClientConnect implements SocketImp{
    private FtcSocketServer server;
    //与某一个客户端的管道
    private AsynchronousSocketChannel socket;
    //与客户端的 会话
    private ClientSession session;
    //通讯实现对象
    private FtcTcpActions cAction;

    public ClientConnect(FtcSocketServer server) {
        this.server = server;
    }

    @Override
    public AsynchronousSocketChannel getSocket() {
        return socket;
    }
    public ClientConnect setSocket(AsynchronousSocketChannel socket) {
        this.socket = socket;
        return this;
    }
    public ClientConnect initial(){
        server.add(this);
        session = new ClientSession(server,this);
        cAction.connectSucceed(session);
        return this;
    }
    @Override
    public boolean isAlive(){
        return socket!=null && socket.isOpen();
    }

    @Override
    public FtcTcpActions getAction() {
        return cAction;
    }

    @Override
    public void setAction(FtcTcpActions action) {
        this.cAction = action;
    }
    public ClientConnect setAction(FtcTcpActionsAdapter action) {
        this.cAction = action;
        return this;
    }

    @Override
    public void close(){
        try {

            if (isAlive()){
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
            session.clear();
            server.remove(this);//服务器 队列中移除一个连接管理对象.
        } catch (IOException e) {
            cAction.error(session,null,e);
        }finally {
            socket = null;
            server = null;
            session=null;
            cAction.connectClosed(session);//客户端在服务端的连接
        }
    }



    @Override
    public Session getSession() {
        return session;
    }



}
