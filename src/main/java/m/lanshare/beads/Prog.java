package m.lanshare.beads;

/**
 * Created by user on 2017/6/20.
 */
public class Prog {
    public static final int DATA_BUFFER_MAX_ZONE = 1500-20; //MTU - ip头
    public static final int UDP_DATA_MIN_BUFFER_ZONE = 576-20-8;// intenet标准MTU - IP头 -UDP头




    public static final byte notifyServerCreateConnect = 100;
    public static final byte notifyServerCreateConnectResp = 101;
    public static final byte sendDataInfo = 103;
    public static final byte sendDataInfoResp = 104;
    public static final byte mtuCheck = 105;
    public static final byte mtuSure = 106;
    public static final byte dataAccess = 107;
    public static final byte dataComplete = 108;



}
