package com.winone.ftc.mtools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.winone.ftc.mtools.FileUtil.SEPARATOR;

/**
 * Created by lzp on 2017/5/8.
 *
 */
public class Log {
    private static boolean isStart = false;
    private static final String TAG ="FTC_LOG ";
    public static String LOG_FILE_PATH = FileUtil.PROGRESS_HOME_PATH;
    private static String LOG_FILE_FILE = "down.log";
    private static String PROGRESS_LOG_FILE_FILE = "prog.log";
    private static String OTHER_LOG_FILE_FILE = "other.log";

    private static final SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:ms]");
    private static boolean print = true;
    public static void setPrint(boolean isPrint){
        Log.print = isPrint;
    }
    public static void i(String TAG,String message){
        if (print) {
            System.out.println(TAG +"  "+ message);
        }else{
            writeLogWrite(TAG +"  "+ message);
        }
    }


    public static void e(String TAG,String message){
        if (print) {
            System.err.println(TAG +"   "+ message);
        }else{
            writeLogWrite2(TAG +"   "+ message);
        }
    }
    public static void w(String TAG,String message){
        if (print) {
            System.out.println(TAG +"  "+ message);
        }else{
            writeLogWrite3(TAG +"  "+ message);
        }
    }

    public static void i(String message){
       i(TAG+getTimeString(0), message);
    }
    public static void e(String message){
        e(TAG+getTimeString(0),message);
    }
    public static void w(String message){
        w(TAG+getTimeString(0),message);
    }

    private static void writeLogWrite(String s) {
        writeLog(s,LOG_FILE_PATH,LOG_FILE_FILE);
    }
    private static void writeLogWrite2(String s) {
        writeLog(s,LOG_FILE_PATH,PROGRESS_LOG_FILE_FILE);
    }
    private static void writeLogWrite3(String s) {writeLog(s,LOG_FILE_PATH,OTHER_LOG_FILE_FILE);}
    public static String getTimeString(long currentValue){
        return currentValue==0? format.format(new Date()) : format.format(new Date(currentValue));
    }
    private static synchronized void writeLog(String content,String path,String name){
        if (!isStart){
            FileUtil.deleteFile(path+SEPARATOR+LOG_FILE_FILE);
            FileUtil.deleteFile(path+SEPARATOR+PROGRESS_LOG_FILE_FILE);
            isStart = true;
        }
        FileUtil.writeStringToFile(content+"\n",path,name,true);
    }
}
