package bottle.backup.client;

/**
 * Created by user on 2017/11/24.
 */
public abstract class FBCThread extends Thread {
    protected FtcBackupClient ftcBackupClient;
    protected volatile boolean isRunning = true;
    public FBCThread(FtcBackupClient ftcBackupClient) {
        this.ftcBackupClient = ftcBackupClient;
        this.setName("ftc-t-"+getId());
        this.setDaemon(true);
        this.start();
    }


}
