package com.m.tcps.s;

import com.m.backup.server.FileUpServerHandle;
import com.m.backup.server.FtcBackupServer;
import com.m.tcps.p.FtcTcpActionsAdapter;
import com.m.tcps.p.Session;
import com.m.tcps.p.SocketImp;
import com.winone.ftc.mtools.Log;

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
        new FileUpServerHandle(session,ftcBackupServer);
    }

    @Override
    public void receiveString(Session session, String message) {
        super.receiveString(session, message);
        Log.i(message);
    }
}
