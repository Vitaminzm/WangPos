package com.symboltech.wangpos.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import com.symboltech.wangpos.http.HttpStringClient;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by symbol on 2016/3/8.
 */
public class MyApplication extends Application {

    /** APP context */
    public static Context context;
    /** activity manange list */
    public static List<Activity> activityList = new LinkedList<Activity>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        HttpStringClient.getinstance().initHttpConfig();
    }

    /*----------------------------------------------------------------*/
    /**
     * activity
     *
     * @author CWI-APST emial:26873204@qq.com
     * @Description: TODO(activity manage)
     * @param activity
     */
    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public static void delActivity(Activity activity) {
        activityList.remove(activity);
    }

    public static void exit() {
        for (Activity activity : activityList) {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    /**上一个下键盘*/
    public static HorizontalKeyBoard lastBoard;
    /**上一个editview  自定义下键盘使用*/
    public static EditText lastEditText;
    /**上一个textview  自定义下键盘使用*/
    public static TextView lastTextView;
    public static HorizontalKeyBoard getLastBoard() {
        return lastBoard;
    }

    public static void setLastBoard(HorizontalKeyBoard lastBoard) {
        MyApplication.lastBoard = lastBoard;
    }

    public static EditText getLastEditText() {
        return lastEditText;
    }

    public static void setLastEditText(EditText lastEditText) {
        MyApplication.lastEditText = lastEditText;
    }

    public static TextView getLastTextView() {
        return lastTextView;
    }

    public static void setLastTextView(TextView lastTextView) {
        MyApplication.lastTextView = lastTextView;
    }
}
