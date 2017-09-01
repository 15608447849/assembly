package com.winone.ftc.mentity.mbean.singer;

import com.winone.ftc.mtools.Log;
import m.sqlites.SQLiteHelper;

import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Created by user on 2017/7/3.
 * 下载文件 记录数据库对象
 */
public class DownloadFileRecordSql extends Thread {
    private DownloadFileRecordSql(){
        initialization();
        setDaemon(true);
        setName("FTC@DownloadFileRecodeToSQL");
        start();
    }

    @Override
    public void run() {
        //记录写入数据库
        while (true){
            if (dataList.size()>0){
                synchronized (this){
                    Iterator<ArrayList<Object>> iterator = dataList.iterator();
                    while (iterator.hasNext()){
                        sqLiteHelper.insertOrUpdate(SQL_TABLE,keysList,iterator.next());
                        iterator.remove();
                    }
                }
            }
            try {
                sleep(10*1000);
            } catch (InterruptedException e) {
            }
        }
    }

    private static class Holder{
        private static DownloadFileRecordSql instant = new DownloadFileRecordSql();
    }
    public static DownloadFileRecordSql get(){
        return Holder.instant;
    }

    public final String SQL_DB ="download_file_record.db";
    public final String SQL_TABLE ="download_file_table";

    public final String SQL_KEY_MD5 ="md5";
    public final String SQL_KEY_RESOURCE ="resource";
    public final String SQL_KEY_PATH ="local_path";
    public final String SQL_KEY_START_POSTION ="start_pos";
    public final String SQL_KEY_END_POSTION ="end_pos";
    public final String SQL_KEY_LENGTH ="length";
    public final String SQL_KEY_TIME = "time";

    private final ArrayList<ArrayList<Object>> dataList = new ArrayList<>();
    private final SQLiteHelper sqLiteHelper = new SQLiteHelper(".",SQL_DB);
    private final ArrayList<String> keysList = new ArrayList<>();
    /**
     * 初始化表
     */
    private void initialization(){
        keysList.add(SQL_KEY_MD5);
        keysList.add(SQL_KEY_RESOURCE);
        keysList.add(SQL_KEY_PATH);
        keysList.add(SQL_KEY_START_POSTION);
        keysList.add(SQL_KEY_END_POSTION);
        keysList.add(SQL_KEY_LENGTH);
        keysList.add(SQL_KEY_TIME);

        LinkedHashMap<String,String> maps = new LinkedHashMap<>();
            maps.put(SQL_KEY_MD5,"text unique");
            maps.put(SQL_KEY_RESOURCE,"text not null");
            maps.put(SQL_KEY_PATH,"text not null");
            maps.put(SQL_KEY_START_POSTION,"integer");
            maps.put(SQL_KEY_END_POSTION,"integer");
            maps.put(SQL_KEY_LENGTH,"integer");
            maps.put(SQL_KEY_LENGTH,"integer");
            maps.put(SQL_KEY_TIME,"text");
        sqLiteHelper.createTable(SQL_TABLE,maps);

    }
    /**
     * 插入数据
     */
    public void addRecord(String md5,String url,String localPath,long start,long end,long length){

        ArrayList<Object> valuesList = new ArrayList();
            valuesList.add(md5);
            valuesList.add(url);
            valuesList.add(localPath);
            valuesList.add(start);
            valuesList.add(end);
            valuesList.add(length);
            valuesList.add(Log.getTimeString(0));
            synchronized (this){
                dataList.add(valuesList);
            }
//        sqLiteHelper.insertOrUpdate(SQL_TABLE,keysList,valuesList);
    }

}
