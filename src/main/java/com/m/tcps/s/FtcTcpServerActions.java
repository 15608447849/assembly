package com.m.tcps.s;

import com.m.tcps.p.FtcTcpActionsAdapter;
import com.m.tcps.p.Session;
import com.m.tcps.p.SocketImp;

/**
 * Created by user on 2017/11/23.
 */
public abstract class FtcTcpServerActions extends FtcTcpActionsAdapter {
    @Override
    public void connectSucceed(Session session) {
        setClientFtcAction(session.getSocketImp());
    }
    public abstract void setClientFtcAction(SocketImp socketImp);

}
