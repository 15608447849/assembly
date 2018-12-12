package com.m.backup.beans;

import com.winone.ftc.mtools.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;

/**
 * Created by lzp on 2017/11/24.
 */
public class BackupFileInfo {


    private String dirs;
    private String rel_path;
    private String fileName;
    private InetSocketAddress serverAddress;

    private int loopCount = 0;


    public BackupFileInfo(String dirs,String file,InetSocketAddress serverAddress) {
        this.dirs = FileUtil.replaceFileSeparatorAndCheck(dirs,null,FileUtil.SEPARATOR);
        file = FileUtil.SEPARATOR+FileUtil.replaceFileSeparatorAndCheck(file,FileUtil.SEPARATOR,null);
        this.rel_path = file.substring(0,file.lastIndexOf(FileUtil.SEPARATOR)+1);
        this.fileName = file.substring(file.lastIndexOf(FileUtil.SEPARATOR)+1);
        this.serverAddress = serverAddress;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public String getDirs() {
        return dirs;
    }


    public String getRel_path() {
        return rel_path;
    }


    public String getFileName() {
        return fileName;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }


    @Override
    public String toString() {
        return "[" +
                dirs + rel_path +fileName +
                " -> " +serverAddress +
                "]";
    }

    public synchronized void clear() {
        //关闭本地文件流等 清理任务
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                randomAccessFile=null;
            }
        }
    }

    public String getFullPath() {
        return dirs+rel_path+fileName;
    }

    public long getFileLength() {
        return new File(getFullPath()).length();
    }

    private RandomAccessFile randomAccessFile;
    public synchronized RandomAccessFile getRandomAccessFile() throws IOException {
        if (randomAccessFile==null) randomAccessFile = new RandomAccessFile(getFullPath(),"r");
        return randomAccessFile;
    }
}
