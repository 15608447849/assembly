package bottle.ftc.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/5/13.
 */
public class StringUtil {
    //字符串不为空
    public static boolean isEntry(String str){
        return str==null || str.trim().length() == 0 ;
    }
    //url编码
    private static boolean isUnescaped(char ch, boolean component) {
        return (65 > ch || ch > 90) && (97 > ch || ch > 122) && (48 > ch || ch > 57)?("-_.!~*'()".indexOf(ch) >= 0?true:(!component?";/?:@&=+$,#".indexOf(ch) >= 0:false)):true;
    }
    private static String toHexEscape(int u0) {
        int u = u0;
        byte[] b = new byte[6];
        int len;
        if(u0 <= 127) {
            b[0] = (byte)u0;
            len = 1;
        } else {
            len = 2;
            int i;
            for(i = u0 >>> 11; i != 0; i >>>= 5) {
                ++len;
            }

            for(i = len - 1; i > 0; --i) {
                b[i] = (byte)(128 | u & 63);
                u >>>= 6;
            }

            b[0] = (byte)(~((1 << 8 - len) - 1) | u);
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len; ++i) {
            sb.append('%');
            if((b[i] & 255) < 16) {
                sb.append('0');
            }

            sb.append(Integer.toHexString(b[i] & 255).toUpperCase());
        }
        return sb.toString();
    }

    /**
     *
     * @param string
     * @param component false 不编码特殊字符 ;/?:@&=+$,#
     * @return
     */
    private static String encode(String string, boolean component) {
        if(string.isEmpty()) {
            return string;
        } else {
            int len = string.length();
            StringBuilder sb = new StringBuilder();
            for(int k = 0; k < len; ++k) {
                char C = string.charAt(k);
                if(isUnescaped(C, component)) {
                    sb.append(C);
                } else {
                    if(C >= '\udc00' && C <= '\udfff') {
                        return null;
                    }
                    int V;
                    if(C >= '\ud800' && C <= '\udbff') {
                        ++k;
                        if(k == len) {
                            return null;
                        }

                        char kChar = string.charAt(k);
                        if(kChar < '\udc00' || kChar > '\udfff') {
                            return null;
                        }
                        V = (C - '\ud800') * 1024 + (kChar - '\udc00') + 65536;
                    } else {
                        V = C;
                    }
                    try {
                        sb.append(toHexEscape(V));
                    } catch (Exception var9) {
                        return null;
                    }
                }
            }
            return sb.toString();
        }
    }

    public static String encodeUrl(String uri){
        return encode(uri,false);
    }

    public static String map2string(HashMap<String,String> map){
        if (map == null || map.isEmpty()) return "";
        Iterator<Map.Entry<String,String>> iterator = map.entrySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        Map.Entry<String,String> entry;
        while (iterator.hasNext()){
            entry = iterator.next();
            stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("#");
        }
        String value = stringBuffer.toString();
        return value.substring(0,value.length()-1);
    }

    public static HashMap<String, String> string2map(String value){
        if (value == null || value.isEmpty()) return null;
        String strArr[] = value.split("#");
        String subArr[];
        HashMap<String,String> map = new HashMap<>();
        for (String string : strArr){
            subArr = string.split("=");
            map.put(subArr[0],subArr[1]);
        }
        return map;

    }
//    private static final String IP_PATTERN = "\\d+\\.\\d+\\.\\d+\\.\\d+";
//    private static final String DOMAIN_PATTERN = "[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)[^/]*";
    private static final String DOMAIN_PATTERN  = "(?<=://)([\\w-]+\\.)+[\\w-]+(?<=/?)";
    public static String matchIpAddress(String url) {
        try {
            if (url.indexOf("@")>0){
                url = url.substring(url.indexOf("@"),url.length()).replace("@","://");
            }
            Pattern p = Pattern.compile(DOMAIN_PATTERN,Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(url);
            if (m.find()) {
                return m.group();
            }
        } catch (Exception e) {
        }
        return "www.baidu.com";
    }


    public static String filter(String s) {
        return s.trim();
    }
}
