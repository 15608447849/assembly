package com.winone.ftc.mentity.mbean.entity;

/**
 * Created by user on 2017/6/24.
 */
public class ManagerParams {

    /**
     * 同时运行任务最大数量
     */
    private int runtimeThreadMax =  Runtime.getRuntime().availableProcessors();
    /**
     * 检测网络
     */
    private boolean checkNetwork = true;
    /**
     * 输出日志
     */
    private boolean isPrintf = true;
    /**
     * 打开运行任务检测记录
     */
    private boolean isRecode = true;

    /**
     * 指定日志输出目录
     */
    private String logPath;

    public ManagerParams() {
    }

    public int getRuntimeThreadMax() {
        return runtimeThreadMax;
    }

    public void setRuntimeThreadMax(int runtimeThreadMax) {
        this.runtimeThreadMax = runtimeThreadMax;
    }

    public boolean isCheckNetwork() {
        return checkNetwork;
    }

    public void setCheckNetwork(boolean checkNetwork) {
        this.checkNetwork = checkNetwork;
    }

    public boolean isPrintf() {
        return isPrintf;
    }

    public void setPrintf(boolean printf) {
        isPrintf = printf;
    }

    public boolean isRecode() {
        return isRecode;
    }

    public void setRecode(boolean recode) {
        isRecode = recode;
    }

    public ManagerParams(int runtimeThreadMax, boolean checkNetwork, boolean isPrintf, boolean isRecode) {
        this.runtimeThreadMax = runtimeThreadMax;
        this.checkNetwork = checkNetwork;
        this.isPrintf = isPrintf;
        this.isRecode = isRecode;
    }
    public ManagerParams(boolean checkNetwork, boolean isPrintf, boolean isRecode) {
        this.checkNetwork = checkNetwork;
        this.isPrintf = isPrintf;
        this.isRecode = isRecode;
    }
    public ManagerParams(boolean checkNetwork, boolean isPrintf, boolean isRecode,String logPath) {
        this.checkNetwork = checkNetwork;
        this.isPrintf = isPrintf;
        this.isRecode = isRecode;
        this.logPath = logPath;
    }

    public ManagerParams(String logPath) {
        this.logPath = logPath;
        this.isPrintf = false;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
