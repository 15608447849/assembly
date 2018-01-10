package com.m.backup.beans;


import com.winone.ftc.mtools.TimeUtil;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 2017/11/27.
 */
public class TimerBean {
    public static final int FIXED_POINT = 1;
    public static final int LOOP = 2;
    private final Action<TimerBean> action;
    private int type = -1;
    private String dateStr;
    private Date date;
    private TimerTask task;
    private Timer timer ;
    public TimerBean(String dateStr,Action action) throws IllegalArgumentException {

        Date date = TimeUtil.str_yMd_Hms_2Date(dateStr);
        if (date==null){
            date = TimeUtil.str_Hms_2Date(dateStr);
            if (date!=null){
                type = LOOP; //每天几点执行
            }
        } else{
            //判断指定时间是否合法
            if (date.getTime()<new Date().getTime()){
                throw new IllegalArgumentException("this fixed point time ( "+ dateStr+" ) is obsolete.");
            }
            type = FIXED_POINT;//指定时刻执行
        }
        if (type==FIXED_POINT || type==LOOP){
            this.dateStr = dateStr;
            this.date = date;
            this.action = action;
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    action.call(TimerBean.this);
                }
            };
        }else{
            throw new IllegalArgumentException("time string by "+ dateStr+",the format is unknown.");
        }

    }
    public int getType() {
        return type;
    }
    public void schedule(){
        if (task==null || timer==null) return;
        if (type==FIXED_POINT) timer.schedule(task,date,TimeUtil.PERIOD_DAY);
        if (type==LOOP) timer.schedule(task,date,TimeUtil.PERIOD_DAY);
    }
    /**
     * 取消
     */
    public void cancel(){
        if (task!=null){
            task.cancel();
            task=null;
        }
        if (timer!=null){
            timer.cancel();
            timer=null;
        }
    }

    public String getDateStr() {
        return dateStr;
    }
}
