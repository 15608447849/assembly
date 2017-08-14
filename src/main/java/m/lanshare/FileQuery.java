package m.lanshare;

import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * Created by user on 2017/6/19.
 */
public class FileQuery extends SimpleFileVisitor<Path> {
    private static final String TAG = "文件查询";
    public static Path home;
    private byte[] queryFileMD5;
    private long fileSize;
    private Path result = null;

    public FileQuery(byte[] queryFileMD5,long fileSize) {
        this.queryFileMD5 = queryFileMD5;
        this.fileSize = fileSize;
    }

    /**
     * 查询文件
     * @return
     */
    public Path queryFile() throws IOException {
        long time = System.currentTimeMillis();
        Path path = Files.walkFileTree(home, this);
        Log.i(TAG,path+" - 查询时间:"+ (System.currentTimeMillis() - time) +" 毫秒.");
        return result;
    }
    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return FileVisitResult.SKIP_SUBTREE;
    }
    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
            //比较md5
            if (filePath.toFile().length() == fileSize && equalsMD5(filePath.toFile(),queryFileMD5)){
                this.result = filePath;
                return FileVisitResult.TERMINATE;
            }
        return FileVisitResult.CONTINUE;
    }

    private boolean equalsMD5(File file, byte[] queryFileMD5) {
        try {
            byte[] fileMD5 = MD5Util.getFileMD5Bytes(file);
            return Arrays.equals(fileMD5,queryFileMD5);
        } catch (Exception e) {

        }
        return false;
    }
}
