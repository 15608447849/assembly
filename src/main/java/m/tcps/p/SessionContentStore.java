package m.tcps.p;

import com.winone.ftc.mtools.Log;
import m.bytebuffs.FtcBuffer;
import m.bytebuffs.FtcBufferPool;

import java.lang.ref.SoftReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;


/**
 * Created by user on 2017/7/8.
 * 存在 内容缓存区buf
 */
public class SessionContentStore {

    private final Session  session;
    public SessionContentStore(Session session) {
        this.session = session;
    }
   /* private SoftReference<FtcBuffer> bufferSoftReference = null;

    public FtcBuffer getReadBuffer() {
        if (bufferSoftReference==null) {
            FtcBuffer buffer = FtcBufferPool.get().getBuffer();
            buffer.setTag(1); //不可以被清理
            bufferSoftReference = new SoftReference(buffer);
        }
        return bufferSoftReference.get();
    }


    public FtcBuffer getWriteBuffer(int length) {
        if (bufferSoftReference==null || bufferSoftReference.get().getBuf()==null) {
            FtcBuffer buffer = FtcBufferPool.get().getBuffer(length);
            bufferSoftReference = new SoftReference(buffer);
        }
        return bufferSoftReference.get();
    }
    public FtcBuffer getWriteBuffer() {
        if (bufferSoftReference!=null && bufferSoftReference.get().getBuf()!=null) {
            return bufferSoftReference.get();
        }
       return null;
    }*/





    public static final int RECEIVE_NODE = 0;//不存储任何东西
    public static final int RECEIVE_CHARSET = 1; //存储字符编码byte[]
    public static final int RECEIVE_STRING = 2; //存储字符串内容byte[]
    public static final int RECEIVE_STREAM = 3;//接受字节流
    private int content_type = RECEIVE_NODE;

    public int getContent_type() {
        return content_type;
    }

    public void setContent_type(int content_type) {
        this.content_type = content_type;
    }

    private ByteBuffer dataByteBuffer;
    private static final int BUFFER_SIZE = 1024;

    private SessionContentHandle handle;

    private long dp;
    //存储数据
    public void storeBytes(byte b){
        if (content_type==RECEIVE_CHARSET || content_type==RECEIVE_STRING || content_type==RECEIVE_STREAM){
            //可以接收数据
            //判断当前已存数据是否到达指定长度
            if (content_length>0 && c_content_index<content_length) {
                if (dataByteBuffer==null){ //如果没有缓冲区, 创建
                    dataByteBuffer=ByteBuffer.allocate(BUFFER_SIZE);
                    dataByteBuffer.clear();
                }
                if (!dataByteBuffer.hasRemaining()){ //如果没有剩余 ,扩容
                    ByteBuffer temp = ByteBuffer.allocate(dataByteBuffer.capacity()+BUFFER_SIZE);
                    temp.clear();
                    dataByteBuffer.flip();
                    //数据转移
                    while (dataByteBuffer.hasRemaining()){
                        temp.put(dataByteBuffer.get());
                    }
                    dataByteBuffer = temp;
                }
                dataByteBuffer.put(b); //存储
                c_content_index++;//下标后移
                if (c_content_index==content_length){
                    //如果数据达标->通知
                    if (handle!=null){
                        //假设当前是 接受字符串 - > 通知处理
                        handle.handle(this,content_type);
                    }
                }
            }else{
                dp++;
                Log.println("超过指定长度, 丢弃 - ",dp);
            }
        }else{
            dp++;
            Log.println("不在指定协议内, 丢弃 - ",dp);
        }
    }
    //清理缓存数据
    public void clearBytes(){
        //清理数据 并且接下来不接受任何数据
        content_type=RECEIVE_NODE;
        content_length=0;
        c_content_index=0;
        if (dataByteBuffer!=null) {
            dataByteBuffer.clear();
            if (dataByteBuffer.capacity()>BUFFER_SIZE) dataByteBuffer = null; //缓冲区过大,释放
        }

    }
    //获取存储完毕的数据-获取之后会清理数据
    public byte[] getBytes(){
        if (dataByteBuffer!=null) {
            dataByteBuffer.flip();
            byte[] bytes  = new byte[dataByteBuffer.limit()];
            int index = 0;
            while (dataByteBuffer.hasRemaining()){
                bytes[index] = dataByteBuffer.get();
                index++;
            }
            clearBytes();
            return bytes;
        }
        return null;
    }

    public void close() {
            clearBytes();
    }


    private int content_length; //当前可接受的数据大小
    private int c_content_index;//当前已接受的下标位置
    //设置接收的数据内容长度
    public void setContent_length(int content_length) {
        this.content_length = content_length;
    }


    private BlockingQueue<FtcBuffer> receiveBufferQueue = new LinkedBlockingQueue();

    /**
     * 存储系统读取到的 数据包
     * @param buffer
     */
    public void storeBuffer(FtcBuffer buffer) {
            //
        try {
            receiveBufferQueue.put(buffer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public FtcBuffer takeBuffer() {
        try {
            return receiveBufferQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    private ByteBuffer ramContentBuffer = ByteBuffer.allocate(1024*16);// 缓存 - 16K ,上次剩余数据
    public ByteBuffer getRamContent() {
        ramContentBuffer.flip();
        ByteBuffer temp = ByteBuffer.allocate(ramContentBuffer.limit());
        while(ramContentBuffer.hasRemaining()){
            temp.put(ramContentBuffer.get());
        }
        ramContentBuffer.clear();
        temp.flip();
        return temp;
    }

}
