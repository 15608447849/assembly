package m.lanshare;

import com.winone.ftc.mtools.NetworkUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by user on 2017/6/19.
 * 文件共享
 * 文件查询
 */
public class LANInfo {
    public static final String TAG = "UDP信息";
    private Path homeDir;
    private int minPort = 60000;
    private int maxPort = 65535;
    private int sendPort = 6999;
    private int multicsPort = 7999;
    private InetSocketAddress sendSocketAddress;
    private InetSocketAddress multicsSocketAddress;
    private InetSocketAddress broadAddress;
    public LANInfo(String home) {
        this.homeDir = Paths.get(home);
        this.initSocket();
    }
    public LANInfo(String home, int minPort, int maxPort, int sendPort, int multicsPort) {
        this.homeDir = Paths.get(home);
        this.minPort = minPort;
        this.maxPort = maxPort;
        this.sendPort = sendPort;
        this.multicsPort = multicsPort;
        this.initSocket();
    }
    //初始化广播
    private void initSocket(){
        InetAddress address = NetworkUtil.getLocalIPInet();
        sendSocketAddress = new InetSocketAddress(address, sendPort);//用于发送消息到服务器
        multicsSocketAddress = new InetSocketAddress(address, multicsPort);//用于发送消息到局域网广播
        broadAddress = new InetSocketAddress("255.255.255.255",multicsPort);// 组播地址
    }

    public int getMinPort() {
        return minPort;
    }

    public int getMaxPort() {
        return maxPort;
    }

    public InetSocketAddress getSendSocketAddress() {
        return sendSocketAddress;
    }

    public InetSocketAddress getMulticsSocketAddress() {
        return multicsSocketAddress;
    }

    public InetSocketAddress getBroadAddress() {
        return broadAddress;
    }

    public Path getHomeDir() {
        return homeDir;
    }
}
