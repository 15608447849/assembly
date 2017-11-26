package m.tcps.p;


import com.winone.ftc.mtools.Log;
import sun.nio.ch.DirectBuffer;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;


/**
 * Created by user on 2017/7/8.
 * 通讯会话
 */
public abstract class Session implements CompletionHandler<Integer, ByteBuffer>{

    private final FtcTcpAioManager ftcTcpManager;
    private SocketImp socketImp;

    private final SessionContentStore sessionContentStore;//接受数据存储
    private final SessionOperation operation;
    private SessionContentHandle sessionHandle;
    private long send_sum,recv_sum;


    public Session(FtcTcpAioManager manager,SocketImp connect) {
        this.ftcTcpManager = manager;
        this.socketImp = connect;
        this.sessionContentStore = new SessionContentStore();
        this.operation = new SessionOperation(this);

    }
    //系统读取到信息 回调到这里
    @Override
    public void completed(Integer integer, ByteBuffer buffer) {
        recv_sum+=integer;
//        Log.println("总接收: "+ recv_sum+" - "+ buffer);
//        Log.println("接收: "+ integer+" - "+ buffer);
        if (!socketImp.isAlive()) return;
        if (integer == -1){
            SocketException socketException = new SocketException("socket connect is close.");
            socketImp.getAction().error(this,null,socketException);
            socketImp.ConnectError(socketException);
            return;
        }
        if (integer>0){
            if (sessionHandle==null){
                sessionHandle = new SessionContentHandle(this);
            }
            sessionContentStore.storeBuffer(buffer);

        }
        read();
    }

    @Override
    public void failed(Throwable throwable, ByteBuffer buffer) {
        socketImp.getAction().error(this,throwable,null);
        socketImp.ConnectError(throwable);
    }

    /**
     * 读取数据监听
     */
    public void read(){
        try {
            ByteBuffer buffer = sessionContentStore.getReadBufferBySystemTcpStack();
            if (buffer!=null && socketImp.isAlive()){
                socketImp.getSocket().read(buffer, buffer,this);//系统从管道读取数据
            }
        } catch (Exception e) {
            socketImp.getAction().error(this,null,e);
        }
    }

    /**
     * 发送数据
     */
    public void send(ByteBuffer buffer) {
            try {
                if (buffer!=null && socketImp.isAlive()){
                  buffer.flip();
                  Future<Integer> future =  socketImp.getSocket().write(buffer); //发送消息到管道
                    while(true){
                        if (future.isDone()){
//                            Log.println("send size : "+ future.get()+" , "+Thread.currentThread().getName());
                            send_sum+=future.get();
//                            Log.println("总发送 : "+ send_sum);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                socketImp.getAction().error(this,null,e);
            }
    }

    public void clear(){
        //清理 剩余保存的数据 ,清理队里中已存在的数据
        sessionContentStore.clear();
    }
    public void close(){
        //清理资源
        clear();
        //关闭处理读取消息的线程
        if (sessionHandle!=null){
            sessionHandle.close();
            sessionHandle=null;
        }

        //关闭管道
//        socketImp.close();
    }



    public SessionOperation getOperation(){return operation;}
    public AsynchronousSocketChannel getSocket(){
        return socketImp.getSocket();
    }
    public SocketImp getSocketImp(){
        return socketImp;
    }
    public SessionContentStore getStore(){return sessionContentStore;}
    public FtcTcpAioManager getFtcTcpAioManager(){
        return ftcTcpManager;
    }

}
