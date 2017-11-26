package m.tcps.p;

import com.winone.ftc.mtools.Log;

import java.io.UnsupportedEncodingException;
import static m.tcps.p.SessionContentStore.RECEIVE_STREAM;
import static m.tcps.p.SessionContentStore.RECEIVE_STRING;

/**
 * Created by user on 2017/11/23.
 */
class SessionContentHandle extends Thread{


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
                byte[] bytes = session.getStore().takeBuffer();
//                Log.println("获取到数据: " +bytes);
                if (bytes!=null){
//                    Log.println("获取到数据: " +bytes.length);
                    handlerBuffer(bytes);
                }
        }
    }




    private void handlerBuffer(byte[] data) {
        //数据拼接
        byte[] bytes = dataMosaic(data);
        //数据处理
        dataHandle(bytes);
    }

    /**
     * 数据拼接
     * @return
     */
    private byte[] dataMosaic(byte[] data) {
//        Log.println("取出 : "+ buf);
        // 1 是否还有上一次剩余未处理的数据 , 有 - 拼接在此段数据前
        byte[] oldBytes = session.getStore().getRamContent();//获取剩余数据,清空剩余数据
        if (oldBytes!=null){
//            Log.println("拼接的数据 - 大小: "+ oldBuf.limit());
            byte[] streamData = new byte[data.length+oldBytes.length];
            //1.要拷贝复制的原始数据
            //2.原始数据的读取位置(从原始数据哪个位置开始拷贝)
            //3.存放要拷贝的原始数据的目的地
            //4.开始存放的位置()
            //5.要读取的原始数据长度(拷贝多长)
            System.arraycopy(oldBytes,0,streamData,0,oldBytes.length);
            System.arraycopy(data,0,streamData,oldBytes.length,data.length);
            data = streamData;
        }
        return data;
    }

    /**
     * 数据处理
     */
    private void dataHandle(byte[] bytes){
//        Log.println("处理数据: "+ bytes.length);
        //判断是否在收集指定数据中
        int collect = session.getStore().toBeCollectedSize();
//        Log.println("是否需要收集的数据 : "+ collect);
        if (collect>0){
            //进行数据的收集
            dataCollected(bytes,0,bytes.length);
        }else{
            //判断数据是否大于协议规定长度(8字节)
            dataHandle2(bytes,0,bytes.length);
        }
    }

    private void dataHandle2(byte[] bytes, int offset, int length) {
//        Log.println("根据数据 - 处理协议体 :  "+offset+" - "+ (offset+length) +" , size: "+ length);
        if (length>8){
            int _offset =  protocolHandler(bytes,offset,8);//处理协议
            if (_offset>0){
                dataCollected(bytes,_offset,length-8);
            }else{
//                Log.println("数据收集错误......................................................................");
                throw new IllegalStateException("bytes read protocol is error.");
            }
        }else{
            //存入剩余数据
            dataRemainingStored(bytes,offset,length);
        }
    }

    /**
     * 协议处理 - 返回数据起点
     */
    private int protocolHandler(byte[] bytes, int offset, int length) {
        if (bytes[offset] == Protocol.NUL &&  bytes[offset+1] == Protocol.ENQ && bytes[offset] == bytes[offset+2]){
                int contentLength = Protocol.byteArrayToInt(bytes,offset+4);
                //请求
                byte aByte = bytes[offset+3];
                SessionContentStore store = session.getStore();
                if (aByte == Protocol.STX){
                    //字符编码
                    store.setContent_type(SessionContentStore.RECEIVE_CHARSET);
                    store.setCollectedSize(contentLength);
                    return offset+length;
                }else if (aByte == Protocol.ETX){
                    //字符串
                    store.setContent_type(SessionContentStore.RECEIVE_STRING);
                    store.setCollectedSize(contentLength);
                    return offset+length;
                }else if (aByte == Protocol.EOT){
                    //数据流
                    store.setContent_type(SessionContentStore.RECEIVE_STREAM);
                    store.setCollectedSize(contentLength);
                    return offset+length;
                }
        }
        return -1; // 错误的协议体 暂时抛出异常, 以后进行滚动检测

    }


    /**
     * 数据采集
     * @param bytes
     */
    private void dataCollected(byte[] bytes,int offset, int length) {
        //判断可收集数据是否大于指定长度
        int collect = session.getStore().toBeCollectedSize();
//        Log.println("收集数据 -  "+ collect+" , 当前数据:"+offset+" - "+ (offset+length)+" ,size:"+ length);
        if (length>=collect){
            //可收集到完整数据
            byte[] data = new byte[collect];
            //1.要拷贝复制的原始数据
            //2.原始数据的读取位置(从原始数据哪个位置开始拷贝)
            //3.存放要拷贝的原始数据的目的地
            //4.开始存放的位置()
            //5.要读取的原始数据长度(拷贝多长)
            System.arraycopy(bytes,offset,data,0,collect);
            //通知
            notify(data);
            if (length-collect > 0){
                //此数据还有剩余
//                Log.println("存在剩余数据 大小: "+(length-collect) );
                dataHandle2(bytes,collect+offset,length-collect);
            }
        }else{
            //此段数据过短 - 存储
            dataRemainingStored(bytes,offset,length);
        }

    }

    private void notify(byte[] data) {
        SocketImp socketImp = session.getSocketImp();
        if (socketImp!=null && socketImp.getAction()!=null){
            int type = session.getStore().getContent_type();
            if(type == SessionContentStore.RECEIVE_CHARSET){ //字符编码数据接收成功
                charsetHandle(data);
            }else if (type == RECEIVE_STRING){//字符串接收成功
                stringContentHandle(data,socketImp.getAction());
            }else if (type==RECEIVE_STREAM){//数据流接收成功
                streamHandle(data,socketImp.getAction());
            }
            //复位
            session.getStore().reset();
        }
    }
    //处理字符编码
    private void charsetHandle(byte[] bytes) {
        charset = Protocol.asciiToString(bytes);
    }
    //处理字符内容
    private void stringContentHandle(byte[] bytes, FtcTcpActions communication) {
        String message;
        try {
            message = new String(bytes,charset);
        } catch (UnsupportedEncodingException e) {
            communication.error(session,e.getCause(),e);
            message = new String(bytes);
        }
        communication.receiveString(session,message);
    }

    //处理流
    private void streamHandle(byte[] bytes, FtcTcpActions communication) {
        communication.receiveBytes(session,bytes);
    }
    /**
     * 存入剩余数据
     */
    private void dataRemainingStored(byte[] bytes, int offset, int length) {
//        Log.println("存储剩余字节长度: "+ length +" , 起点:"+offset);
        session.getStore().storeRemainBytes(bytes,offset,length);
    }
    public void close() {
        isFlag = false;
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