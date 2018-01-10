package com.m.backup.client;

import com.m.backup.beans.TimerBean;
import com.m.backup.beans.Action;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 2017/11/27.
 */
public class FBCTimeTaskOp{
    private final HashMap<String,TimerBean> timeTaskMap;
    private final FtcBackupClient client;
    public FBCTimeTaskOp(FtcBackupClient client) {
        this.timeTaskMap = new HashMap<>();
        this.client = client;
    }

    /**
     * 设置定时任务
     */
    public final void setTime(String timeStr){
        try{
            TimerBean tb = new TimerBean(timeStr, new Action<TimerBean> () {

                @Override
                public void call(TimerBean timerBean) {
                    //启动 客户端遍历文件夹
                    final List<InetSocketAddress> serverAddressList = client.getServerAddressList();
                    if (serverAddressList!=null && serverAddressList.size()>0){
                        for (InetSocketAddress it:serverAddressList){
                            client.ergodicDirectory(it);
                        }
                    }

                    if(timerBean.getType() == TimerBean.FIXED_POINT){
                        //定点
                        //从队列中移除
                        timerBean.cancel();
                        timeTaskMap.remove(timerBean.getDateStr());
                    }
                }
            });
            if (timeTaskMap.containsKey(timeStr)){
                timeTaskMap.get(timeTaskMap).cancel();
            }
            tb.schedule();
            timeTaskMap.put(timeStr,tb);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }











}
