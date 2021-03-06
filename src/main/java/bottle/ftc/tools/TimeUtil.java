package bottle.ftc.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by user on 2017/11/27.
 */
public class TimeUtil {

    /**
     * 一天得间隔时间
     */
    public static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

    /**添加x天*/
    private static Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }
    /**string -> date ,  参数:"11:00:00"  如果小于当前时间,向后加一天*/
    public static Date str_Hms_2Date(String timeString) {
        try {
            String[] strArr = timeString.split(":");

            Calendar calendar = Calendar.getInstance();
            if (strArr.length >= 1){
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strArr[0]));
            }else{
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            }
            if (strArr.length >= 2){
                calendar.set(Calendar.MINUTE, Integer.parseInt(strArr[1]));
            }else{
                calendar.set(Calendar.MINUTE,0);
            }
            if (strArr.length >= 3){
                calendar.set(Calendar.SECOND, Integer.parseInt(strArr[2]));
            }else{
                calendar.set(Calendar.SECOND, 0);
            }
            Date date = calendar.getTime();
            if (date.before(new Date())) {
                date = addDay(date, 1);
            }
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
       return null;
    }

    /**
     * 例: 2017-11-11 9:50:00
     */
    public static Date str_yMd_Hms_2Date(String timeString){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return simpleDateFormat.parse(timeString);
        } catch (ParseException e) {
        }
        return null;
    }



//    public static void main(String[] a){
//        Log.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(str_Hms_2Date("8:00:00")));
//        Log.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(str_yMd_Hms_2Date("2017-11-27 8:00:00")));
//    }


}
