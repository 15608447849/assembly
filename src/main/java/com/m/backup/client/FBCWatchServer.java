package com.m.backup.client;

import com.winone.ftc.mtools.Log;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 2017/11/27.
 */
public class FBCWatchServer extends FileAlterationMonitor implements FileAlterationListener {



    private final FtcBackupClient ftcBackupClient;
    private final Set<File> modifyList = new HashSet<>();
    public FBCWatchServer(FtcBackupClient ftcBackupClient) {
        super(1000);
        this.ftcBackupClient = ftcBackupClient;
        FileAlterationObserver observer = new FileAlterationObserver(new File(ftcBackupClient.getDirectory()));
        observer.addListener(this);
        this.addObserver(observer);
    }

    @Override
    public void onStart(FileAlterationObserver observer) {

    }
    @Override
    public void onDirectoryCreate(File directory) {

    }
    @Override
    public void onDirectoryChange(File directory) {

    }
    @Override
    public void onDirectoryDelete(File directory) {

    }
    @Override
    public void onFileCreate(File file) {
        modifyList.add(file);
    }
    @Override
    public void onFileChange(File file) {
//        Log.i("文件改变 "+file+"  "+ new SimpleDateFormat("HH:mm:ss").format(new Date(file.lastModified())) );
        modifyList.add(file);
    }
    @Override
    public void onFileDelete(File file) {

    }
    @Override
    public void onStop(FileAlterationObserver observer) {

        Iterator<File> iterator = modifyList.iterator();
        File mFile;
        while (iterator.hasNext()){
            mFile = iterator.next();
            if (mFile.exists()){
                int c = (int) (System.currentTimeMillis() - mFile.lastModified());
                if(c > 90*1000){
                    iterator.remove();
                    final List<InetSocketAddress> serverAddressList = ftcBackupClient.getServerAddressList();
                    if (serverAddressList!=null && serverAddressList.size()>0){
                        for (InetSocketAddress it:serverAddressList){
                            try {
                                ftcBackupClient.addBackupFile(mFile, it);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }else{
                iterator.remove();
            }

        }
    }


}
