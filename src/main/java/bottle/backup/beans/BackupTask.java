package bottle.backup.beans;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class BackupTask {

    private int type =  -1;

    private BackupFile backupFile;

    private InetSocketAddress serverAddress;

    private int loopCount = 0;

    public BackupTask( InetSocketAddress serverAddress,BackupFile backupFile) {
        type = 1;
        this.backupFile = backupFile;
        this.serverAddress = serverAddress;
    }

    private List<BackupFile> backupFileList;

    public BackupTask(InetSocketAddress serverAddress, List<BackupFile> backupFileList) {
        type = 2;
        this.serverAddress = serverAddress;
        this.backupFileList = new ArrayList<>(backupFileList);
    }

    public int getType() {
        return type;
    }

    public BackupFile getBackupFile() {
        return backupFile;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public List<BackupFile> getBackupFileList() {
        return backupFileList;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void incLoopCount() {
        this.loopCount++;
    }


}
