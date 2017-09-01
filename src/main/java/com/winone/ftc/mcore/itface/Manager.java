package com.winone.ftc.mcore.itface;

import com.winone.ftc.mentity.itface.MRun;
import com.winone.ftc.mentity.itface.Mftcs;
import com.winone.ftc.mentity.mbean.entity.Task;

/**
 * Created by lzp on 2017/5/8.
 *
 */
public interface Manager extends Mftcs {
    Task putTask(Task task,MRun runnable);
    Task executeTask(Task task);
    void removeTask(Task task);
    void released();//释放全部
    int getCurrentTaskSize();
}
