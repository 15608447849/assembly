package com.m.sqlites;

import com.winone.ftc.mtools.FileUtil;
import com.winone.ftc.mtools.StringUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/30.
 */
public class SQLiteHelper extends Thread {

    public static final String MEMORY_DB_PATH = ":memory:";

    private int outTime = 30 * 1000;

    public int getOutTime() {
        return outTime;
    }

    public void setOutTime(int outTime) {
        this.outTime = outTime;
    }

    private final ReentrantLock lock = new ReentrantLock();
    private final ArrayList<SQLiteConnect> mConnectPools = new ArrayList<>();
    protected final String path;
    protected final String dbName;



    /**
     *
     * @param fileDirPath 数据库路径 不存在自动创建
     * @param dbName 数据库名
     */
    public SQLiteHelper(String fileDirPath,String dbName){
        if (StringUtil.isEntry(fileDirPath)){
            this.path = MEMORY_DB_PATH;
        }else{
            if (FileUtil.checkDir(fileDirPath)){
                this.path = ":"+fileDirPath+"/";
            }else{
                throw new IllegalStateException("SQL path '" + fileDirPath +"' is valid.");
            }

        }

       this.dbName = dbName;
    }

    @Override
    public void run() {
        while (true){
           synchronized (this){
               try {
                   this.wait(outTime);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           check();
        }
    }

    /**
     * 检查连接池
     */
    private void check() {
        try {
            lock.lock();
            if (mConnectPools.size()>0){
                Iterator<SQLiteConnect> iterator = mConnectPools.iterator();
                SQLiteConnect connect ;
                while (iterator.hasNext()){
                    connect = iterator.next();
                    if (connect.isClose() || connect.isOuttime()){
                        iterator.remove();
                        connect.close();
                    }
                }
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 获取连接池
     */
    public SQLiteConnect getConnect(){
        try {
            lock.lock();
            if (mConnectPools.size()>0){
                Iterator<SQLiteConnect> iterator = mConnectPools.iterator();
                SQLiteConnect connect ;
                while (iterator.hasNext()){
                    connect = iterator.next();
                    if (!connect.isClose() && connect.getState() == SQLiteConnect.NORMAL){
                        connect.setState(SQLiteConnect.USED);
                        connect.updateTime();
                        return connect;
                    }
                }
            }
            return createConnect();
        }finally {
            lock.unlock();
        }
    }
    public void putConnect(SQLiteConnect connect){
        assert connect!=null;
        connect.setState(SQLiteConnect.NORMAL);
    }

    private SQLiteConnect createConnect() {
        SQLiteConnect connect = new SQLiteConnect(this);
        mConnectPools.add(connect);
        return connect;
    }

    /**
     * 创建表
     * @param tableName
     * @param map
     */
    public void createTable(String tableName,Map<String,String> map){
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("create table if not exists ").append(tableName).append(" (");

        if (map!=null){
            Iterator<Map.Entry<String,String>> iterator = map.entrySet().iterator();
            Map.Entry<String,String> entry;
            int index=0;
            while (iterator.hasNext()){
                entry = iterator.next();
                stringBuffer.append(entry.getKey()).append(" ").append(entry.getValue());
                index++;
                if (index != map.size()) stringBuffer.append(",");
            }
        }
        stringBuffer.append(");");
        executeSql(stringBuffer.toString());
    }

    /**
     * 执行sql
     * @param sql
     */
    public void executeSql(String sql){
       SQLiteConnect connection = getConnect();
//        Log.i("执行SQL    "+sql);
        Statement stat = null;
        try {
            stat = connection.getConnect().createStatement();
            stat.executeUpdate( sql );
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            closeStatement(stat);
            putConnect(connection);
        }
    }
    //关闭声明对象
    private void closeStatement(Statement stat){
        if (stat!=null){
            try {
                stat.close();
            } catch (SQLException e) {
            }
        }
    }

    public void insertOrUpdate(String tableName, ArrayList<String> keys,ArrayList<Object> values){
      StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append("replace into ").append(tableName);
      if (keys!=null && keys.size()>0){
          stringBuffer.append(" (");
          for (int i=0;i<keys.size();i++){
              stringBuffer.append(keys.get(i));
              if (i!= keys.size()-1) stringBuffer.append(",");
          }
          stringBuffer.append(")");
      }
        if (values!=null && values.size()>0){
            stringBuffer.append(" values (");
            for (int i=0;i<values.size();i++){
                stringBuffer.append("'").append(values.get(i)).append("'");
                if (i!= keys.size()-1) stringBuffer.append(",");
            }
            stringBuffer.append(")");
        }
        stringBuffer.append(";");
        executeSql(stringBuffer.toString());
    }

}
