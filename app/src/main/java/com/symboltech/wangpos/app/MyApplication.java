package com.symboltech.wangpos.app;

import android.app.Application;
import android.content.Context;

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
        LogUtil.i("lgs", "Myapplication--------");
    }
}