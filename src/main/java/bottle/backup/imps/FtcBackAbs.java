package bottle.backup.imps;

import bottle.ftc.tools.FileUtil;

import java.io.File;

/**
 * Created by user on 2017/11/23.
 */
public abstract class FtcBackAbs {
    //目录
    protected final String directory ;
    public FtcBackAbs(String directory) {
        this.directory = FileUtil.replaceFileSeparatorAndCheck(directory,null, File.pathSeparator);
    }

    public String getDirectory() {
        return directory;
    }
}
