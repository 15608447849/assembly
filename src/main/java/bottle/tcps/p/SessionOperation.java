package bottle.tcps.p;

import bottle.ftc.tools.Log;
import bottle.ftc.tools.StringUtil;


import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2017/11/22.
 */
public class SessionOperation {

    private final Session session ;

    public SessionOperation(Session session) {
        this.session = session;
    }

    /**
     *
     * @param message 消息
     * @param message 编码格式
     *
     * 协议: NUL + ENQ + NUL + STX + 字符编码数据长度 + 字符编码数据
     * 协议: NUL + ENQ + NUL + ETX + 字符串数据长度 + 字符串数据
     *
     */
    public void writeString(String message,String charset){


        charset = StringUtil.isEntry(charset)?"utf-8":charset;

        byte[] data;
        try {
            data = message.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            writeString(message,"uft-8");
            return;
        }

        byte[] charsetBytes_ascii = Protocol.stringToAscii(charset);
        if(data.length == 0) return;
//        Log.i("发送字符串: "+ message+" 编码方式" + charset+" 字节长度"+data.length);
        ByteBuffer buf = session.getStore().getSendBufferBySystemTcpStack(data.length);//清空
        Protocol.protocol(buf,Protocol.STX,charsetBytes_ascii.length);
        buf.put(charsetBytes_ascii);
        session.send(buf);
        buf = session.getStore().getSendBufferBySystemTcpStack(data.length);//清空
        Protocol.protocol(buf,Protocol.ETX,data.length);
        buf.put(data);
        //发送消息
        session.send(buf);
    }
    /**
     * @param bytes 字节数组
     * 协议: NUL +请求(ENQ)+ NUL + EOT + length +  数据
     */
    public void writeBytes(byte[] bytes,int offset,int length){ //偏移量,长度
        ByteBuffer buf = session.getStore().getSendBufferBySystemTcpStack(length);

        int capacity = buf.capacity()-8;

        if(length > capacity){ //切分数据

            int sliceSum = length / capacity; //取整
            int mod = length % capacity;  //取模
//            Log.i("切割数据 : "+ offset+" - "+ (offset+length)+" , 长度:"+ length+" ,切分片段数:"+sliceSum+" 剩余:"+mod);
            for (int i=0;i<sliceSum;i++){
//                    Log.i("           "+ (offset+(i*capacity))+" - "+ (offset+(i*capacity)+capacity));
                writeBytes(bytes,offset+(i*capacity),capacity);
            }
            if (mod>0){
                writeBytes(bytes,offset+(sliceSum*capacity),mod);
            }
            return;
        }
        Protocol.protocol(buf,Protocol.EOT,length);
        buf.put(bytes,offset,length);
        session.send(buf);
    }
}
