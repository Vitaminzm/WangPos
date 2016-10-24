package com.symboltech.wangpos.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.symboltech.wangpos.http.HttpStringClient;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;

import java.util.LinkedList;
import java.util.List;

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