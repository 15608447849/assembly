package com.m.backup.client;

import com.winone.ftc.mtools.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 2017/11/24.
 *
 */
public class FileVisitor extends SimpleFileVisitor<Path>{
    private static final String TAG = "文件夹遍历器";
    private final FtcBackupClient ftcBackupClient;
    private final Path home;
    private InetSocketAddress serverAddress;
    private List<String> filterSuffixList = new ArrayList<>();
    public FileVisitor(FtcBackupClient ftcBackupClient) {
        this.ftcBackupClient = ftcBackupClient;
        home = Paths.get(ftcBackupClient.getDirectory());
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return FileVisitResult.SKIP_SUBTREE;
    }
    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {

        try {

            boolean isAdd = true;
            File file = filePath.toFile();
            String fileName = file.getName();
            if (fileName.contains(".")){
                String suffix = file.getName().substring(file.getName().lastIndexOf("."));
                if (filterSuffixList.contains(suffix)){
                    isAdd = false;
                }
            }

            if (isAdd)
            {
//                Log.i(Thread.currentThread() +" 遍历文件 : "+ filePath.toFile().getCanonicalPath());
                ftcBackupClient.addBackupFile(filePath.toFile(),this.serverAddress);//添加一个同步文件
                try {Thread.sleep(100);} catch (InterruptedException e) {}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    public void startVisitor(InetSocketAddress serverAddress,String... filterSuffix){
        if (this.serverAddress!=null) throw new IllegalStateException("visitor file acting to translate "+this.serverAddress+",do not repeat the attempt.");
        this.serverAddress = serverAddress;
        filterSuffixList.clear();
        if (filterSuffix!=null){
            for (String suffix : filterSuffix){
                filterSuffixList.add(suffix);
            }
        }
        try {
            Files.walkFileTree(home, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.serverAddress = null;

    }



}
