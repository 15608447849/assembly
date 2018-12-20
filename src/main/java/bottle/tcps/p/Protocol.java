package bottle.tcps.p;

import bottle.ftc.tools.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by user on 2017/7/8.
 * ASCII 控制字符
 * bin     dec
 * 00000000	0		NUL(null)	空字符
 00000001	1		SOH(start of headling)	标题开始
 00000010	2		STX (start of text)	正文开始
 00000011	3		ETX (end of text)	正文结束
 00000100	4		EOT (end of transmission)	传输结束
 00000101	5		ENQ (enquiry)	请求
 00000110	6		ACK (acknowledge)	收到通知
 00000111	7		BEL (bell)	响铃
 00001000	8		BS (backspace)	退格
 00001001	9		HT (horizontal tab)	水平制表符
 00001010	10		LF (NL line feed, new line)	换行键
 00001011	11		VT (vertical tab)	垂直制表符
 00001100	12		FF (NP form feed, new page)	换页键
 00001101	13		CR (carriage return)	回车键
 00001110	14		SO (shift out)	不用切换
 00001111	15		SI (shift in)	启用切换
 00010000	16		DLE (data link escape)	数据链路转义
 00010001	17		DC1 (device control 1)	设备控制1
 00010010	18		DC2 (device control 2)	设备控制2
 00010011	19		DC3 (device control 3)	设备控制3
 00010100	20		DC4 (device control 4)	设备控制4
 00010101	21		NAK (negative acknowledge)	拒绝接收
 00010110	22		SYN (synchronous idle)	同步空闲
 00010111	23		ETB (end of trans. block)	传输块结束
 00011000	24		CAN (cancel)	取消
 00011001	25		EM (end of medium)	介质中断
 00011010	26		SUB (substitute)	替补
 00011011	27		ESC (escape)	溢出
 00011100	28		FS (file separator)	文件分割符
 00011101	29		GS (group separator)	分组符
 00011110	30		RS (record separator)	记录分离符
 00011111	31		US (unit separator)	单元分隔符
 01111111   127     DEL (delete)	删除
 */
public class Protocol {
    public static  final int MTU = 1500-20-8;

    public static final byte NUL = 0;//空字符
    public static final byte SOH = 1;//
    public static final byte STX = 2;//正文开始
    public static final byte ETX = 3;//正文结束
    public static final byte EOT = 4;//传输结束
    public static final byte ENQ = 5;//请求
    public static final byte ACK = 6;//收到通知

    public static final byte BEL = 7;
    public static final byte BS = 8;
    public static final byte HT = 9;
    public static final byte LF = 10;//换行
    public static final byte VT = 11;
    public static final byte FF = 12;
    public static final byte CR = 13;//回车键
    public static final byte SO = 14;
    public static final byte SI = 15;
    public static final byte DLE = 16;
    public static final byte DC1 = 17;
    public static final byte DC2 = 18;
    public static final byte DC3 = 19;
    public static final byte DC4 = 20;
    public static final byte NAK = 21;//拒绝接收
    public static final byte SYN = 22;//同步空闲
    public static final byte ETB = 23;//传输块结束
    public static final byte CAN = 24;//取消
    public static final byte EM = 25;
    public static final byte SUB = 26;
    public static final byte ESC = 27;
    public static final byte FS = 28;//文件分割符
    public static final byte GS = 29;
    public static final byte RS = 30;
    public static final byte US = 30;
    public static final byte DEL = 127;





    public static String getBit(byte by){
        StringBuffer sb = new StringBuffer();
        sb.append((by>>7)&0x1)
                .append((by>>6)&0x1)
                .append((by>>5)&0x1)
                .append((by>>4)&0x1)
                .append((by>>3)&0x1)
                .append((by>>2)&0x1)
                .append((by>>1)&0x1)
                .append((by>>0)&0x1);
        return sb.toString();
    }

    public static int byteArrayToInt(byte[] b,int offset) {

        return   b[offset+3] & 0xFF |
                (b[offset+2] & 0xFF) << 8 |
                (b[offset+1] & 0xFF) << 16 |
                (b[offset] & 0xFF) << 24;
    }
    public static byte[] stringToAscii(String string) {
            byte[] bytes = new byte[string.length()];
            for(int i = 0; i < string.length();i++){
                bytes[i] = (byte) string.charAt(i);
            }
            return bytes;
    }
    public static String asciiToString(byte[] ascii){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ascii.length;i++){
            stringBuilder.append((char)ascii[i]);
        }
        return stringBuilder.toString();
    }


    public static void main(String[] args){
        byte EOT =8;
        byte[] a = new String(" ").getBytes();
        Log.i(getBit(EOT));
        String s = "GBK";
        byte[] d = stringToAscii(s);

        Log.i(Arrays.toString(d),asciiToString(d));
    }

    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
    public static void protocol(ByteBuffer buf,byte ptl,int content_len) {
        buf.put(NUL);
        buf.put(ENQ);
        buf.put(NUL);
        buf.put(ptl);
        byte[] len_bytes = intToByteArray(content_len);
        for (int i = 0;i<len_bytes.length;i++){
            buf.put(len_bytes[i]);
        }
    }
}
