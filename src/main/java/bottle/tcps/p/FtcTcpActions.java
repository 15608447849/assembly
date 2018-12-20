package bottle.tcps.p;

/**
 * Created by user on 2017/7/8.
 */
public interface FtcTcpActions {
    /**
     * 连接成功
     * @param session
     */
    void connectSucceed(Session session);
    /**
     * 接受到一个消息
     * @param session 会话
     * @param message 消息
     */
    void receiveString(Session session,String message);

    /**
     * 接受数据流
     */
    void receiveBytes(Session session,byte[] bytes);

    /**
     * 连接关闭
     */
    void connectClosed(Session session);

    void error(Session session,Throwable throwable,Exception e);

    void connectFail(Session session);
}
