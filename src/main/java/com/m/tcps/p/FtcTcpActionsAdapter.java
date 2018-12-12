package com.m.tcps.p;

import com.winone.ftc.mtools.Log;

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

    }
    //客户端 - 连接失败
    @Override
    public void connectFail(Session session) {
        if (session!=null){
            if (session.getSocketImp()!=null){
                session.getSocketImp().close();
            }
        }
    }

    @Override
    public void error(Session session, Throwable throwable, Exception e) {

        if (session!=null){
            session.close();
        }
//        if (throwable!=null){
//            throwable.printStackTrace();
//        }
//        if (e!=null){
//            e.printStackTrace();
//        }
    }
}
