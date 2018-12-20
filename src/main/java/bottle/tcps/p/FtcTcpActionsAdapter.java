package bottle.tcps.p;

/**
 * Created by user on 2017/11/23.
 */
public abstract class FtcTcpActionsAdapter implements FtcTcpActions{
    @Override
    public void connectSucceed(Session session) {

    }

    @Override
    public void receiveString(Session session, String message) {

    }

    @Override
    public void receiveBytes(Session session, byte[] bytes) {

    }
    //连接关闭
    @Override
    public void connectClosed(Session session) {
        if (session!=null){
            session.close();
        }
    }
    //客户端 - 连接失败
    @Override
    public void connectFail(Session session) {

    }

    @Override
    public void error(Session session, Throwable throwable, Exception e) {

//        if (throwable!=null){
//            throwable.printStackTrace();
//        }
//        if (e!=null){
//            e.printStackTrace();
//        }
    }
}
