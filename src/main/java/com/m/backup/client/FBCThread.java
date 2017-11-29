package com.m.backup.client;

/**
 * Created by user on 2017/11/24.
 */
public abstract class FBCThread extends Thread {
    protected FtcBackupClient ftcBackupClient;
    protected boolean isRunning = true;
    public FBCThread(FtcBackupClient ftcBackupClient) {
        this.ftcBackupClient = ftcBackupClient;
        this.start();
    }
}
