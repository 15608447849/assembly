package m.backup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by user on 2017/11/22.
 */
public class test2 {


    public static void main(String[] args) throws IOException {
        String file = "C:\\Users\\user\\Desktop\\B";
        RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
        randomAccessFile.seek(0);
        byte[] random = new byte[1024*4];
        for (int i = 0; i<random.length;i++){
            random[i] = (byte)0;
        }
        randomAccessFile.write(random);
        randomAccessFile.close();
    }
}
