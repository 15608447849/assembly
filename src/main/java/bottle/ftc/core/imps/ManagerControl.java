package bottle.ftc.core.imps;

import bottle.ftc.core.itface.Manager;
import bottle.ftc.entity.itface.Mftcs;
import bottle.ftc.entity.mbean.entity.Task;
import bottle.ftc.tools.ClazzUtil;
import bottle.ftc.tools.StringUtil;

/**
 * Created by lzp on 2017/5/11.
 * 控制器
 */
public class ManagerControl implements Mftcs {

    private Manager manager;//管理器
    private String loaderClassName;//下载
    private String uploaderClassName;//上传
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

    protected void setUploader(String uploader){
        this.uploaderClassName = uploader;
    }

    public Manager getManager() {
        return manager;
    }

    public Mftcs getLoader() {
        return getImps(loaderClassName);
    }

    public Mftcs getUploader() {
        return getImps(uploaderClassName);
    }

    @Override
    public Task load(Task task) {
        return getTaskImps(task,getLoader(),"load");
    }

    @Override
    public Task upload(Task task) {
        return getTaskImps(task, getUploader(),"upload");
    }

    @Override
    public Task finish(Task task) {
        if (getManager()!=null) getManager().finish(task);
        return null;
    }


    private Mftcs getImps(String clazzName){
        if (!StringUtil.isEntry(clazzName)){
            Object object = ClazzUtil.newInstance(clazzName,clazz,param);
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
