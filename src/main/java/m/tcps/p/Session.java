package m.tcps.p;

import com.winone.ftc.mtools.Log;
import m.bytebuffs.FtcBuffer;
import m.bytebuffs.FtcBufferPool;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;



/**
 * Created by user on 2017/7/8.
 * 通讯会话
 */
public abstract class Session implements CompletionHandler<Integer, FtcBuffer>{

    private final FtcTcpAioManager ftcTcpManager;
    private SocketImp socketImp;
    private final SessionOperation operation;
    private final SessionContentHandle sessionHandle;
    private final SessionContentStore sessionContentStore;//接受数据存储 -
    private long send_sum,recv_sum;
    private static final int BUFFER_SIZE = 1024*16;//缓冲区大小

    public Session(FtcTcpAioManager manager,SocketImp connect) {
        this.ftcTcpManager = manager;
        this.socketImp = connect;
        this.operation = new SessionOperation(this);
        this.sessionHandle = new SessionContentHandle(this);
        this.sessionContentStore = new SessionContentStore(this);
    }
    //系统读取到信息 回调到这里
    @Override
    public void completed(Integer integer, FtcBuffer buffer) {
        recv_sum+=integer;
        Log.println("总接收: "+ recv_sum);
        if (!socketImp.isAlive()) return;
        if (integer == -1){
            SocketException socketException = new SocketException("socket connect is close.");
            socketImp.getAction().error(this,null,socketException);
            socketImp.ConnectError(socketException);
            return;
        }
        if (integer>0){
            sessionContentStore.storeBuffer(buffer);
        }
        read();
    }

    @Override
    public void failed(Throwable throwable, FtcBuffer buffer) {
        socketImp.getAction().error(this,throwable,null);
        socketImp.ConnectError(throwable);
    }

    /**
     * 读取数据监听
     */
    public void read(){
        try {
            FtcBuffer buffer = FtcBufferPool.get().getBuffer(BUFFER_SIZE);
            if (buffer!=null && socketImp.isAlive()){
                socketImp.getSocket().read( buffer.clearBuf(), buffer,this);//系统从管道读取数据
            }
        } catch (Exception e) {
            socketImp.getAction().error(this,null,e);
        }
    }
    /**
     * 发送数据
     */
    public void send(FtcBuffer buffer) {
            try {
                if (buffer!=null && socketImp.isAlive()){
                  Future<Integer> future =  socketImp.getSocket().write(buffer.flipBuf()); //发送消息到管道
                    while(true){
                        if (future.isDone()){
//                            Log.println("send size : "+ future.get()+" , "+Thread.currentThread().getName());
                            send_sum+=future.get();
                            Log.println("总发送 : "+ send_sum);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                socketImp.getAction().error(this,null,e);
            }

    }
    public void close(){
        //清理资源

        socketImp.close();
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
