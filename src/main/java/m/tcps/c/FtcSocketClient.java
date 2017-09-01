package m.tcps.c;

import com.winone.ftc.mtools.Log;
import m.tcps.p.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
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
    private final int RECONNECT_TIME;
    public AsynchronousSocketChannel socket;

    private InetSocketAddress localAddress;
    private AsynchronousChannelGroup asynchronousChannelGroup;

    private boolean isConnected;
    private InetSocketAddress serverAddress;
    private CommunicationAction communicationAction;
    private final SessionBean readBean = new SessionBean(1);
    private final ServerSession session = new ServerSession(this);//读取写入
    public FtcSocketClient(InetSocketAddress serverAddress, CommunicationAction communicationAction) {
        this(null,serverAddress,communicationAction,3000);
    }
    public FtcSocketClient(InetSocketAddress localAddress,InetSocketAddress serverAddress, CommunicationAction communicationAction,int reTime) {
        this.localAddress = localAddress;
        this.RECONNECT_TIME = reTime;
        this.serverAddress = serverAddress;
        this.communicationAction = communicationAction;

    }
    //连接服务器
    public void connectServer(){
        try {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void completed(Void aVoid, Void aVoid2) {
        isConnected = true;
//        Log.i("成功连接 - "+serverAddress,",启动数据读取...");
        communicationAction.connectSucceed(session);
        session.read(readBean);
    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
//        throwable.printStackTrace();
        communicationAction.error(session,throwable,null);
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
//            asynchronousChannelGroup.shutdown();
            socket.close();
//            Log.i(" TCP CONNECTED CLOSE.");

        } catch (Exception e) {
            communicationAction.error(session,null,e);
        }finally {
//            asynchronousChannelGroup=null;
            socket = null;
            isConnected = false;
            readBean.close();
            communicationAction.connectClosed(session);
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

    @Override
    public SockServer getServer() {
        //客户端获取不到客户端对象
        return null;
    }
}
