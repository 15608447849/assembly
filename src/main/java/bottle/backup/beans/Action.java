package bottle.backup.beans;

/**
 * Created by user on 2017/11/27.
 */
public interface Action<T> {
    void call(T t);
}
