package m.tcps.s;

import com.winone.ftc.mtools.Log;
import m.tcps.p.*;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by user on 2017/7/8.
 * 读取数据 和 写入数据
 */
public class ClientConnect implements SocketImp{
    private FtcSocketServer server;
    //管道
    private AsynchronousSocketChannel socket;
    //与客户端的 会话
    private ClientSession session;
    //通讯实现对象
    private CommunicationAction cAction;

    //当前使用的 监听读取 数据对象
    private SessionBean readBean;

    public ClientConnect(FtcSocketServer server) {
        this.server = server;
    }

    public ClientConnect setCommunicationAction(CommunicationAction cAction){
        this.cAction = cAction;
        return this;
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
        session = new ClientSession(this);
        cAction.connectSucceed(session);
        return this;
    }
    @Override
    public boolean isAlive(){
        return socket!=null && socket.isOpen();
    }

    @Override
    public CommunicationAction getCommunication() {
        return cAction;
    }
    @Override
    public void close(){
        try {
            server.remove(this);
            session.close();
            if (isAlive()){
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
            Log.i("关闭: "+ this+" "+ socket);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            socket = null;
            session = null;
            server = null;
        }
    }

    @Override
    public void ConnectError(Throwable throwable) {
        throwable.printStackTrace();
        //一个客户端 连接异常
        close();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public Op getOp() {
        if (session!=null) return session.getOp();
        return null;
    }


    public FtcSocketServer getServer() {
        return server;
    }
    public CommunicationAction getCommunicationAction() {
        return cAction;
    }

}
