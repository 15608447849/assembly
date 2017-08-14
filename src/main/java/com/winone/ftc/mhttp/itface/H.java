package com.winone.ftc.mhttp.itface;

/**
 * Created by lzp on 2017/5/12.
 *  毫无作用~
 */
public class H {

}





/**
 * *
 *
 *
 *
 *
 *    int recode = httpURLConnection.getResponseCode();
 //文件传送完毕
 if (recode == HttpURLConnection.HTTP_OK) {
 reader =  new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
 StringBuffer resultBuffer = new StringBuffer();
 String tempLine = null;
 while ((tempLine = reader.readLine()) != null) {
 resultBuffer.append(tempLine);
 resultBuffer.append("\n");
 }
 Log.i("文件上传返回结果:", resultBuffer.toString());

 }else{
 state.setError("文件上传返回值:"+recode);
 state.setState(-1);//失败
 }

 reader.close();
 *  if(reader!=null){
 try {
 reader.close();
 } catch (IOException e) {
 }
 }
 *
 *
 */