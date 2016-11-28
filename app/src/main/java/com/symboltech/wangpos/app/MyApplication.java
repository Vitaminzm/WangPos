package com.symboltech.wangpos.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.service.RunTimeService;

import java.util.Timer;
import java.util.TimerTask;

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
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //初始化成功自身捕获异常
        //CrashHandler.getInstance().init(context);
        LogUtil.i("lgs", "Myapplication--------");
        checkNettimertask();
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