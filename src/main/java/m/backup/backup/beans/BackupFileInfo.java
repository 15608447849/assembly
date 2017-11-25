package m.backup.backup.beans;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Created by user on 2017/11/24.
 */
public class BackupFileInfo {
    private String dirs;
    private String rel_path;
    private String fileName;


    public BackupFileInfo(String dirs,String file) {
        this.dirs = FileUtil.replaceFileSeparatorAndCheck(dirs,null,FileUtil.SEPARATOR);
        file = FileUtil.SEPARATOR+FileUtil.replaceFileSeparatorAndCheck(file,FileUtil.SEPARATOR,null);
        this.rel_path = file.substring(0,file.lastIndexOf(FileUtil.SEPARATOR)+1);
        this.fileName = file.substring(file.lastIndexOf(FileUtil.SEPARATOR)+1);

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


    @Override
    public String toString() {
        return "BackupFileInfo{" +
                "dirs='" + dirs + '\'' +
                ", rel_path='" + rel_path + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public void clear() {
        //关闭本地文件流等 清理任务
        if (randomAccessFile!=null){
            try {
                randomAccessFile.close();
                randomAccessFile=null;
            } catch (IOException e) {
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
    public RandomAccessFile getRandomAccessFile() throws IOException {
        if (randomAccessFile==null) randomAccessFile = new RandomAccessFile(getFullPath(),"r");
        return randomAccessFile;
    }
}
