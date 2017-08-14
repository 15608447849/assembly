package com.winone.ftc.mentity.mbean;

import java.io.Serializable;

/**
 * Created by lzp on 2017/5/8.
 * FTP信息
 */
public class FtpInfo {
    private String host;
    private int port = 21;
    private String userName;
    private String password;

    public FtpInfo() {
    }


    public FtpInfo(String host, int port, String userName, String password) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
              sb.append("用户名: " + userName).append(" ,密码: " + password).append(" ,主机地址: " + host).append(" ,端口号: " + port);
        return sb.toString();
    }

    //比较是不是同一个ftp的内容
    @Override
    public boolean equals(Object o) {
        if (o!=null && o instanceof FtpInfo){
            FtpInfo fuser = (FtpInfo) o;
            if (this.host.equals(fuser.getHost()) &&
                    this.port == fuser.getPort() &&
                    this.userName.equals(fuser.getUserName()) &&
                    this.password.equals(fuser.getPassword())) {
                return true;
            } else {
                return false;
            }
        }
       return super.equals(o);
    }

    @Override
    public int hashCode() {
        return host.hashCode()+port+userName.hashCode()+password.hashCode();
    }
}
