package com.winone.ftc.lunch;

import com.winone.ftc.mcore.imps.ManagerImp;

import com.winone.ftc.mentity.mbean.ManagerParams;
import com.winone.ftc.mentity.mbean.State;
import com.winone.ftc.mentity.mbean.Task;
import com.winone.ftc.mentity.mbean.TaskFactory;

import com.winone.ftc.mtools.Log;
import com.winone.ftc.mtools.NetworkUtil;

import m.tcps.c.FtcSocketClient;
import m.tcps.p.CommunicationAction;
import m.tcps.p.Op;
import m.tcps.p.Session;
import m.tcps.s.FtcSocketServer;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main{
    public static void main(String[] args) throws Exception {
//        test();
//        ser();
//        cli();
//        execute();
//        upload();
//        uploadFTP();
//        TEST_();
    }

    private static void test() {

        String url= "ftp://admin:admin@172.16.0.248:9595/screencut/001430102001009.png";

        Task task = TaskFactory.ftpTaskUpdate(url,"C:\\FileServerDirs\\source","723dd72fjw1f4mrw5wlcmj22yo4g0e88.jpg");

        task.setOnResult(new Task.onResultAdapter() {
            @Override
            public void onSuccess(State state) {
                Log.i(state.getTask().toString());
            }

            @Override
            public void onFail(State state) {
                Log.i(state.getTask().toString());
            }

            @Override
            public void onLoading(State state) {
            }
        });

//        ManagerImp.get().execute(task);

       url =  "ftp://admin:admin@172.16.0.248:9595/screencut/001430102001009/1502089362270.png";

        task = TaskFactory.ftpTaskUpdate(url,"C:\\FileServerDirs\\source","2132505358-31.jpg");

        task.setOnResult(new Task.onResultAdapter() {
            @Override
            public void onSuccess(State state) {
                Log.i(state.getTask().toString());
            }

            @Override
            public void onFail(State state) {
                Log.i(state.getTask().toString());
            }

            @Override
            public void onLoading(State state) {
            }
        });

        ManagerImp.get().execute(task);




    }




















    private static int success=0,fait=0;
    private static void S(){
        Log.i("成功:"+success+" ,失败:"+ fait);
    }



    private static void uploadFTP() {
        ManagerImp.get().execute(
                TaskFactory.ftpTaskUpdate(
                        "ftp://admin:admin@172.16.0.100:21/LIZHAOPING/1.png",
                        "C:\\FileServerDirs\\source",
                        "bg_1.jpg"
                       ).setOnResult(new Task.onResultAdapter() {
                    @Override
                    public void onSuccess(State state) {
                        Log.i("成功");
                    }

                    @Override
                    public void onFail(State state) {
                       Log.i("失败");
                    }
                })
        );
    }

    private static void upload() {
                ManagerImp.get().execute(
                TaskFactory.httpTaskUpdate(
                        "http://172.16.0.200:8080/ftc/upload",
                        "POST",
                        "C:\\FileServerDirs\\source",
                        "bg_1.jpg",
                        "李兆平","测试.png")
                );
    }

    private static void execute() {

        HashMap<String,String> map = new HashMap<>();
        map.put("Referer","http://mall.icbc.com.cn");
//        ManagerImp.get().execute(
//                TaskFactory.httpTaskDown(
//                        "http://image8.mall.icbc.com.cn/image/10019627/1495886753052.jpg",
//                        "GET",
//                        "C:\\FileServerDirs",
//                        "1495886753052.jpg",
//                        map,
//                        false));
//        ManagerImp.get().execute(
//                TaskFactory.httpTaskDown(
//                        "http://image8.mall.icbc.com.cn/image/10002371/1468306750798.jpg",
//                        "GET",
//                        "C:\\FileServerDirs",
//                        "1468306750798.jpg",
//                        map,
//                        false));
//                        ManagerImp.get().execute(
//                TaskFactory.httpTaskDown(
//                        "http://image5.mall.icbc.com.cn/image/10015276/1491817777108.jpg",
//                        "GET",
//                        "C:\\FileServerDirs",
//                        "1491817777108.jpg",
//                        map,
//                        false));
                        ManagerImp.get().execute(
                TaskFactory.httpTaskDown(
                        "http://image8.mall.icbc.com.cn/image/10022723/1498528158092_4.png",
                        "GET",
                        "C:\\FileServerDirs",
                        "1498528158092_4.jpg",
                        map,
                        false).setOnResult(new Task.onResultAdapter() {
                    @Override
                    public void onSuccess(State state) {
                        Log.i("成功0:  "+ state);
                    }
                }));

//                        ManagerImp.get().execute(
//                TaskFactory.httpTaskDown(
//                        "http://image7.mall.icbc.com.cn/image/10007734/1468487082844_4.jpg",
//                        "GET",
//                        "C:\\FileServerDirs",
//                        "1468487082844_4.jpg",
//                        map,
//                        false));

                ManagerImp.get().execute(TaskFactory.httpTaskDown(
                        "http://image8.mall.icbc.com.cn/image/10022723/1498528158092_4.png",
                        "GET",
                        "C:\\FileServerDirs",
                        "1498528158092_4.jpg",
                        map,
                        false).setOnResult(new Task.onResultAdapter() {
                    @Override
                    public void onSuccess(State state) {
                        Log.i("成功1 :  "+ state);
                    }
                }));
        ManagerImp.get().execute(TaskFactory.httpTaskDown(
                "http://image8.mall.icbc.com.cn/image/10022723/1498528158092_4.png",
                "GET",
                "C:\\FileServerDirs",
                "1498528158092_4.jpg",
                map,
                false).setOnResult(new Task.onResultAdapter() {
            @Override
            public void onSuccess(State state) {
                Log.i("成功2 :  "+ state);
            }
        }));
        ManagerImp.get().execute(TaskFactory.httpTaskDown(
                "http://image8.mall.icbc.com.cn/image/10022723/1498528158092_4.png",
                "GET",
                "C:\\FileServerDirs",
                "1498528158092_4.jpg",
                map,
                false).setOnResult(new Task.onResultAdapter() {
            @Override
            public void onSuccess(State state) {
                Log.i("成功3 :  "+ state);
            }
        }));
        ManagerImp.get().execute(TaskFactory.httpTaskDown(
                "http://image8.mall.icbc.com.cn/image/10022723/1498528158092_4.png",
                "GET",
                "C:\\FileServerDirs",
                "1498528158092_4.jpg",
                map,
                false).setOnResult(new Task.onResultAdapter() {
            @Override
            public void onSuccess(State state) {
                Log.i("成功4 :  "+ state);
            }
        }));

//                        ManagerImp.get().execute(
//                TaskFactory.httpTaskDown(
//                        "http://image6.mall.icbc.com.cn/image/10006845/1499061518473.jpg",
//                        "GET",
//                        "C:\\FileServerDirs",
//                        "1499061518473.jpg",
//                        map,
//                        false));
    }

    private static void cli() {
        FtcSocketClient client = new FtcSocketClient(new InetSocketAddress("172.16.0.248", 65000), new CommunicationAction() {
            @Override
            public void connectSucceed(Session session) {
                Log.i("连接成功:"+ session.getSocket());
//                session.writeString("CATCH%precious_metal.PreciousMetal,credit_card.CreditCard");
                session.writeString("LAUNCH#00100%credit_card.CreditCard");
                session.writeString("LAUNCH#00101%fund.Fund");
                session.writeString("LAUNCH#00101%financial.Financial");
            }

            @Override
            public void receiveString(Session session, Op operation, String message) {
                Log.i("收到服务器消息: "+ message);
            }
        });
        client.connectServer();

        ManagerImp.get();
    }

    private static void ser() {
            ManagerImp.get().initial(new ManagerParams(false,true,false));
        try {
//            NetworkUtil.getLocalIPInet();
            List<InetAddress> list = NetworkUtil.getLocalIPInetList();

            for (InetAddress ip : list){
                InetSocketAddress address = new InetSocketAddress(ip,65000);
                FtcSocketServer socketServer = new FtcSocketServer(address,(new CommunicationAction() {
                    @Override
                    public void connectSucceed(Session session) {
                        Log.i("收到一个连接: "+ session);
                    }

                    @Override
                    public void receiveString(Session session, Op operation, String message) {
                        Log.i("收到消息: "+ message);
                        operation.writeString(message);
                    }
                })).openListener().launchAccept();
            }

        } catch (IOException e) {
            Log.i("启动TCP服务器失败.");
        }
    }



}
