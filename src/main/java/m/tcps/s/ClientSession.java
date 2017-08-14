package m.tcps.s;

import m.tcps.p.Session;
import m.tcps.p.SessionBean;
import m.tcps.p.SocketImp;

/**
 * Created by user on 2017/7/8.
 */
public class ClientSession extends Session {
    private SessionBean readBean;
    public ClientSession(SocketImp connect) {
        super(connect);
        this.readBean = new SessionBean(1);
        read(this.readBean);
    }
    @Override
    public void close() {
        readBean.close();
    }
}
