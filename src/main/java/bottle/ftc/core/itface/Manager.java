package bottle.ftc.core.itface;

import bottle.ftc.entity.itface.MRun;
import bottle.ftc.entity.itface.Mftcs;
import bottle.ftc.entity.mbean.entity.Task;

/**
 * Created by lzp on 2017/5/8.
 *
 */
public interface Manager extends Mftcs {
    Task putTask(Task task, MRun runnable);
    Task executeTask(Task task);
    void removeTask(Task task);
    void released();//释放全部
    int getCurrentTaskSize();
}
