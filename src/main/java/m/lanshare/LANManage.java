package m.lanshare;

import m.lanshare.threads.LANThread;
import m.lanshare.threads.UDPFileTaskThread;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/19.
 *  局域网文件共享
 *  1 设置主目录
 */
public class LANManage {
    private static final String TAG = "UDP文件服务";



    private static class Holder{
        private static final LANManage manager = new LANManage();
    }
    public static LANManage get(){
        return Holder.manager;
    }

    private final ReentrantLock lock = new ReentrantLock();
    //端口使用列表
    private HashSet<Integer> portUseList = new HashSet<>();
    //文件下载任务列表
    private HashMap<String,UDPFileTaskThread> taskMap = new HashMap<>();
    private Selector selector;
    private DatagramChannel channelSend;
    private DatagramChannel channelMutics;
    private LANInfo info;
    private LANThread thread;
    //设置信息
    public void initial(LANInfo info){
        this.info = info;
    }
    /*
    *启动
     */
    public void start() throws Exception {
        if (info==null) throw new NullPointerException("info is null");
        if (thread!=null) throw new Exception("this is starting.");

            FileQuery.home = info.getHomeDir();

            selector = Selector.open();
            //发送消息的端口
            channelSend = DatagramChannel.open().bind(info.getSendSocketAddress());
            channelSend.configureBlocking(false);
            channelSend.register(selector, SelectionKey.OP_READ);

            addPort(info.getSendSocketAddress().getPort());

            //接受组播信息的端口
            channelMutics = DatagramChannel.open().bind(info.getMulticsSocketAddress());
            channelMutics.configureBlocking(false);
            channelMutics.register(selector, SelectionKey.OP_READ);
            channelMutics.setOption(StandardSocketOptions.SO_BROADCAST,true);
            addPort(info.getMulticsSocketAddress().getPort());

            //开始
            thread = new LANThread(this);
    }

    /**
     * 发送文件路径到服务器
     */
    public void sendFilePath(String serverIp,int serverPort,String path) {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(serverIp,serverPort);
            byte[] pathBytes = path.getBytes("UTF-8");
            ByteBuffer buffer = ByteBuffer.allocate(1+pathBytes.length);
            buffer.put((byte)1);
            buffer.put(pathBytes);
            buffer.flip();
            channelSend.send(buffer,socketAddress);

        } catch (UnsupportedEncodingException e) {
            ;
        } catch (IOException e) {
            ;
        }
    }


    public Selector getSelector() {
        return selector;
    }
    public LANInfo getInfo() {
        return info;
    }
    public DatagramChannel getChannelSend() {
        return channelSend;
    }
    public DatagramChannel getChannelMutics() {
        return channelMutics;
    }

    /**
     * 添加一个端口
     */
    public void addPort(int port){
        try{
            lock.lock();
            portUseList.add(port);
        }finally {
            lock.unlock();
        }
    }
    /**
     * 返回一个端口
     */
    public int getPort(){
        int p = info.getMinPort() + (int)(Math.random() * ((info.getMaxPort() - info.getMinPort()) + 1));
        return checkPort(p)?getPort():p;
    }
    //检测端口
    private boolean checkPort(int port){
        try{
            lock.lock();
            return  portUseList.contains(port);
        }finally {
            lock.unlock();
        }
    }
    /**
     * 移除端口
     */
    public void removePort(int port){

        try {
            lock.lock();
            portUseList.remove(port);
            Log.i("返回端口:"+ port);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 添加一个下载任务
     * @param fileMd5
     * @param udpFileTask
     */
    public boolean addFileTask(byte[] fileMd5, UDPFileTaskThread udpFileTask) {
        try {
            lock.lock();
            String md5 = MD5Util.bytesGetMD5String(fileMd5);
            if (taskMap.containsKey(md5)) return false;
            taskMap.put(md5,udpFileTask);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取一个下载任务
     * @param fileMd5
     * @return
     */
    public UDPFileTaskThread getFileTask(byte[] fileMd5) {
        try {
            lock.lock();
            String md5 = MD5Util.bytesGetMD5String(fileMd5);
            if (taskMap.containsKey(md5)){
                return taskMap.get(md5);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

}
