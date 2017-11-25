package com.winone.ftc.mtools;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.winone.ftc.mtools.FileUtil.SEPARATOR;

/**
 * Created by lzp on 2017/5/8.
 *
 */
public class Log {

    public static final String TAG ="FTC_LOG";
    public static String LOG_FILE_PATH = FileUtil.PROGRESS_HOME_PATH;
    private static String LOG_FILE_FILE = "info.log";
    private static String PROGRESS_LOG_FILE_FILE = "progress.log";
    private static String OTHER_LOG_FILE_FILE = "other.log";
    private static final SimpleDateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:ms]");//
    private static boolean print = true;
    private static boolean isStartWriteFile = false;
    public static void setPrint(boolean isPrint){
        print = isPrint;
    }

    public static void i(String TAG,String message){
        if (print) {
            System.out.println(TAG +"  "+ message);
        }else{
            writeLogWrite(TAG +"  "+ message);
        }
    }

    public static void w(String TAG,String message){
        if (print) {
            System.err.println(TAG +"  "+ message);
        }else{
            writeLogWrite3(TAG +"  "+ message);
        }
    }

    public static void e(String TAG,String message){
        if (print) {
            System.err.println(TAG +"   "+ message);
        }else{
            writeLogWrite2(TAG +"   "+ message);
        }
    }

    public static void println(Object... arr){
        StringBuilder stringBuilder = new StringBuilder();
        for(Object  obj : arr){
            stringBuilder.append(obj);
        }
        System.out.println(TAG + getTimeString(0)+">>\n\t"+ stringBuilder.toString()+"\n");

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
        writeLog(s,LOG_FILE_FILE);
    }
    private static void writeLogWrite2(String s) {
        writeLog(s,PROGRESS_LOG_FILE_FILE);
    }
    private static void writeLogWrite3(String s) {writeLog(s,OTHER_LOG_FILE_FILE);}

    public static String getTimeString(long currentValue){
        return currentValue==0? format.format(new Date()) : format.format(new Date(currentValue));
    }

    private static synchronized void writeLog(String content,String file){
        if (FileUtil.checkDir(LOG_FILE_PATH+FileUtil.SEPARATOR+TAG)){
            if (!isStartWriteFile){
                FileUtil.deleteFile(LOG_FILE_PATH+FileUtil.SEPARATOR+TAG+FileUtil.SEPARATOR+LOG_FILE_FILE);
                FileUtil.deleteFile(LOG_FILE_PATH+FileUtil.SEPARATOR+TAG+FileUtil.SEPARATOR+PROGRESS_LOG_FILE_FILE);
                FileUtil.deleteFile(LOG_FILE_PATH+FileUtil.SEPARATOR+TAG+FileUtil.SEPARATOR+OTHER_LOG_FILE_FILE);
                isStartWriteFile = true;
            }
            FileUtil.writeStringToFile(content+"\n",LOG_FILE_PATH+FileUtil.SEPARATOR+TAG,file,true);
        }

    }
}
