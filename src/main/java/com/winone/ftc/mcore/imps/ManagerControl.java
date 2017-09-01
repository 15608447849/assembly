package com.winone.ftc.mcore.imps;

import com.winone.ftc.mcore.itface.Manager;
import com.winone.ftc.mentity.itface.Mftcs;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mtools.ClazzUtil;
import com.winone.ftc.mtools.StringUtil;

/**
 * Created by lzp on 2017/5/11.
 * 控制器
 */
public class ManagerControl implements Mftcs{

    private Manager manager;//管理器
    private String loaderClassName;//下载
    private String updaterClassName;//上传
    private Class[] clazz ;
    private Object[] param;
    public ManagerControl(Manager manager) {
        this.manager = manager;
        clazz = new Class[]{Mftcs.class};
        param = new Object[]{this};
    }

    protected void setLoader(String loader){
        this.loaderClassName = loader;
    }

    protected void setUpdateer(String updateer){
        this.updaterClassName = updateer;
    }

    public Manager getManager() {
        return manager;
    }

    public Mftcs getLoader() {
        return getImps(loaderClassName);
    }

    public Mftcs getUpdater() {
        return getImps(updaterClassName);
    }

    @Override
    public Task load(Task task) {
        return getTaskImps(task,getLoader(),"load");
    }

    @Override
    public Task upload(Task task) {
        return getTaskImps(task, getUpdater(),"upload");
    }

    @Override
    public Task finish(Task task) {
        if (getManager()!=null) getManager().finish(task);
        return null;
    }


    private Mftcs getImps(String clzzName){
        if (!StringUtil.isEntry(clzzName)){
            Object object = ClazzUtil.newInstance(clzzName,clazz,param);
            if (object!=null){
                return (Mftcs) object;
            }
        }
        return null;
    }

    private Task getTaskImps(Task task,Mftcs imps,String operation){
        if (task.getState() == Task.State.NEW){

            if (getManager()!=null && imps!=null && !StringUtil.isEntry(operation)){
                return getManager().putTask(task,() -> {
                    ClazzUtil.invokeMethod(imps,operation,new Class[]{Task.class},new Object[]{task});
                });

            }
        }
        return null;
    }
}
