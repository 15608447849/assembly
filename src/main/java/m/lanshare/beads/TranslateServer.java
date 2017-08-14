package m.lanshare.beads;

import m.lanshare.LANManage;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.file.Path;

/**
 * Created by user on 2017/6/20.
 */
public class TranslateServer extends TranslateData{
    public SocketAddress clientCommunicationAddress;
    public SocketAddress clientDataAddress;
    public Path filePath;
    public int communicationPort;
    public int dataPort;
    public LANManage manage;
    public DatagramChannel communicationChannel;
    public DatagramChannel dataChannel;
}
