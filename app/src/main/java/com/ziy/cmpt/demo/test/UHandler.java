package com.ziy.cmpt.demo.test;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class UHandler implements Handler.Callback {
//    private static final String TAG = UHandler.class.getSimpleName();

//    private static int sAttachCallbackCount = 0;


    @Override
    public boolean handleMessage(Message msg) {
        try {

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    private UHandler() {
        mThread = new HandlerThread("zz.handler");
        mThread.start();
        mHandler = new Handler(mThread.getLooper(), this);
    }

    private static UHandler sUH = null;
    public static Handler getHandler() {
        if (null == sUH) {
            synchronized (UHandler.class) {
                if (null == sUH) {
                    try {
                        sUH = new UHandler();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sUH.mHandler;
    }
    private final Handler mHandler;
    private final HandlerThread mThread;

    public static void initHandler() {
        getHandler();
    }

    public static void sendMessage(int what) {
        sendMessage(what, 0);
    }

    public static void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0);
    }

    public static void sendMessage(int what, Object obj, long delay) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        sendMessage(msg, delay);
    }

    public static void sendMessage(int what, long delay) {
        Message msg = Message.obtain();
        msg.what = what;
        sendMessage(msg, delay);
    }

    public static void sendMessage(Message msg) {
        sendMessage(msg, 0);
    }

    public static void sendMessage(Message msg, long delay) {
        if (delay <= 0) {
            getHandler().sendMessage(msg);
        } else {
            getHandler().sendMessageDelayed(msg, delay);
        }
    }

    public static void remove(int what){
        getHandler().removeMessages(what);
    }

    public static void removeCallback(Runnable b) {
        getHandler().removeCallbacks(b);
    }

    public static void post(Runnable b) {
        getHandler().post(b);
    }

    public static void post(Runnable b, long delay) {
        if (delay <= 0) {
            getHandler().post(b);
        } else {
            getHandler().postDelayed(b, delay);
        }
    }
}
