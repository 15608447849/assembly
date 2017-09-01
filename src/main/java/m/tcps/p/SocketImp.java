package m.tcps.p;

import m.tcps.c.ServerSession;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * Created by user on 2017/7/8.
 */
public interface SocketImp {
    /**
     * 获取socket
     * @return
     */
     AsynchronousSocketChannel getSocket();
    /**
     * 是否存活
     * @return
     */
     boolean isAlive();

    /**
     * 获取通讯实现对象
     * @return
     */
     CommunicationAction getCommunication();

    /**
     * 关闭
     */
    void close();

    /**
     * 连接异常
     * @param throwable
     */
    void ConnectError(Throwable throwable);

    Session getSession();
    Op getOp();

    /**如果是服务器端, 获取服务器*/
    SockServer getServer();
}
