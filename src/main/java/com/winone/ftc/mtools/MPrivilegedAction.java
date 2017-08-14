package com.winone.ftc.mtools;

import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Created by user on 2017/6/12.
 */
public class MPrivilegedAction implements PrivilegedAction {
    private MappedByteBuffer bytebuffer;

    public MPrivilegedAction(MappedByteBuffer bytebuffer) {
        this.bytebuffer = bytebuffer;
        try {
            AccessController.doPrivileged(this);
        } catch (Exception e) {
            ;
        }
    }

    @Override
    public Object run() {
        try {
            if (bytebuffer==null) return null;
            Method getCleanerMethod = null;
            getCleanerMethod = bytebuffer.getClass().getMethod("cleaner",new Class[0]);
            getCleanerMethod.setAccessible(true);
            sun.misc.Cleaner cleaner = null;
            cleaner = (sun.misc.Cleaner)getCleanerMethod.invoke(bytebuffer,new Object[0]);
            cleaner.clean();
        } catch (Exception e) {
            try {
                // 加上这几行代码,手动unmap
                Method m = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
                m.setAccessible(true);
                m.invoke(FileChannelImpl.class, bytebuffer);
            } catch (Exception e1) {
            }
        }
        return null;
    }
}
