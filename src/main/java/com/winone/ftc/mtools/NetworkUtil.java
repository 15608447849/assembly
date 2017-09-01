package com.winone.ftc.mtools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by lzp on 2017/5/13.
 * 其他工具
 * 检测网络
 */
public class NetworkUtil {

    public static boolean ping(String address){

        if (address==null || "".equals(address)) return false;
        boolean connect = false;
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        BufferedReader br = null;
        try {
            process = runtime.exec("ping " + address);
            br = new BufferedReader(new InputStreamReader(process.getInputStream(),"GBK"));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            Log.i(sb.toString());
            if (!sb.toString().equals("")) {
                line = "";
                if (sb.toString().indexOf("TTL") > 0) {
                    // 网络畅通
                    connect = true;
                } else {
                    // 网络不畅通
                    connect = false;
                }
            }
        } catch (IOException e) {
        }finally {
            if (br!=null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (process!=null){
                process.destroy();
            }
        }
        return connect;
    }

    /**
     * 得到本地IP
     * @return
     */
    public static  List<InetAddress> getLocalIPInetList() {
        List<InetAddress> inetAddressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni =  e1.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address){continue;}
                        inetAddressList.add(ia);
                    }
                }
            }
        } catch (SocketException e) {
        }
        return inetAddressList;
    }
    /**
     * 得到本地IP
     * @return
     */
    public static  InetAddress getLocalIPInet() {
        try {
            Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces();
            while (e1.hasMoreElements()) {
                NetworkInterface ni = e1.nextElement();
                System.out.println(ni);
                if (ni.isUp() && !ni.isLoopback()) {
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address){continue;}
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
    }
}
