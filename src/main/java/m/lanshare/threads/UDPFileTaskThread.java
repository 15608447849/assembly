package m.lanshare.threads;

import m.lanshare.FileQuery;
import m.lanshare.LANManage;
import m.lanshare.beads.DataSource;
import m.lanshare.beads.TranslateClient;
import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/19.
 */
public class UDPFileTaskThread extends Thread {

    private DatagramChannel channel_masticate;
    private Path loadFilePath;
    private Path loadFileTempPath;
    private byte[] fileMd5;
    private long fileSize;
    private LANManage manage;
    private boolean isAdd = true;
    private HashSet<DataSource> sourceSet = new HashSet<>();
    private ArrayList<TranslateClient> taskList = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public UDPFileTaskThread(byte[] fileMd5,long fileSize, String filePathStr, LANManage manager) {
        if (FileQuery.home == null) return;
        this.loadFilePath = Paths.get(FileQuery.home+filePathStr);
        this.loadFileTempPath = Paths.get(FileQuery.home+filePathStr+".udp");
        this.fileMd5 = fileMd5;
        this.fileSize = fileSize;
        this.channel_masticate = manager.getChannelMutics();
        this.manage = manager;
        if (this.manage.addFileTask(fileMd5,this)){
            this.start();
        }
    }

    public LANManage getLanManage(){
        return manage;
    }
    public Path getFileTempPath() {
        return loadFileTempPath;
    }

    public byte[] getFileMD5() {
        return fileMd5;
    }
    //添加数据源
    public void putSource(DataSource source){
        if (isAdd){
            sourceSet.add(source);
        }
    }

    //添加子任务
    public void addChildTask(TranslateClient translateClient) {
        taskList.add(translateClient);
    }
    //移除子任务
    public void removeChildTask(TranslateClient translateClient){
        try {
            lock.lock();
            Log.i("移除子任务: "+translateClient+" - - "+ taskList.remove(translateClient)+" 当前任务数:"+taskList.size());
            if (taskList.isEmpty()){
                synchronized (this){
                    this.notifyAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void run() {
        //发射局域网广播 -> 文件md5
        ByteBuffer buffer = ByteBuffer.allocate(1+16+8);
        buffer.clear();
        buffer.put((byte)21);
        buffer.put(fileMd5);
        buffer.putLong(fileSize);
        buffer.flip();
        long time = System.currentTimeMillis();
        isAdd = true;
        try {
            channel_masticate.send(buffer,manage.getInfo().getBroadAddress());
        } catch (IOException e) {
            ;
        }
//        while (channel_masticate.isOpen() && (System.currentTimeMillis() - time)<(5*1000)){
//        }
            try {

                synchronized (this){
                    this.wait(1000 * 5); //等待1秒
                }
            } catch (Exception e) {
                ;
            }

        isAdd = false;

        Log.i(this+ " : 当前文件源数量: "+ sourceSet.size());
        if (sourceSet.size()>0){
            //创建子任务
           createSubTask();
            //关联数据源
            connectSource();
            //创建子连接
            createConnect();
            //循环监听
           loopWatch();
        }else{
            //请求服务器传输
            Log.i("请求服务器传输文件.");
            queryServerTranslate();
        }




    }

    private void loopWatch() {
        long time = System.currentTimeMillis();
        synchronized (this){
            try {
                this.wait();
            } catch (InterruptedException e) {
                ;
            }
        }
        Log.i("退出循环监听..."+(System.currentTimeMillis() - time)+" 毫秒.");
        boolean flag = MD5Util.isEqualMD5(fileMd5,MD5Util.getFileMD5Bytes(loadFileTempPath.toFile()));
        Log.i("任务全部结束,文件 对比结果: "+ flag);
        if (flag){
            Log.i("从命名: "+ loadFileTempPath.toFile().renameTo(loadFilePath.toFile()));
        }else{
            Log.i("传输数据错误 ,删除 : "+ loadFileTempPath.toFile().delete());
        }
    }

    /**
     * 请求服务器传输文件
     */
    private void queryServerTranslate() {

    }

    //创建子任务
    private void createSubTask() {
        int sourceNumber = sourceSet.size();
        //创建下载传输
        //根据源数量 - 分段:  例: 当前数据源 3 , 则 文件大小 / 3 -> 每段的数量, 余数加在最后一段
        int ramNun = (int) (fileSize%sourceNumber);
        int sliceNunber = (int) ((fileSize - ramNun) / sourceNumber);
        TranslateClient task;
        for (int i = 0;i<sourceNumber;i++){
            task = new TranslateClient(this);
            if (i== (sourceNumber-1)){
                task.setDataSlice(sliceNunber+ramNun,i * sliceNunber ,(i+1) * sliceNunber + ramNun);
            }else{
                task.setDataSlice(sliceNunber,i * sliceNunber ,(i+1) * sliceNunber);
            }

        }
        Log.i("数据分段成功"+taskList);
    }
    //关联数据源
    private void connectSource() {
        Iterator<TranslateClient> iterator = taskList.iterator();
        Object[] object = sourceSet.toArray();
        int index = 0;
        TranslateClient client;
        DataSource dataSource;
        while (iterator.hasNext()){
            client = iterator.next();
            dataSource = (DataSource) object[index];
            client.serverSocketAddress = dataSource.getToAddress();
            client.filePathBytes = dataSource.getFilePathBytes();
            index++;
            if (index==object.length) index=0;
        }
        Log.i("数据关联成功.");
    }


    /**
     * 创建子UDP连接
     *
     */
    private void createConnect() {

         try {
             lock.lock();
             Iterator<TranslateClient> iterator = taskList.iterator();
             TranslateClient client;
             while (iterator.hasNext()){
                 client =  iterator.next();
                 if (!client.isExcute){
                     client.connect();
                 }
             }
         } finally {
             lock.unlock();
         }
    }


}
