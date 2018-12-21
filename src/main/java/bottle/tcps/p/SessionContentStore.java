package bottle.tcps.p;

import sun.nio.ch.DirectBuffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by user on 2017/7/8.
 * 存在 内容缓存区buf
 */
class SessionContentStore {
    public static final int RECEIVE_NODE = 0;//不存储任何东西
    public static final int RECEIVE_CHARSET = 1; //存储字符编码byte[]
    public static final int RECEIVE_STRING = 2; //存储字符串内容byte[]
    public static final int RECEIVE_STREAM = 3;//接受字节流
    private int protocol = RECEIVE_NODE;
    public int getContent_type() {
        return protocol;
    }
    public void setContent_type(int content_type) {
        this.protocol = content_type;
    }
    private static final int BUFFER_BLOCK_SIZE = 16 * 1024 * 1024; // 4M jvm堆外内存
    /**
     * 系统接收到的缓冲区 包数据
     */
    private final BlockingQueue<byte[]> receiveBufferQueue = new LinkedBlockingQueue();
    /**
     * 存储系统读取到的 数据包
     * @param buffer
     */
    public void storeBuffer(ByteBuffer buffer) {
            //
        try {
            buffer.flip();
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            buffer.clear();
            receiveBufferQueue.put(bytes);
//            Log.i("队列大小: " + receiveBufferQueue.size()+" 添加数据: "+ bytes.length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public byte[] takeBuffer() {
        try {
//            Log.i("队列大小: " + receiveBufferQueue.size()+" 尝试取出中");
            return receiveBufferQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }


    //剩余数据
    private ByteBuffer ramContentBuffer;//上次剩余数据

    public byte[] getRamContent() {
        if (ramContentBuffer!=null && ramContentBuffer.position()>0){
            ramContentBuffer.flip();
            byte[] bytes = new byte[ramContentBuffer.limit()];
            ramContentBuffer.get(bytes);
            ramContentBuffer.clear();
            return bytes;
        }else{
            return null;
        }
    }
    //待收集的数据体长度
    private int dataBodyLength = -1;
    public int toBeCollectedSize() {
        return dataBodyLength;
    }
    //设置接收的数据内容长度
    public void setCollectedSize(int dataBodyLength) {
        this.dataBodyLength = dataBodyLength;
    }
    //存入剩余数据
    public void storeRemainBytes(byte[] bytes,int offset,int length) {
        if (ramContentBuffer==null) ramContentBuffer = createByteBuffer(BUFFER_BLOCK_SIZE);
        if (length>ramContentBuffer.capacity()) throw new IllegalStateException("tcp read remain  buffer size is insufficient.the data length: "+ length);
        ramContentBuffer.clear();
        ramContentBuffer.put(bytes,offset,length);
    }

    //复位
    public void reset() {
        dataBodyLength = -1;
        protocol = RECEIVE_NODE;
    }
    public void clean(final ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect()) {
//            Log.i(byteBuffer);
            ((DirectBuffer)byteBuffer).cleaner().clean();
        }
    }
    public ByteBuffer createByteBuffer(int size){
        return ByteBuffer.allocateDirect(size);//申请堆外内存
//        return ByteBuffer.allocate(size);
    }
    public void clear() {
//        Log.i("清理缓冲区");
        if (ramContentBuffer!=null){
            ramContentBuffer.clear();
            clean(ramContentBuffer);
            ramContentBuffer=null;
        }
        if (sendBufferBySystemTcpStack!=null){

            sendBufferBySystemTcpStack.clear();
            clean(sendBufferBySystemTcpStack);
            sendBufferBySystemTcpStack=null;
        }
        if (readBufferBySystemTcpStack!=null){
            readBufferBySystemTcpStack.clear();
            clean(readBufferBySystemTcpStack);
            readBufferBySystemTcpStack=null;
        }
        receiveBufferQueue.clear();
        System.gc();
    }
    private ByteBuffer sendBufferBySystemTcpStack;
    private ByteBuffer readBufferBySystemTcpStack;
    public ByteBuffer getSendBufferBySystemTcpStack() {

        if (sendBufferBySystemTcpStack==null){

            sendBufferBySystemTcpStack = createByteBuffer(BUFFER_BLOCK_SIZE+8);//八位协议位
//            Log.i("sendBufferBySystemTcpStack :"+sendBufferBySystemTcpStack);
//            Log.i("创建发送消息缓冲区");
        }
        sendBufferBySystemTcpStack.clear();
        return sendBufferBySystemTcpStack;
    }

    public ByteBuffer getSendBufferBySystemTcpStack(int len) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(len+8); ////八位协议位
        byteBuffer.clear();
        return byteBuffer;
    }

    public ByteBuffer getReadBufferBySystemTcpStack() {
        if (readBufferBySystemTcpStack==null){
            readBufferBySystemTcpStack = createByteBuffer(BUFFER_BLOCK_SIZE+8);
//            Log.i("readBufferBySystemTcpStack :"+readBufferBySystemTcpStack);
//            Log.i("创建读取消息缓冲区");
        }
        return readBufferBySystemTcpStack;
    }
}
