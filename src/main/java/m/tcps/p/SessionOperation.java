package m.tcps.p;

import com.winone.ftc.mtools.StringUtil;

import java.io.IOException;
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
    public void writeString(String message,String charset) throws IOException {
        charset = StringUtil.isEntry(charset)?"utf-8":charset;
        byte[] charsetBytes_ascii = Protocol.stringToAscii(charset);
        byte[] data = message.getBytes(charset);
        if(data==null || data.length==0) return;
        SessionContentStore sessionBean = new SessionContentStore(SessionContentStore.TYPE_WRITE);
        ByteBuffer buf = sessionBean.getWriteBuffer(8+charsetBytes_ascii.length).clearBuf();//清空
        Protocol.protocol(buf,Protocol.STX,charsetBytes_ascii.length);
        for(int i=0;i<charsetBytes_ascii.length;i++){
            buf.put(charsetBytes_ascii[i]);
        }
        session.send(sessionBean);

        sessionBean = new SessionContentStore(SessionContentStore.TYPE_WRITE);
        buf = sessionBean.getWriteBuffer(8+data.length).clearBuf();//清空
        Protocol.protocol(buf,Protocol.ETX,data.length);
        for (int i=0;i<data.length;i++){
            buf.put(data[i]);
        }
        //发送消息
        session.send(sessionBean);
    }
    /**
     * @param bytes 字节数组
     * 协议: NUL +请求(ENQ)+ NUL + EOT + length +  数据
     */
    public void writeBytes(byte[] bytes,int offset,int length) throws IOException{
        SessionContentStore sessionBean = new SessionContentStore(SessionContentStore.TYPE_WRITE);
        ByteBuffer buf = sessionBean.getWriteBuffer(8+bytes.length).clearBuf();
        Protocol.protocol(buf,Protocol.EOT,length);
        for(int i=offset;i<length;i++){
            buf.put(bytes[i]);
        }
        session.send(sessionBean);
    }
}
