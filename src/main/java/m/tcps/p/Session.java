package m.tcps.p;

import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.StringUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;

/**
 * Created by user on 2017/7/8.
 * 通讯会话
 */
public abstract class Session implements CompletionHandler<Integer, SessionBean>,Op{

    private SocketImp socketImp;
    private volatile  boolean isWriteable = true;
    public Session(SocketImp connect) {
        this.socketImp = connect;
    }

    @Override
    public void completed(Integer integer, SessionBean sessionBean) {

        if (!socketImp.isAlive()) return;
        if (integer == -1){
            socketImp.ConnectError(new Exception("socket connect is close."));
            return;
        }
        if (sessionBean.getType() == 1){
           readMessage(integer,sessionBean);
        }
        if (sessionBean.getType() == 2){
            //写入 不实现
//            Log.i("写入: " + integer);
            isWriteable = true;
            sessionBean.getWriteBuffer().getBuf().clear();
        }
    }

    protected void readMessage(Integer integer, SessionBean sessionBean){

        //读取
        if (integer>0){
            try {
                ByteBuffer buf = sessionBean.getReadBuffer().flipBuf();
                byte c;
                while (buf.hasRemaining()){
                    c = buf.get();
                    if (c == Protocol.STRING){
                        sessionBean.setContent_type(1);
                    }else if (c == Protocol.DATA_END){//  \r回车(发送数据)
                        if (socketImp.getCommunication()!=null){
                            if (sessionBean.getContent_type() == 1){
                                ByteBuffer data = sessionBean.getDatas();
//                                Log.i("BUFFER: "+ data);
                                //发送字符串到回调
                                socketImp.getCommunication().receiveString(this,this,new String(data.array(),"UTF-8"));
                            }

                        }
                    }else if (c == Protocol.DATA_CLEAR){//  \n换行(清理)
                        //清空
                        sessionBean.clearData();
                    }else{
                        sessionBean.addDatas(c);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //再次监听读取
        read(sessionBean);
    }

    @Override
    public void failed(Throwable throwable, SessionBean sessionBean) {
        socketImp.ConnectError(throwable);
    }

    /**
     * 写入String信息到对方
     * @param message
     */
    @Override
    public void writeString(String message) {

        while (!isWriteable);

        assert message!=null;
        byte[] data;
        try {
            data = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }

        SessionBean sessionBean = new SessionBean(2);
        ByteBuffer buf = sessionBean.getWriteBuffer(data.length+3).clearBuf();//清空
        buf.put(Protocol.STRING);
        for (int i=0;i<data.length;i++){
            buf.put(data[i]);
        }
        buf.put(Protocol.DATA_END);
        buf.put(Protocol.DATA_CLEAR);
        //发送消息
        send(sessionBean);
    }


    /**
     * 读取数据监听
     * @param sessionBean
     */
    public void read(SessionBean sessionBean){
        ByteBuffer buffer = sessionBean.getReadBuffer().compactBuf();
        socketImp.getSocket().read( buffer, sessionBean,this);
    }
    /**
     * 发送数据监听
     */
    public void send(SessionBean sessionBean) {
        ByteBuffer buffer = sessionBean.getWriteBuffer().flipBuf();
        if (buffer==null) return;
        socketImp.getSocket().write(buffer,sessionBean,this); //发送消息并且重置
        isWriteable = false;
    }
    public void close(){
        socketImp=null;
    }
    public Op getOp(){
        return this;
    }
    public AsynchronousSocketChannel getSocket(){
        return socketImp.getSocket();
    }
}
