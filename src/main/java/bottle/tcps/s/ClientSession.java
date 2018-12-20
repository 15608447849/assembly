package bottle.tcps.s;

import bottle.tcps.p.FtcTcpAioManager;
import bottle.tcps.p.Session;
import bottle.tcps.p.SocketImp;

/**
 * Created by user on 2017/7/8.
 * 服务端 - 与某一个客户端的 会话
 */
public class ClientSession extends Session {

    public ClientSession(FtcTcpAioManager manager, SocketImp connect) {
        super(manager,connect);
        read();
    }

}
