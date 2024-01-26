package com.ziy.cmpt.demo.test;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ReflectUtils {
//    private static final String TAG = "ReflectUtils";
    private static ClassLoader sClassLoader = null;
    public static void setDefaultClassLoader(ClassLoader cl) {
        if (null != cl) sClassLoader = cl;
    }
    public static ClassLoader getDefaultClassLoader() {
        return sClassLoader;
    }

    public static Class<?> findClass(String clsName) {
        return findClass(null, clsName, false);
    }

    public static Class<?> findClass(String clsName, boolean initialize) {
        return findClass(null, clsName, initialize);
    }

    public static Class<?> findClass(ClassLoader classLoader, String clsName) {
        return findClass(classLoader, clsName, false);
    }

    public static Class<?> findClass(ClassLoader classLoader, String clsName, boolean initialize) {
        Class<?> cls = null;
        if (null != classLoader) {
            try {
                cls = Class.forName(clsName, initialize, classLoader);
            } catch (Throwable e) {
            }
        }
        if (null != cls) return cls;

        if (null != sClassLoader && classLoader != sClassLoader) {
            try {
                cls = Class.forName(clsName, initialize, sClassLoader);
            } catch (Throwable e) {
            }
        }
        if (null != cls) return cls;

        ClassLoader vm = getVMStackLoader();
        if (null != vm && classLoader != vm) {
            try {
                cls = Class.forName(clsName, initialize, vm);
            } catch (Throwable e) {
            }
        }
        return cls;
    }

    private static ClassLoader sVMLoader = null;
    public static ClassLoader getVMStackLoader() {
        if (null == sVMLoader) {
            try {
                Method m = Class.forName("dalvik.system.VMStack").getDeclaredMethod("getCallingClassLoader");
                m.setAccessible(true);
                sVMLoader = (ClassLoader) m.invoke(null);
            } catch (Throwable e) {
            }
        }
        return sVMLoader;
    }

    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        return getDeclaredMethod(object.getClass(), methodName, parameterTypes);
    }

    public static Method getDeclaredMethod(String clsName, String methodName, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredMethod(findClass(clsName), methodName, parameterTypes);
    }

    public static Method getDeclaredMethod(ClassLoader classLoader, String clsName, String methodName, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredMethod(findClass(classLoader, clsName), methodName, parameterTypes);
    }

    public static Method getDeclaredMethod(Class<?> cls, String methodName, Class<?>... parameterTypes) {
        try {
            for (; cls != Object.class; cls = cls.getSuperclass()) {
                try {
                    Method m = cls.getDeclaredMethod(methodName, parameterTypes);
                    if (!m.isAccessible()) m.setAccessible(true);
                    return m;
                } catch (Throwable ignore) {
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Field getDeclaredField(Object object, String fieldName) {
        return getDeclaredField(object.getClass(), fieldName);
    }

    public static Field getDeclaredField(String clsName, String fieldName) throws Throwable {
        return getDeclaredField(findClass(clsName), fieldName);
    }

    public static Field getDeclaredField(ClassLoader cl, String clsName, String fieldName) throws Throwable {
        return getDeclaredField(findClass(cl, clsName), fieldName);
    }

    public static Field getDeclaredField(Class<?> cls, String fieldName) {
        if (null == cls) return null;
        for (; cls != Object.class; cls = cls.getSuperclass()) {
            try {
                Field f = cls.getDeclaredField(fieldName);
                if (!f.isAccessible()) f.setAccessible(true);
                return f;
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    public static Object getStaticFieldValue(String clsName, String name) {
        try {
            return getStaticFieldValue(findClass(clsName), name);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getStaticFieldValue(ClassLoader cl, String clsName, String name) {
        try {
            return getStaticFieldValue(findClass(cl, clsName), name);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getStaticFieldValue(Class<?> cls, String name) {
        return getFieldValue(cls, null, name);
    }

    public static Object getFieldValue(Object obj, String name) {
        if (null != obj) {
            return getFieldValue(obj.getClass(), obj, name);
        }
        return null;
    }

    /**
     * 如QQ中Field变量名一样，类型不一样
     */
    public static Object getFieldValue(Object obj, String name, Class<?> fType) {
        if (null != obj) {
            return getFieldValue(obj.getClass(), obj, name, fType);
        }
        return null;
    }

    public static Object getFieldValue(Class<?> cls, Object obj, String name, Class<?> fType) {
        try {
            Field fe = null;
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field fd : declaredFields) {
                if (fd.getName().equals(name) && fd.getType().getName().equals(fType.getName())) {
                    fd.setAccessible(true);
                    fe = fd;
                    break;
                }
            }
            if (null != fe) {
                return fe.get(obj);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean copyFields(Object objSrc, Object objDes) {
        Class<?> cls = objSrc.getClass();
        if (cls != objDes.getClass()) return false;

        Field[] ff;
        try {
            for (; null != cls && cls != Object.class; cls = cls.getSuperclass()) {
                ff = cls.getDeclaredFields();
                if (null != ff) {
                    for (Field f : ff) {
                        if (!f.isAccessible()) f.setAccessible(true);
                        f.set(objDes, f.get(objSrc));
                    }
                }
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Object getFieldValue(Class<?> cls, Object org, String name) {
        try {
            Field f = getDeclaredField(cls, name);
            if (null != f) {
                return f.get(org);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getLongField(Object org, String name) {
        try {
            Field f = getDeclaredField(org.getClass(), name);
            if (null != f) {
                return f.getLong(org);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static int getIntField(Object org, String name) {
        try {
            Field f = getDeclaredField(org.getClass(), name);
            if (null != f) {
                return f.getInt(org);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static boolean getBooleanField(Object org, String name) {
        try {
            Field f = getDeclaredField(org.getClass(), name);
            if (null != f) {
                return f.getBoolean(org);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setStaticFieldValue(String clsName, String name, Object val) {
        try {
            setStaticFieldValue(findClass(clsName), name, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setStaticFieldValue(ClassLoader cl, String clsName, String name, Object val) {
        try {
            setStaticFieldValue(findClass(cl, clsName), name, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setStaticFieldValue(Class<?> cls, String name, Object val) {
        try {
            setFieldValue(cls, name, null, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setFieldValue(Object obj, String name, Object val) {
        try {
            setFieldValue(obj.getClass(), name, obj, val);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void setFieldValue(Class<?> cls, String name, Object org, Object val) throws Throwable {
        Field f = getDeclaredField(cls, name);
        if (null != f) {
            f.set(org, val);
        }
    }

    public static Constructor<?> getDeclaredConstructor(String clsName, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredConstructor(findClass(clsName), parameterTypes);
    }

    public static Constructor<?> getDeclaredConstructor(ClassLoader cl, String clsName, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredConstructor(findClass(cl, clsName), parameterTypes);
    }

    public static Constructor<?> getDeclaredConstructor(Object obj, Class<?>... parameterTypes) throws Throwable {
        return getDeclaredConstructor(obj.getClass(), parameterTypes);
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> cls, Class<?>... parameterTypes) throws Throwable {
        Constructor<?> constructor = cls.getDeclaredConstructor(parameterTypes);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor;
    }

    private static boolean sameParameterType(Class<?>[] params1, Class<?>[] params2) {
        if (null == params1) params1 = new Class[]{};
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (!params1[i].getName().equals( params2[i].getName()))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static Method getDeclaredMethod(String clsName, String methodName, String returnType,
                                           Class<?>... parameterTypes){
        return getDeclaredMethod(findClass(clsName), methodName, returnType, parameterTypes);
    }

    public static Method getDeclaredMethod(ClassLoader cl, String clsName, String methodName, String returnType,
                                           Class<?>... parameterTypes){
        return getDeclaredMethod(findClass(cl, clsName), methodName, returnType, parameterTypes);
    }

    public static Method getDeclaredMethod(Object obj, String methodName, String returnType,
                                           Class<?>... parameterTypes){
        return getDeclaredMethod(obj.getClass(), methodName, returnType, parameterTypes);
    }

    public static Method getDeclaredMethod(Class<?> cls, String methodName, String returnType,
                                           Class<?>... parameterTypes) {
        try {
            for (; cls != Object.class; cls = cls.getSuperclass()) {
                Method[] mtds = cls.getDeclaredMethods();
                for (Method m : mtds) {
                    if (!m.getName().equals(methodName)) continue;
                    if (!m.getReturnType().getName().equals(returnType)) continue;

                    if (sameParameterType(parameterTypes, m.getParameterTypes())) {
                        if (!m.isAccessible()) m.setAccessible(true);
                        return m;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
