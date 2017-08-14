package com.winone.ftc.mtools;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by user on 2017/6/5.
 */
public class MD5Util {

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
    /**
     * 获取文件md5的byte值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getFileMD5Bytes(File file){
        return getFileMD5Bytes(file,0,-1);
    }
    /**
     * 获取文件md5的byte值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getFileMD5Bytes(File file,long start,long end){
        byte[] result = null;
        FileChannel ch = null;
        MappedByteBuffer byteBuffer = null;
        try {
            ch = new RandomAccessFile(file,"r").getChannel();
            if (end == -1) end = ch.size();
//            Log.e("MD5 GET: "+ file.getAbsolutePath()+" start:"+start+" ,end:"+ end);
            byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, start, end-start);//内存映射
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update(byteBuffer);
            result = messagedigest.digest();
        } catch (Exception e) {
            ;
        } finally {
            if (ch!=null){
                try {
                    ch.close();
                } catch (IOException e) {
                    ;
                }
            }
            new MPrivilegedAction(byteBuffer);
            System.gc();
        }
        return result;
    }
    /**
     * 获取文件MD5的String
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMD5String(File file) throws Exception {
        return bytesGetMD5String(getFileMD5Bytes(file));
    }
    /**
     * 获取String的MD5值
     * @param s
     * @return
     */
    public static String getMD5String(String s) {
        byte[] bytes = s.getBytes();
        MessageDigest messagedigest = null;
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            ;
            return null;
        }
        messagedigest.update(bytes);
        return bytesGetMD5String(messagedigest.digest());
    }
    /**
     * 获取字节的md5->16进制字符串
     * @param bytes
     * @return
     */
    public static String bytesGetMD5String(byte[] bytes) {
        return byteArrayToHexString(bytes);
    }
    /**
     * byte->16进制
     * @param bytes
     * @return
     */
    private static String byteArrayToHexString(byte bytes[]) {
        return byteArrayToHexString(bytes, 0, bytes.length);
    }
    /**
     * 截取一段byte变16进制字符串
     * @param bytes
     * @param m
     * @param n
     * @return
     */
    private static String byteArrayToHexString(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    /**
     * 比较MD5
     * @param digesta
     * @param digestb
     * @return
     */
    public static boolean isEqualMD5(byte[] digesta,byte[] digestb){
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            return messagedigest.isEqual(digesta,digestb);
        } catch (NoSuchAlgorithmException e) {
            ;
        }

        return false;
    }
    public static boolean isEqualFileMd5(File src,File desr){
        return isEqualMD5(getFileMD5Bytes(src),getFileMD5Bytes(desr));
    }

}
