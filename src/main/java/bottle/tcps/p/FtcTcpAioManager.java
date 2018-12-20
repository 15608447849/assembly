package bottle.tcps.p;

import java.util.List;

/**
 * Created by user on 2017/11/22.
 */
public interface FtcTcpAioManager {

    /**获取FTC TCP 管理对象*/
    FtcTcpAioManager getFtcTcpManager();
    //服务端相关
    List<SocketImp> getCurrentClientList();
    //服务端相关
    int getCurrentClientSize();
}
