import bottle.ftc.tools.MD5Util;
import bottle.backup.slice.SliceInfo;
import bottle.backup.slice.SliceMapper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by user on 2017/11/21.
 */
public class test {
    public static void main(String[] args) throws IOException {

        String server_file = "C:\\Users\\user\\Desktop\\B";
        String client_file = "C:\\Users\\user\\Desktop\\A";
        String backup_file = "C:\\Users\\user\\Desktop\\C";
        //目标 服务端 同步 客户端文件

        /** 1 在服务端, 对服务端文件分片*/
        File file = new File(server_file);
        long size= file.length();
        int block = 1024;
        long slice_sum = size/ block;

       // 切分文件
        int mod = (int) (size % block);
        if (mod>0){
            slice_sum+=1;
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"r");
        ArrayList<SliceInfo> sliceList = new ArrayList<>();

        int len;
        long position = 0;
        for(int i = 0 ; i < slice_sum;i++ ){
            len = (int) Math.min(block,(randomAccessFile.length()-position));
            byte[] buffer = new byte[len];
            randomAccessFile.seek(position);
            randomAccessFile.read(buffer);
//            System.out.println("服务端 - 分片数据: "+ position+" - "+(position+len)+" : "+ new String(buffer));
            SliceInfo sliceBean = new SliceInfo();
            sliceBean.setPosition(position);
            sliceBean.setLength(len);
            sliceBean.setAdler32Hex(MD5Util.adler32Hex(buffer));
            sliceBean.setMd5Hex(MD5Util.getBytesMd5ByString(buffer));
            sliceList.add(sliceBean);
            position+= block;
        }
        randomAccessFile.close();
        //FileUtil.writeStringToFile(new Gson().toJson(sliceList),"C:\\Users\\user\\Desktop\\","切分数据",false);
        /**
         * ==========
         * 2 ...(S) 分片信息传输 中...-> (C)
         * ===========
         */
        System.out.println();
        /**
         * 3 客户端,分片信息处理
         */
        Hashtable<String,LinkedList<SliceInfo>> table = new Hashtable<>();
        for (SliceInfo sliceBean:sliceList){
            LinkedList list = table.get(sliceBean.getAdler32Hex());
            if (list==null){
                list  = new LinkedList();
            }
            list.add(sliceBean);
            table.put(sliceBean.getAdler32Hex(),list);
        }

        /**
         * 对客户端文件 进行滚动检测
         */
        randomAccessFile = new RandomAccessFile(client_file,"r");

        len = 0;
        position = 0;
        boolean moveBlock = false;
        long old_posistion = position;
        List<SliceMapper> indexList = new ArrayList<>();
        while(true){
//            System.out.println(pos);
            len = (int) Math.min(block,(randomAccessFile.length()-position));
            if (len<=0) break;

            randomAccessFile.seek(position);

            byte[] buf= new byte[len];
            randomAccessFile.read(buf);
//            System.out.println("客户端 - 滚动检测数据: "+ new String(buf));
            String adler32Hex = MD5Util.adler32Hex(buf);
            Iterator<Map.Entry<String,LinkedList<SliceInfo>>> iterator = table.entrySet().iterator();
            Map.Entry<String,LinkedList<SliceInfo>> entry;
            moveBlock = false;
            while (iterator.hasNext()){
                entry = iterator.next();
                if (entry.getKey().equalsIgnoreCase(adler32Hex)){
                    String md5 = MD5Util.getBytesMd5ByString(buf);
                    LinkedList list = entry.getValue();
                    Iterator<SliceInfo> it = list.iterator();
                    SliceInfo sliceBean;
                    while (it.hasNext()){
                        sliceBean = it.next();
                        if (sliceBean.getMd5Hex().equalsIgnoreCase(md5)){
//                            if (Integer.parseInt(sliceBean.getIndex()) == pos){

//                                System.out.println("    服务端文件 - 存在相同数据段: 本地位置: "+position+" - "+ (position+sliceBean.getLength()) +",>>> 服务端文件的位置"+sliceBean.getPosition()+" - "+ (sliceBean.getPosition()+sliceBean.getLength()) +", 不需要传输!");
                                SliceMapper mapper = new SliceMapper(0);
                                    mapper.setPosition(position);
                                    mapper.setLength(sliceBean.getLength());
                                    mapper.setSliceInfo(sliceBean);
                                indexList.add(mapper);
                                moveBlock = true;

                                it.remove();
                                break;
//                            }
                        }
                    }
                    if (list.size()==0){
                        iterator.remove();
                    }
                    if (moveBlock) break;
                }
            }
            if (old_posistion<position){
                //System.out.println("客户端完整文件 与服务端不匹配数据段: "+old_posistion+" - "+ position+" "+", 需要传输. 位置: "+sliceBean.getPosition()+" - "+ (sliceBean.getPosition()+sliceBean.getLength()));
//                System.out.println("客户端完整文件 与服务端不匹配数据段: "+old_posistion+" - "+ position+" "+", 需要传输.");
                SliceMapper mapperByNode = new SliceMapper(1);
                mapperByNode.setPosition(old_posistion);
                mapperByNode.setLength(position-old_posistion);
                if (moveBlock)  indexList.add(mapperByNode);

            }
            if (moveBlock){
                position+=block;//向后移动一块数据
                old_posistion = position;
            }else{
                position++; //向后偏移一字节
            }

//            System.out.println("pos: "+ position +" - "+ old_posistion);
        }
        randomAccessFile.close();
        //FileUtil.writeStringToFile(new Gson().toJson(indexList),"C:\\Users\\user\\Desktop\\","相同片段",false);

        //判断是否存在相同数据块 - 根据相同数据片段 获取不同数据片段的 下标-长度


        /**客户端开始 - 传输*/
        System.out.println();
       RandomAccessFile client = new RandomAccessFile(client_file, "r");
        RandomAccessFile server = new RandomAccessFile(server_file, "r");

        RandomAccessFile translation = new RandomAccessFile(backup_file,"rw");
        for (SliceMapper sliceMapper:indexList){
            translation.seek(sliceMapper.getPosition());
            if (sliceMapper.getType()==1){ //
                System.out.println("远程传输..."+ sliceMapper.getPosition()+" - "+ (sliceMapper.getPosition()+sliceMapper.getLength()) +", 长度:"+ sliceMapper.getLength() );
                byte[] buf = new byte[(int) sliceMapper.getLength()];

                client.seek(sliceMapper.getPosition());
                client.read(buf);
//                System.out.println("客户端 传输数据..."+ sliceMapper.getPosition()+" - "+ (sliceMapper.getPosition()+sliceMapper.getLength()) +", 长度:"+ sliceMapper.getLength()+", 数据:"+ new String(buf) );
                translation.write(buf);
            }else{

                SliceInfo info = sliceMapper.getSliceInfo();
//                System.out.println("本地存在片段: "+ info.getPosition()+" - "+ (info.getPosition()+info.getLength())+",  放在文件 "+sliceMapper.getPosition());

                byte[] buf = new byte[(int) info.getLength()];
                server.seek(info.getPosition());
                server.read(buf);
//                System.out.println("服务端 读取..."+info.getPosition()+" - "+ (info.getPosition()+info.getLength())+", 长度:"+info.getLength()+",数据:"+ new String(buf)+" --->>> " + sliceMapper.getPosition()+" - "+ (sliceMapper.getPosition()+sliceMapper.getLength()) );
                translation.write(buf);
            }
        }

        translation.close();
        client.close();
        server.close();

    }

}
