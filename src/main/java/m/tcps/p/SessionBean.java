package m.tcps.p;

import m.bytebuffs.MyBuffer;
import m.bytebuffs.MyBufferPool;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;


/**
 * Created by user on 2017/7/8.
 * 存在 内容缓存区buf
 */
public class SessionBean {
    /**
     * 1 读取
     * 2 写入
     */
    private int type;

    public SessionBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    private SoftReference<MyBuffer> bufferSoftReference = null;

    public MyBuffer getReadBuffer() {
        if (bufferSoftReference==null) {
            MyBuffer buffer = MyBufferPool.get().getBuffer();
            buffer.setTag(1);
            bufferSoftReference = new SoftReference(buffer);
        }
        return bufferSoftReference.get();
    }


    public MyBuffer getWriteBuffer(int length) {
        if (bufferSoftReference==null || bufferSoftReference.get().getBuf()==null) {
            MyBuffer buffer = MyBufferPool.get().getBuffer(length);
            bufferSoftReference = new SoftReference(buffer);
        }
        return bufferSoftReference.get();
    }
    public MyBuffer getWriteBuffer() {
        if (bufferSoftReference!=null && bufferSoftReference.get().getBuf()!=null) {
            return bufferSoftReference.get();
        }
       return null;
    }

    private StringBuffer stringBuffer;

    public StringBuffer getReadContent() {
        if (stringBuffer==null) stringBuffer = new StringBuffer();
        return stringBuffer;
    }


    public void clearData(){
        content_type = 0;
        if (stringBuffer!=null) {
            stringBuffer.delete(0,stringBuffer.length());
            stringBuffer = null;
        }
        if (dataByteBuffer!=null) {
            dataByteBuffer.clear();
            dataByteBuffer = null;
        }
    }

    private int content_type = 0; //1 字符串

    public int getContent_type() {
        return content_type;
    }

    public void setContent_type(int content_type) {
        this.content_type = content_type;
    }

    private ByteBuffer dataByteBuffer;

    public void addDatas(byte b){
        if (dataByteBuffer==null){
            dataByteBuffer=ByteBuffer.allocate(1024);
            dataByteBuffer.clear();
        }

        if (!dataByteBuffer.hasRemaining()){
            ByteBuffer temp = ByteBuffer.allocate(dataByteBuffer.capacity()+1024);
            temp.clear();
            temp.put(dataByteBuffer.array());
            dataByteBuffer = temp;
        }
        dataByteBuffer.put(b); //存储
    }
    public ByteBuffer getDatas(){
        if (dataByteBuffer!=null) dataByteBuffer.flip();
        ByteBuffer temp = ByteBuffer.allocate(dataByteBuffer.limit());
        temp.put(dataByteBuffer.array(),0,dataByteBuffer.limit());
        temp.flip();
        dataByteBuffer = temp;
        return dataByteBuffer;
    }

    public void close() {
        //清理数据
        clearData();
        if (bufferSoftReference!=null){
            if (bufferSoftReference.get()!=null){
                MyBufferPool.get().clear(bufferSoftReference.get());
            }
        }
    }
}
