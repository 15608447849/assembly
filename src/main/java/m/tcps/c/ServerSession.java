package m.tcps.c;

import com.winone.ftc.mtools.StringUtil;
import m.bytebuffs.MyBuffer;
import m.tcps.p.*;
import m.tcps.s.FtcSocketServer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.CompletionHandler;

/**
 * Created by user on 2017/7/8.
 * 读取服务发送的内容 并且可以发送数据到服务器
 */
public class ServerSession extends Session {
    public ServerSession(SocketImp connect) {
        super(connect);
    }
}
