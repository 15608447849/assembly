package bottle.ftc.entity.itface;

import bottle.ftc.entity.mbean.entity.Task;

/**
 * Created by Administrator on 2017/5/8.
 */
public interface Mftcs {
    Task load(Task task);
    Task upload(Task task);
    Task finish(Task task);
}
