package com.symboltech.wangpos.app;

import android.app.Application;
import android.content.Context;

import com.symboltech.wangpos.exception.CrashHandler;
import com.symboltech.wangpos.log.LogUtil;

/**
 * Created by symbol on 2016/3/8.
 */
public class MyApplication extends Application {

    /**
     * APP context
     */
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //初始化成功自身捕获异常
        CrashHandler.getInstance().init(context);
        LogUtil.i("lgs", "Myapplication--------");
    }
}