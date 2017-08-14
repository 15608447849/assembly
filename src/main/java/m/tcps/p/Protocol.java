package m.tcps.p;

/**
 * Created by user on 2017/7/8.
 */
public class Protocol {
    public static  final int MTU = 1500-20-8;
    public static final byte DATA_END = '\r';//回车
    public static final byte DATA_CLEAR = '\n';//换行
    public static final byte STRING = '&';//String
}
