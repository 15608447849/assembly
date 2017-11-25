package m.tcps.p;

import m.tcps.p.FtcTcpActions;
import m.tcps.p.Session;

/**
 * Created by user on 2017/11/23.
 */
public abstract class FtcTcpActionsAdapter implements FtcTcpActions{
    @Override
    public void connectSucceed(Session session) {

    }

    @Override
    public void receiveString(Session session, String message) {

    }

    @Override
    public void receiveBytes(Session session, byte[] bytes) {

    }

    @Override
    public void connectClosed(Session session) {

    }

    @Override
    public void error(Session session, Throwable throwable, Exception e) {

    }
}
