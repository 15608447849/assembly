package m.tcps.s;

import m.tcps.p.*;

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
            server.remove(this);
            session.close();
            if (isAlive()){
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }
            cAction.connectClosed(session);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
//            socket = null;
//            session = null;
//            server = null;
        }
    }

    @Override
    public void ConnectError(Throwable throwable) {
        //一个客户端 连接异常
        close();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public String getInfo() {

        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\t");
            stringBuilder.append("本地:服务端(  ");
            stringBuilder.append(socket.getLocalAddress().toString());
            stringBuilder.append(" )");
            stringBuilder.append("<------------> ");
            stringBuilder.append("远程:客户端(  ");
            stringBuilder.append(socket.getRemoteAddress());
            stringBuilder.append("  )");
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
