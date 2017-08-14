package com.winone.ftc.mcore.itface;

import com.winone.ftc.mentity.itface.Mftcs;

import com.winone.ftc.mentity.mbean.Task;

/**
 * Created by lzp on 2017/5/11.
 * 执行者
 */
public abstract class Excute implements Mftcs{
    public Mftcs manager;
    public Excute(Mftcs manager) {
        this.manager = manager;
    }
    @Override
    public Task finish(Task task) {
        if (manager!=null) manager.finish(task);
        return null;
    }
}
