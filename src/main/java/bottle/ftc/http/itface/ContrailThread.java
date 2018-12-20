package bottle.ftc.http.itface;

import java.util.concurrent.CountDownLatch;

/**
 * Created by lzp on 2017/5/10.
 * 控制线程
 */
public class ContrailThread extends Thread {

    private onAction onAction;
    private CountDownLatch cdl;
    public ContrailThread(ContrailThread.onAction onAction,CountDownLatch cdl) {
        this.setName("HTTP_CT_"+getId());
        this.setPriority(6);
        this.onAction = onAction;
        this.cdl = cdl;
        onAction.build(this);
    }


    @Override
    public void run() {
        try {
//            Log.i(this +" run!");
            onAction.action();
            mStop();
//            Log.i(this +" run over!");
        }catch (Exception e){
            onAction.error(e);
        }finally {
            onAction = null;
            cdl.countDown();
        }
    }

    public void mStop(){
        if (onAction!=null) onAction.stop();
    }

    public boolean isWork() {
        if (onAction!=null) return onAction.isWork();
        return false;
    }

    public interface onAction{
        void build(ContrailThread thread);
        void action() throws Exception;
        void stop();
        void error(Exception e);
        boolean isWork();
    }
}
