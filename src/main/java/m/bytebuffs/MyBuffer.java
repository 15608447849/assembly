package m.bytebuffs;


import java.nio.ByteBuffer;

/**
 * Created by user on 2017/7/8.
 */
public class MyBuffer {

    public static int OUT_OF_DATE_LIMIT = 5 * 1000; //15秒
    private ByteBuffer buffer;

    private long usedTime;//最近使用时间大于规定时间,清理这个buf
    private int tag = 0; //0临时 1 不可被清理
    protected MyBuffer(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }


    public ByteBuffer flipBuf(){
        if (buffer!=null){
            buffer.flip();
            updateUsedTime();
        }
        return buffer;
    }

    public ByteBuffer clearBuf(){

        if (buffer!=null){
            buffer.clear();
            updateUsedTime();
        }
        return buffer;
    }


    public ByteBuffer compactBuf(){
        if (buffer!=null){
            buffer.compact();
            updateUsedTime();
        }
        return buffer;
    }


    public void clear(){
        if (buffer!=null){
            buffer.clear();
            buffer = null;
            System.gc();
        }
    }
    public ByteBuffer getBuf() {
            return buffer;
    }


    public void updateUsedTime(){
        if (isNotClearPower()) return;
        usedTime = System.currentTimeMillis();
    }

    /**
     * 检测过时
     * @return
     */
    protected boolean isOutOfDate(){
        return (System.currentTimeMillis() - usedTime)> OUT_OF_DATE_LIMIT;
    }

    /**
     * 获取缓冲区大小
     * @return
     */
    public int getLength() {
        return buffer.capacity();
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    /**
     * 是否允许被清理
     */
    public boolean isNotClearPower(){
        return tag == 1;
    }

}
