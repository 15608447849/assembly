package bottle.tcps.c;


import bottle.tcps.p.Session;
import bottle.tcps.p.SocketImp;

/**
 * Created by user on 2017/7/8.
 * 客户端 : 读取服务发送的内容 并且可以发送数据到服务器
 */
public class ServerSession extends Session {
    public ServerSession(SocketImp connect) {
        super(null,connect);
    }
}
