package com.winone.ftc.lunch;

import com.winone.ftc.mcore.imps.ManagerImp;

import com.winone.ftc.mentity.mbean.entity.ManagerParams;
import com.winone.ftc.mentity.mbean.entity.State;
import com.winone.ftc.mentity.mbean.entity.Task;
import com.winone.ftc.mentity.mbean.entity.TaskFactory;

import com.winone.ftc.mtools.Log;

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
    }

    private static void down(String url) {

        ManagerImp.get().execute(TaskFactory.httpTaskDown(url,
                "GET",
                "./temp",
                url.substring(url.lastIndexOf("/")+1),
                true,"2d29de32451df9fe070e2e3e4e7a3dc3"));
    }

    private static void httpDown() {
        ManagerImp.get().initial(new ManagerParams(4,false,true,false));

        ManagerImp.get().execute(TaskFactory.httpTaskDown("http://bfo.clientdown.sdo.com/GA_2.26.2.0_20170818/GA_client_2.26.2.0_20170818.exe",
                "GET",
                "C:\\FileServerDirs\\TEST",
                "rxyh.exe",
                true).setDownloadLimitMax(200));
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






}
