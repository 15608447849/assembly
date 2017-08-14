package m.lanshare.beads;

import java.util.HashMap;

/**
 * Created by user on 2017/6/20.
 */
public class TranslateData  {

    //数据大小
    public long dataLength;
    //数据起点
    public long startPos;
    //数据终点
    public long endPos;
    public int mtu;
    public HashMap<Integer,Long> dataSliceMap;
}
