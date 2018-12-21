package bottle.backup.client;

import bottle.backup.beans.BackupFile;
import bottle.ftc.tools.Log;

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

    private List<BackupFile> fileList = new ArrayList<>();
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
        boolean isAdd = !ftcBackupClient.isFilterSuffixFile(filePath.toFile());
        if (isAdd) fileList.add(ftcBackupClient.genBackupFile(filePath.toFile()));

        return FileVisitResult.CONTINUE;
    }


    public synchronized List<BackupFile> startVisitor() throws IOException{

        fileList.clear();
        Files.walkFileTree(home, this);
        List<BackupFile> result = new ArrayList<>(fileList);
        fileList.clear();
        return result;
    }

}
