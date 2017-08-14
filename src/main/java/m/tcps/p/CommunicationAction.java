package m.tcps.p;

import java.sql.Connection;

/**
 * Created by user on 2017/7/8.
 */
public interface CommunicationAction {
    //连接成功
    void connectSucceed(Session session);

    /**
     * 接受到一个消息
     * @param session 会话
     * @param operation 操作
     * @param message 消息
     */
    void receiveString(Session session,Op operation,String message);



}