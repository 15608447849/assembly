package com.m.tcps.p;


import com.winone.ftc.mtools.Log;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Created by user on 2017/7/8.
 * 通讯会话
 */
public abstract class Session implements CompletionHandler<Integer, ByteBuffer>{

    private final FtcTcpAioManager ftcTcpManager;
    private final SocketImp socketImp;

    private final SessionContentStore sessionContentStore;//接受数据存储
    private final SessionOperation operation;
    private SessionContentHandle sessionHandle;
    private long send_sum,recv_sum;


    public Session(FtcTcpAioManager manager,SocketImp connect) {
        this.ftcTcpManager = manager;
        this.socketImp = connect;
        this.sessionContentStore = new SessionContentStore();
        this.operation = new SessionOperation(this);

    }
    //系统读取到信息 回调到这里
    @Override
    public void completed(Integer integer, ByteBuffer buffer) {
        recv_sum+=integer;
//        Log.println("总接收: "+ recv_sum+" - "+ buffer);
//        Log.println("接收: "+ integer+" - "+ buffer);
        if (!socketImp.isAlive()) return;
        if (integer == -1){
            //一个客户端 连接异常
            socketImp.getAction().error(this,null, new SocketException("socket connect is closed."));
            return;
        }
        if (integer>0){
            if (sessionHandle==null){
                sessionHandle = new SessionContentHandle(this);
            }
            sessionContentStore.storeBuffer(buffer);

        }
        read();
    }

    @Override
    public void failed(Throwable throwable, ByteBuffer buffer) {
        //读取数据异常
        socketImp.getAction().error(this,throwable,null);
    }

    /**
     * 读取数据(异步)
     */
    public void read(){
            ByteBuffer buffer = sessionContentStore.getReadBufferBySystemTcpStack();
            if (buffer!=null && socketImp.isAlive()){
                socketImp.getSocket().read(buffer, buffer,this);//系统从管道读取数据
            }
    }

    /**
     * 发送数据(同步)
     */
    public void send(ByteBuffer buffer) {
                if (buffer!=null && socketImp.isAlive()){
                  buffer.flip();
                  Future<Integer> future =  socketImp.getSocket().write(buffer); //发送消息到管道
                    try {
                        while(true){
                            if (future.isDone()){
                                send_sum+=future.get();
    //                            Log.println("总发送 : "+ send_sum);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        //发送数据异常
                        socketImp.getAction().error(this,null,e);
                    }
                }

    }

    public void clear(){
        //清理 剩余保存的数据 ,清理队里中已存在的数据
        sessionContentStore.clear();
        //关闭处理读取消息的线程
        if (sessionHandle!=null){
            sessionHandle.close();
            sessionHandle=null;
        }
    }
    public void close(){
        //关闭管道
        clear();
        socketImp.close();
    }



    public SessionOperation getOperation(){return operation;}
    public AsynchronousSocketChannel getSocket(){
        return socketImp.getSocket();
    }
    public SocketImp getSocketImp(){
        return socketImp;
    }
    public SessionContentStore getStore(){return sessionContentStore;}
    public FtcTcpAioManager getFtcTcpAioManager(){
        return ftcTcpManager;
    }

}