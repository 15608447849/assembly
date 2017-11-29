package com.m.backup.slice;

import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.MD5Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by user on 2017/11/24.
 */
public class SliceUtil {

    private static void close(RandomAccessFile r){
        try {
            if (r!=null) r.close();
        } catch (IOException e) {
        }
    }

    public static int sliceSizeConvert(long fileSize){
        //按照  (512 / 100*1024*2) >> (0.025) 的比例换算
        int sliceSize = (int) (fileSize *  0.025);
        return sliceSize>0?sliceSize:512;

    }



    /**
     * 对文件分片,返回分片信息
     * @param file
     * @return
     */
    public static ArrayList<SliceInfo> fileSliceInfoList(File file,int sliceSize){
        long fileSize = file.length();
        long sliceSum = fileSize / sliceSize;
        int mod = (int) (fileSize % sliceSize);
        if (mod>0){
            sliceSum+=1;
        }
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(file,"r");
            ArrayList<SliceInfo> sliceList = new ArrayList<>();
            int len;
            long position = 0;
            byte[] buffer  = new byte[sliceSize];
            SliceInfo sliceInfo;
            for(int i = 0 ; i < sliceSum;i++ ){
                len = (int) Math.min(sliceSize,(randomAccessFile.length()-position));

                randomAccessFile.seek(position);
                randomAccessFile.read(buffer,0,len);
//                Log.println("数据分片: "+ new String(buffer,0,len)+" , "+ " - "+ len);
                sliceInfo = new SliceInfo();
                sliceInfo.setPosition(position);
                sliceInfo.setLength(len);
                sliceInfo.setAdler32Hex(MD5Util.adler32Hex(buffer,0,len));
                sliceInfo.setMd5Hex(MD5Util.getBytesMd5ByString(buffer,0,len));
                sliceList.add(sliceInfo);
                position += sliceSize;
            }
            return sliceList;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            close(randomAccessFile);
        }
        return null;
    }

    //对分片数据 分组 ,方便对比查询
    public static Hashtable<String,LinkedList<SliceInfo>> sliceInfoToTable(ArrayList<SliceInfo> sliceInfoList) {
        Hashtable<String,LinkedList<SliceInfo>> table = new Hashtable<>();
        for (SliceInfo sliceBean:sliceInfoList){
            LinkedList list = table.get(sliceBean.getAdler32Hex());
            if (list==null){list  = new LinkedList();}
            list.add(sliceBean);
            table.put(sliceBean.getAdler32Hex(),list);
        }
        return table;
    }


    /**
     * 滚动检测
     * @param table
     * @param file
     * @return
     */
    public static SliceScrollResult scrollCheck(Hashtable<String, LinkedList<SliceInfo>> table, File file,int sliceSize) {
        RandomAccessFile randomAccessFile = null;
        try{
            randomAccessFile = new RandomAccessFile(file, "r");
            long fileLength = randomAccessFile.length();
            int move = (int)(fileLength*0.01);
            int len = 0;
            long position = 0;
            boolean moveBlock = false;
            byte[] buf = new byte[sliceSize];
            String adler32Hex;
            Iterator<Map.Entry<String,LinkedList<SliceInfo>>> iterator;
            Map.Entry<String,LinkedList<SliceInfo>> entry;
            String md5;
            LinkedList list;
            Iterator<SliceInfo> it ;
            SliceInfo sliceInfo;
            SliceMapper mapper_same;

            SliceScrollResult result = new SliceScrollResult();
            while(true){

                moveBlock = false;
                len = (int) Math.min(sliceSize,(fileLength-position));
                if (len<1) {
                    break;
                }

                randomAccessFile.seek(position);

                randomAccessFile.read(buf,0,len);

                adler32Hex = MD5Util.adler32Hex(buf,0,len);

                iterator = table.entrySet().iterator();
                while (iterator.hasNext()){
                    entry = iterator.next();
                    if (entry.getKey().equalsIgnoreCase(adler32Hex)){
                        md5 = MD5Util.getBytesMd5ByString(buf,0,len);
                        list = entry.getValue();
                        if (list.size()>0){
                            it = list.iterator();
                            while (it.hasNext()){
                                sliceInfo = it.next();
                                if (sliceInfo.getMd5Hex().equals(md5)){
                                    mapper_same = new SliceMapper(0);
                                    mapper_same.setPosition(position);
                                    mapper_same.setLength(sliceInfo.getLength());
                                    mapper_same.setSliceInfo(sliceInfo);
                                    result.getList_same().add(mapper_same);
                                    moveBlock = true;
                                    it.remove();
                                    break;
                                }
                            }
                        }else{
                            iterator.remove();
                        }
                    }
                    if (moveBlock) break;
                }

                if (moveBlock){
//                    Log.println("移动块 : ",(Math.min((int)(fileLength-position),sliceSize)<sliceSize)?(int)(fileLength-position):sliceSize);
                    position+=sliceSize;//向后移动一块数据

                }else{
//                    Log.println("移动格子 : ",(Math.min((int)(fileLength-position),move)<move)?1:move);
                    position+=move;  //向后偏移文件大小的1% 字节

                }
//                Log.println("滚动检测: ", position ," , " +fileLength," , " ,NumberFormat.getInstance().format((double)position/(double)fileLength * 100),"%");
                if (position>fileLength) break;
//                Log.println("滚动检测: ", position ," , " +fileLength," , " ,NumberFormat.getInstance().format((double)position/(double)fileLength * 100),"%");
            }

            //根据相同的 确定不同的坐标
            SliceMapper mapper_diff;
            long recodePosition = 0;
            if (result.getSameSize()>0){
                for (SliceMapper same : result.getList_same()){
                    if (recodePosition<same.getPosition()){
                        len = (int) (same.getPosition() - recodePosition);
                        mapper_diff = new SliceMapper(1);
                        mapper_diff.setPosition(recodePosition);
                        mapper_diff.setLength(len);
                        result.getList_diff().add(mapper_diff);

                    }
                     recodePosition = same.getPosition()+same.getLength();

                }
                if (recodePosition!=fileLength){
                    len = (int) (fileLength - recodePosition);
                    mapper_diff = new SliceMapper(1);
                    mapper_diff.setPosition(recodePosition);
                    mapper_diff.setLength(len);
                    result.getList_diff().add(mapper_diff);
                }
            }

           return result;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            close(randomAccessFile);
        }
        return null;
    }
}
