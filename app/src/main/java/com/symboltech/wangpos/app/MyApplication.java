package com.symboltech.wangpos.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.service.RunTimeService;

import java.util.Timer;
import java.util.TimerTask;

import cn.weipass.pos.sdk.Weipos;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * Created by symbol on 2016/3/8.
 */
public class MyApplication extends Application {

    /**
     * APP context
     */
    public static Context context;

    private static Timer mTimer;

    private static TimerTask mTask;
    /**
     * 监听pos状态需要传递的收款员
     */
    private static String cashierId = "-1";

    public static String posType= ConstantData.POS_TYPE_Y ;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //初始化成功自身捕获异常
        //CrashHandler.getInstance().init(context);
        LogUtil.i("lgs", "Myapplication--------");
        checkNettimertask();
        if(posType.equals(ConstantData.POS_TYPE_W)){
            initSdk(context);
        }
    }

    /**
     * 初始化SDK
     *
     * @param context
     */
    public void initSdk(Context context) {
        /**
         * WeiposImpl的初始化（init函数）和销毁（destroy函数），
         * 最好分别放在一级页面的onCreate和onDestroy中执行。 其他子页面不用再调用，可以直接获取能力对象并使用。
         */
        WeiposImpl.as().init(context, new Weipos.OnInitListener() {

            @Override
            public void onInitOk() {
                LogUtil.e("lgs", "onInitOk--------------");
            }

            @Override
            public void onError(String message) {
                final String msg = message;
                LogUtil.e("lgs", "onError--------------" + msg);
            }

            @Override
            public void onDestroy() {
                LogUtil.e("lgs", "onDestroy--------------");
            }
        });
    }

    /**
     * 检测网络状态定时器
     *
     * @author CWI-APST email:26873204@qq.com
     * @Description: TODO
     */
    private void checkNettimertask() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTask == null) {
            mTask = new TimerTask() {
                @Override
                public void run() {
                    Intent serviceintent = new Intent(context, RunTimeService.class);
                    serviceintent.putExtra(ConstantData.CHECK_NET, false);
                    startService(serviceintent);
                }
            };
        }
    }

    public static void startTask(){
        mTimer.schedule(mTask, 0, AppConfigFile.NETWORK_STATUS_INTERVAL);
    }

    public static void stopTask(){
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    public static String getCashierId() {
        return cashierId;
    }

    public static void setCashierId(String cashierId) {
        MyApplication.cashierId = cashierId;
    }
}