import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;

/**
 * Created by user on 2017/11/28.
 */
public class test3 {
    public static void clean(final ByteBuffer byteBuffer) {
        if (byteBuffer.isDirect()) {
            ((DirectBuffer)byteBuffer).cleaner().clean();
        }
    }

    public static void sleep(long i) {
        try {
            Thread.sleep(i);
        }catch(Exception e) {
              /*skip*/
        }
    }

    private static  ByteBuffer buffer;
    public static void main(String []args) throws Exception {
        buffer = ByteBuffer.allocateDirect(1024 * 1024 * 1024);
        buffer.clear();
        buffer.putLong(1024L);
        buffer.putLong(1024L);
        buffer.putLong(1024L);
        buffer.putLong(1024L);
        System.out.println("start");
        sleep(5 * 1000);
        clean(buffer);//执行垃圾回收
//         System.gc();//执行Full gc进行垃圾回收
        System.out.println("end");
        sleep(30* 10000);
    }



}
