package com.ziy.cmpt.demo.test;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HookMain implements IXposedHookLoadPackage {
    private Object r1Obj = null;
    private static boolean sInit = false;

    private Map<String, BigModelNew> talkerModels = new HashMap<>();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("Loaded app: " + lpparam.packageName);
        Log.e("SparkChat","Loaded app:  " + lpparam.packageName);
        if (!sInit) {
            hookXIAO(lpparam);
            sInit = true;
        }
//        hook(lpparam);
    }
    private void hookXIAO(XC_LoadPackage.LoadPackageParam lpparam) throws ClassNotFoundException {

        Log.d("SparkChat","inject hook ui show obj");
        XposedHelpers.findAndHookConstructor("com.tencent.mm.ui.chatting.r1", lpparam.classLoader,
                Class.forName("f24.b", false, lpparam.classLoader),
                Class.forName("com.tencent.mm.pluginsdk.ui.chat.ChatFooter",false, lpparam.classLoader), java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("SparkChat","f24.b before " + lpparam.packageName);
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("SparkChat","f24.b after " + lpparam.packageName);
                r1Obj = param.thisObject;

                super.afterHookedMethod(param);
            }
        });

        Log.d("SparkChat","inject hook receiver msg");

        XposedHelpers.findAndHookMethod("t11.c", lpparam.classLoader, "z1",
                Class.forName("zf0.m$a", false, lpparam.classLoader),
                Class.forName("com.tencent.mm.storage.f4", false, lpparam.classLoader),
                java.lang.String.class,
                java.lang.String.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try {
//                    Log.e("SparkChat","t11.c hook");
                    Method getMsgIdMethod = ReflectUtils.getDeclaredMethod(param.args[1], "getMsgId");
                    long MsgId = (long) getMsgIdMethod.invoke(param.args[1]);
                    String strMsgId = String.valueOf(MsgId);
                    Log.d("SparkChat","receiver msg id " + strMsgId);

                    Method getContentMethod =  ReflectUtils.getDeclaredMethod(param.args[1], "getContent");
                    String content = (String) getContentMethod.invoke(param.args[1]);
                    Log.d("SparkChat","receiver content " + content);
//                    sendMsg();
                    UHandler.getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BigModelNew talkerModel;
                                if (talkerModels.containsKey(strMsgId)) {
                                    talkerModel = talkerModels.get(strMsgId);
                                } else {
                                    talkerModel = new BigModelNew();
                                    talkerModel.init(strMsgId, new BigModelNew.IMessageCallback() {
                                        @Override
                                        public void onMessage(String msg) {
                                            UHandler.getHandler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Log.d("SparkChat","UI show result text");
                                                        Method method = ReflectUtils.getDeclaredMethod(r1Obj, "c", String.class);
                                                        method.invoke(r1Obj, msg);
                                                    } catch (Throwable e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, 3000);
                                        }
                                    });
                                    talkerModels.put(strMsgId, talkerModel);
                                }
                                Log.d("SparkChat","tos Send msg to GPT");
                                talkerModel.sendMsg(content);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    }, 3000);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        Log.d("SparkChat","inject hook end");

    }

    private void sendMsg() {
        try {
            Method method = ReflectUtils.getDeclaredMethod(r1Obj, "c", String.class);
            method.invoke(r1Obj, "this is test");

        }catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void enableLog(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.Log$LogInstance",  lpparam.classLoader, "d", java.lang.String.class, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat","D:" + Arrays.toString(param.args));
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.Log$LogInstance",  lpparam.classLoader, "e", java.lang.String.class, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat","E:" + Arrays.toString(param.args));
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.Log$LogInstance",  lpparam.classLoader, "f", java.lang.String.class, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat","F:" + Arrays.toString(param.args));
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod("com.tencent.mm.sdk.platformtools.Log$LogInstance",  lpparam.classLoader, "i", java.lang.String.class, java.lang.String.class, java.lang.Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat","I:" + Arrays.toString(param.args));
                super.beforeHookedMethod(param);
            }
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }

    private void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        // 具体流程

        Class<?> clazz = XposedHelpers.findClass("com.tencent.mm.sdk.platformtools.Log", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(clazz, "getLogLevel", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(new Integer(0));
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod(clazz, "v", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(clazz, "e", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
                super.afterHookedMethod(param);
            }
        });


        XposedHelpers.findAndHookMethod(clazz, "f", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(clazz, "i", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
                super.afterHookedMethod(param);
            }
        });

        XposedHelpers.findAndHookMethod(clazz, "w", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
                super.afterHookedMethod(param);
            }
        });

//        Class<?> clazzLog= XposedHelpers.findClass("com.tencent.mm.sdk.platformtools.Log", lpparam.classLoader);
//        XposedHelpers.findAndHookMethod(clazzLog, "logD", long.class, String.class, String.class, String.class, int.class, int.class, long.class, long.class, String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
//                super.afterHookedMethod(param);
//            }
//        });
//
//        XposedHelpers.findAndHookMethod(clazzLog, "logE", long.class, String.class, String.class, String.class, int.class, int.class, long.class, long.class, String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
//                super.afterHookedMethod(param);
//            }
//        });
//
//
//        XposedHelpers.findAndHookMethod(clazzLog, "logF", long.class, String.class, String.class, String.class, int.class, int.class, long.class, long.class, String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
//                super.afterHookedMethod(param);
//            }
//        });
//
//        XposedHelpers.findAndHookMethod(clazzLog, "logI", long.class, String.class, String.class, String.class, int.class, int.class, long.class, long.class, String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
//                super.afterHookedMethod(param);
//            }
//        });
//
//        XposedHelpers.findAndHookMethod(clazzLog, "logV", long.class, String.class, String.class, String.class, int.class, int.class, long.class, long.class, String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
//                super.afterHookedMethod(param);
//            }
//        });
//
//        XposedHelpers.findAndHookMethod(clazzLog, "logW", long.class, String.class, String.class, String.class, int.class, int.class, long.class, long.class, String.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("wx-SparkChat-log","" + Arrays.toString(param.args));
//                super.afterHookedMethod(param);
//            }
//        });

    }
}