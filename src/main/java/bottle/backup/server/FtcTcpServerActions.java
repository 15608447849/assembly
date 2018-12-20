package bottle.backup.server;

import bottle.backup.server.FileUpServerHandle;
import bottle.backup.server.FtcBackupServer;
import bottle.ftc.tools.Log;
import bottle.tcps.p.FtcTcpActionsAdapter;
import bottle.tcps.p.Session;

/**
 * Created by user on 2017/11/23.
 */
public class FtcTcpServerActions extends FtcTcpActionsAdapter {

    private final FtcBackupServer ftcBackupServer;

    public FtcTcpServerActions(FtcBackupServer ftcBackupServer) {
        this.ftcBackupServer = ftcBackupServer;
    }

    @Override
    public void connectSucceed(Session session) {
        FileUpServerHandle fileUpServerHandle = new FileUpServerHandle(ftcBackupServer);
        fileUpServerHandle.bindSession(session);
    }

}
