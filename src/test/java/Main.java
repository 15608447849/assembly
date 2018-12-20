import bottle.tcps.s.FtcSocketServer;
import bottle.ftc.core.imps.FtcManager;

import bottle.ftc.entity.mbean.entity.ManagerParams;
import bottle.ftc.entity.mbean.entity.State;
import bottle.ftc.entity.mbean.entity.Task;
import bottle.ftc.entity.mbean.entity.TaskFactory;

import bottle.ftc.tools.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

public class Main{
    public static void main(String[] args) throws Exception {
//        test();
//        ser();
//        cli();
//        execute();
//        upload();
//        uploadFTP();
//        TEST_();
        httpDown();

//        down("http://172.16.0.201:8080/lee/1.zip");
//        down(args[0]);

//        ticServer();
        while (true);
    }


    private static void down(String url) {

        FtcManager.get().execute(TaskFactory.httpTaskDown(
                url,
                "GET",
                "./temp",
                url.substring(url.lastIndexOf("/")+1),
                true,"2d29de32451df9fe070e2e3e4e7a3dc3"));
    }
    private static void httpDown() {
        FtcManager.get().initial(new ManagerParams(4,false,true,false));
        FtcManager.get().execute(
                TaskFactory.httpTaskDown(
                 "http://pic1.win4000.com/pic/5/8e/e3d0f556ea.jpg?down",
                "GET",
                "C:\\",
                "1020116405-6.jpg",
                true)

//                        .setDownloadLimitMax(200)
        );
//        ManagerImp.get().execute(TaskFactory.httpTaskDown(" http://mxd.clientdown.sdo.com.sd.qcloudcdn.com/145/Data145.zip",
//                "GET",
//                "C:\\FileServerDirs\\TEST",
//                "Data145.zip",
//                true));
//        ManagerImp.get().execute(TaskFactory.httpTaskDown("http://www.icbc.com.cn/SiteCollectionDocuments/ICBC/Resources/ICBC/sy/photo/2014new/gj3.jpg",
//                "GET",
//                "C:\\FileServerDirs\\TEST",
//                "1.jpg",
//                true));

//        /**
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-1.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-2.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-3.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-4.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-5.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-6.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-7.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-8.pdf
//         http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-9.pdf
//         */
//
//       for (int i = 1;i<=9;i++){
//           ManagerImp.get().execute(TaskFactory.httpTaskDown("http://dzb.scdaily.cn/pdf/2017/0825/2017-08-25-"+i+".pdf",
//                   "GET",
//                   "C:\\FileServerDirs\\TEST\\pdf",
//                   i+".pdf",
//                   true));
//       }

//        Task task =  TaskFactory.httpTaskDown("http://172.16.0.201:8080/lee/1.zip",
//                "GET",
//                "C:\\FileServerDirs\\TEST",
//                "单面茶几.zip",
//                false);
//                ManagerImp.get().execute(
//                        task
//                );


//        while (true);
    }
    private static void test() {

        String url= "ftp://admin:admin@172.16.0.248:9595/screencut/001430102001009.png";

        Task task = TaskFactory.ftpTaskUpdate(url,"C:\\FileServerDirs\\source","723dd72fjw1f4mrw5wlcmj22yo4g0e88.jpg");





        FtcManager.get().execute(task);




    }
    private static int success=0,fait=0;
    private static void S(){
        Log.i("成功:"+success+" ,失败:"+ fait);
    }
    private static void uploadFTP() {
        FtcManager.get().execute(
                TaskFactory.ftpTaskUpdate(
                        "ftp://admin:admin@192.168.0.240:9999/a/b/c/d/1.txt",
                        "C:\\FileServerDirs\\source",
                        "aaa"
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
                FtcManager.get().execute(
                TaskFactory.httpTaskUpdate(
                        "http://192.168.1.240:8090/fileUpload",
                        "POST",
                        "E:\\迅雷下载",
                        "WIN.7_SP1_.X64_V2018.iso",
                        "iso","WIN.7_SP1_.X64_V2018.iso").setFormName("file")
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
                        FtcManager.get().execute(
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

                FtcManager.get().execute(TaskFactory.httpTaskDown(
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
        FtcManager.get().execute(TaskFactory.httpTaskDown(
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
        FtcManager.get().execute(TaskFactory.httpTaskDown(
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
        FtcManager.get().execute(TaskFactory.httpTaskDown(
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

    private static void ticServer() {
        try {
            FtcSocketServer ftcSocketServer = new FtcSocketServer(new
                    InetSocketAddress("192.168.1.126", 7777), null);
            ftcSocketServer.openListener().launchAccept();
            while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}
