package bottle.backup.server;

import java.io.File;

public interface Callback{
    void complete(File file);
}
