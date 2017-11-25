package m.tcps.p;

import m.bytebuffs.FtcBuffer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static m.tcps.p.SessionContentStore.RECEIVE_STREAM;
import static m.tcps.p.SessionContentStore.RECEIVE_STRING;

/**
 * Created by user on 2017/11/23.
 */
public class SessionContentHandle extends Thread{


    private final Session session;
    private String charset;
    private boolean isFlag = true;
    public SessionContentHandle(Session session) {
        this.session = session;
        this.start();
    }



    @Override
    public void run() {
        while (isFlag){
            SessionContentStore store = session.getStore();
            if (store!=null){
                FtcBuffer buffer  = store.takeBuffer();
                if (buffer!=null){
                    handlerBuffer(buffer);
                }
            }
        }
    }



    public void handle(SessionContentStore sessionContentBean, int type){
        SocketImp socketImp = session.getSocketImp();
        if (socketImp!=null && socketImp.getAction()!=null){
            if(type == SessionContentStore.RECEIVE_CHARSET){ //字符编码数据接收成功
                charsetHandle(sessionContentBean);
            }else if (type == RECEIVE_STRING){//字符串接收成功
                stringContentHandle(sessionContentBean,socketImp.getAction());
            }else if (type==RECEIVE_STREAM){//数据流接收成功
                streamHandle(sessionContentBean,socketImp.getAction());
            }
        }
    }




    //处理字符编码
    private void charsetHandle(SessionContentStore sessionContentBean) {
        byte[] bytes = sessionContentBean.getBytes();
        if (bytes!=null){
            charset = Protocol.asciiToString(bytes);
        }
    }
    //处理字符内容
    private void stringContentHandle(SessionContentStore sessionContentBean, FtcTcpActions communication) {
        byte[] bytes = sessionContentBean.getBytes();
        if (bytes!=null){
                String message;
                try {
                    message = new String(bytes,charset);
                } catch (UnsupportedEncodingException e) {
                    communication.error(session,e.getCause(),e);
                    message = new String(bytes);
                }
                communication.receiveString(session,message);
        }
    }

    //处理流
    private void streamHandle(SessionContentStore sessionContentBean, FtcTcpActions communication) {
        byte[] bytes = sessionContentBean.getBytes();
        if (bytes!=null){
            communication.receiveBytes(session,bytes);
        }
    }


    private boolean checkProtocolType(byte[] protocol_bytes,SessionContentStore sessionContentBean) {

        if (protocol_bytes[0] == protocol_bytes[2] &&  protocol_bytes[2] == Protocol.NUL){
            if (protocol_bytes[1] == Protocol.ENQ){
                int contentLength = Protocol.byteArrayToInt(protocol_bytes,4);
                //请求
                if (protocol_bytes[3] == Protocol.STX){
                    //字符编码
                    sessionContentBean.setContent_type(SessionContentStore.RECEIVE_CHARSET);
                    sessionContentBean.setContent_length(contentLength);
                    return true;
                }else if (protocol_bytes[3] == Protocol.ETX){
                    //字符串
                    sessionContentBean.setContent_type(SessionContentStore.RECEIVE_STRING);
                    sessionContentBean.setContent_length(contentLength);
                    return true;
                }else if (protocol_bytes[3] == Protocol.EOT){
                    //数据流
                    sessionContentBean.setContent_type(SessionContentStore.RECEIVE_STREAM);
                    sessionContentBean.setContent_length(contentLength);
                    return true;
                }
            }
        }
        return false;
    }
    //读取
    // 取出4位  判断是否是协议头
    // 如果是协议头 ...
    // 取出后面 4位数 - 得到内容长度
    public void handlerProtocol(Integer integer, SessionContentStore sessionContentBean){
        if (integer>0){

            ByteBuffer buf = sessionContentBean.getReadBuffer().flipBuf();
            if (integer>8){
                //取出前8位数
                byte[] protocol_bytes = new byte[8];
                for (int i = 0;i<protocol_bytes.length;i++){
                    protocol_bytes[i] = buf.get();
                }
                //判断协议并设置可接受内容的长度
                boolean flag = checkProtocolType(protocol_bytes,sessionContentBean);
                if (!flag){
                    //不存在 - 直接添加数据
                    for (int i=0;i<protocol_bytes.length;i++){
                        sessionContentBean.storeBytes(protocol_bytes[i]);
                    }
                }
            }
            while(buf.hasRemaining()){
                sessionContentBean.storeBytes(buf.get());
            }
        }
    }

    private void handlerBuffer(FtcBuffer buffer) {
        ByteBuffer buf = buffer.flipBuf();
        int size = buf.limit();
        // 1 确顶是否还有上一次剩余未处理的数据 , 有 - 拼接在此段数据前
        ByteBuffer oldBuf = session.getStore().getRamContent();//获取剩余数据,flip , 删除Buffer
        if (oldBuf.limit()>0){
            size += oldBuf.limit();
        }
        byte[] streamData = new byte[size];
        int index = 0;
        while(oldBuf.hasRemaining()){
            streamData[index] = oldBuf.get();
            index++;
        }

        while (buf.hasRemaining()){
            streamData[index] = buf.get();
            index++;
        }
        // 2 确定当前存储中没有相关协议正在收集, 有- 进行存储 ,没有 进行数据前八位的协议分析
        int collect = session.getStore().toBeCollectedSize();
        if ( collect == 0){ //没有待收集数据
            //协议分析

        }else{
            //放入这么多数据
            index = 0;
            int limit = Math.min(streamData.length,collect);
            while(index<limit){
                session.getStore().storeBytes(streamData[index]);
                index++;
            }
            while(index<streamData.length){
                //还有一部分剩余数据
                session.getStore().storeBytesByRam(streamData[index]);
                index++;
            }


        }




    }


}


/**
 if (sessionContentBean.getContent_type() == 1){
 //                                Log.i("BUFFER: "+ data);
 //发送字符串到回调
 socketImp.getCommunication().receiveString(this, new String(data.array(), "UTF-8"));
 }else if(sessionContentBean.getContent_type() == 2){
 //发送数据流到回调
 socketImp.getCommunication().receiveStream(this, data.array());
 }*/