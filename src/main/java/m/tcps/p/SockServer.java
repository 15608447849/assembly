package m.tcps.p;

import java.util.List;

/**
 * Created by user on 2017/8/22.
 */
public interface SockServer {
    List<SocketImp> getCurrentClientList();
    int getCurrentClientSize();
}
