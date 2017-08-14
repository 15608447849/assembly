package m.tcps.c;

import com.winone.ftc.mtools.Log;
import m.tcps.p.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
/**
 * Created by user on 2017/7/8.
 * 1 发送字符串
 * 2 接受字符串
 * 3 接受数据
 *
 */
public class FtcSocketClient implements SocketImp, CompletionHandler<Void, Void> {
    //重新连接时间
    private static final int RECONNECT_TIME = 1000 * 30;
    public AsynchronousSocketChannel socket;
    private boolean isConnected;
    private InetSocketAddress serverAddress;
    private CommunicationAction communicationAction;
    private SessionBean readBean = new SessionBean(1);
    private ServerSession session = new ServerSession(this);//读取写入
    public FtcSocketClient(InetSocketAddress serverAddress, CommunicationAction communicationAction) {
        this.serverAddress = serverAddress;
        this.communicationAction = communicationAction;
    }

    //连接服务器
    public void connectServer(){
        try {
                socket = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10)));
                socket.setOption(StandardSocketOptions.SO_KEEPALIVE,true);//保持连接
                socket.setOption(StandardSocketOptions.TCP_NODELAY,true);
                socket.connect(serverAddress,null,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void completed(Void aVoid, Void aVoid2) {
        isConnected = true;
        Log.i("成功连接 - "+serverAddress,",启动数据读取...");
        communicationAction.connectSucceed(session);
        session.read(readBean);
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
        throwable.printStackTrace();
        //重新连接
        reConnection();
    }
    /**
     * 重新连接
     */
    public void reConnection() {
        closeConnect();
        try {
            Thread.sleep(RECONNECT_TIME);
        } catch (InterruptedException e) {
        }
        connectServer();
    }

    public void closeConnect() {
        if (socket==null) return;
        try {
            socket.close();
            Log.i(" TCP CONNECTED CLOSE.");
        } catch (IOException e) {
        }finally {
            socket = null;
            isConnected = false;
            readBean.close();
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
    public CommunicationAction getCommunication() {
        return communicationAction;
    }

    @Override
    public void close() {
        closeConnect();
    }

    @Override
    public void ConnectError(Throwable throwable) {
        throwable.printStackTrace();
        reConnection();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Op getOp() {
        return session.getOp();
    }


}
