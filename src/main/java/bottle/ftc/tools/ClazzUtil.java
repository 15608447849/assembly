package bottle.ftc.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/5/19.
 */
public class ClazzUtil {


    public static Object newInstance(String clazzName, Class[] clazzArr, Object[] paramArr){

        try {
            Class clazz =Class.forName(clazzName);
            Constructor constructor = clazz.getConstructor(clazzArr);
            Object object = constructor.newInstance(paramArr);
            return object;
        } catch (ClassNotFoundException e) {
            ;
        } catch (NoSuchMethodException e) {
            ;
        } catch (IllegalAccessException e) {
            ;
        } catch (InstantiationException e) {
            ;
        } catch (InvocationTargetException e) {
            ;
        }
        return null;
    }

    public static void invokeMethod(Object object,String methodName,Class[] paramType,Object[] paramList){
        try {
            Class clazz = object.getClass();
            Method method = clazz.getMethod(methodName,paramType);
            method.invoke(object,paramList);
        } catch (NoSuchMethodException e) {
            ;
        } catch (InvocationTargetException e) {
            ;
        } catch (IllegalAccessException e) {
            ;
        }
    }

}
