package bottle.ftc.ftp.ftps;

import bottle.ftc.ftp.itface.FtpClientIntface;
import bottle.ftc.ftp.itface.FtpManager;
import bottle.ftc.entity.mbean.entity.FtpInfo;
import bottle.ftc.entity.mbean.entity.State;
import bottle.ftc.entity.mbean.entity.Task;
import bottle.ftc.tools.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lzp on 2017/5/11.
 * 维护ftp客户端列表
 *
 */
public class MFtpManager extends ArrayList implements FtpManager,Runnable {

    private static final String TAG = "FTP客户端管理列表";
    private ReentrantLock lock = new ReentrantLock();
    private static class InstantHolder{
        private static MFtpManager instants = new MFtpManager();
    }
    private MFtpManager() {
        initparam();
//        Log.i(TAG, "创建成功");
    }
    public static MFtpManager getInstants(){
        return InstantHolder.instants;
    }

    private int time = 30 * 1000;
    private Thread mThread;
    private void initparam() {
        if (mThread==null) {
            mThread = new Thread(this);
            mThread.start(); //启动
        }
    }
    @Override
    public FtpClientIntface getClient(Task task) {
        try{
            lock.lock();
            FtpInfo info = task.getFtpInfo();
            if (info==null){
                task.getExistState().setError(State.ErrorCode.ERROR_BY_FTP_CLIENT,"FTP服务器信息获取失败.");
                return null;
            }
            //先去列表查看
            Iterator<FtpClientIntface>  itr = iterator();
            FtpClientIntface client = null;
            while (itr.hasNext()){
                client = itr.next();
                if (client.isUsable() && client.check(info)){
                    //可用 并且是 同一个 ftp客户端
                    break;
                }
                client = null;
            }
            if (client == null){
                client = new MFtp(info);
                add(client);
                Log.i(TAG, "新建客户端 : "+ client);
                if (!client.connect() || !client.login()){ //连接-登录
                    task.getExistState().setError(State.ErrorCode.PERMISSION_DENIED,"FTP服务器连接或者登陆失败,FTP信息: "+ info);
                    return null;
                }
            }
            client.inUse();//设置为使用中
            return client;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void backClient(FtpClientIntface client) {
        client.notUsed();//设置未使用
    }

    @Override
    public void backClienOnError(FtpClientIntface client) {
        client.isError();//设置错误
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(time);
                //检测FTP客户端 是否正在使用中 - 1分钟一次
                check();
            }catch (Exception e){
                //断开所有的ftp连接
            }
        }
    }
    //检测
    private void check() {
        try{
            lock.lock();
            if (size() > 0){
                Iterator<FtpClientIntface> itr = iterator();
                FtpClientIntface client;
                while (itr.hasNext()){
                    client = itr.next();
                    if (client.isNotUsed()){
                        itr.remove();
                        client.logout();//登出
                        client.disconnect();//断开
                        Log.e(TAG, "移除客户端 : "+ client);
                    }
                }
            }
        }finally {
            lock.unlock();
        }
    }


}
