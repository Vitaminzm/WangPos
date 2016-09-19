package com.symboltech.wangpos.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import com.symboltech.wangpos.http.HttpStringClient;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;

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
    private static  String host_config = "192.168.5.155:81";
    private static String billId;
    private static String last_billid;

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

    public static String getHost_config() {
        if (!StringUtil.isEmpty(host_config)) {
            return host_config;
        } else {
            host_config = SpSaveUtils.read(MyApplication.context, ConstantData.IP_HOST_CONFIG, "");
            return host_config;
        }
    }

    public static void setHost_config(String host_config) {
        if (!StringUtil.isEmpty(host_config.trim())) {
            MyApplication.host_config = host_config.trim();
            SpSaveUtils.write(MyApplication.context, ConstantData.IP_HOST_CONFIG, host_config.trim());
        }
    }

    public static String getLast_billid() {
        if (!StringUtil.isEmpty(last_billid)) {
            return last_billid;
        } else {
            last_billid = SpSaveUtils.read(MyApplication.context, ConstantData.RECEIPT_NUMBER_LAST, "");
            return last_billid;
        }
    }

    public static void setLast_billid(String last_billid) {
        if (!StringUtil.isEmpty(last_billid)) {
            MyApplication.last_billid = last_billid;
            SpSaveUtils.write(MyApplication.context, ConstantData.RECEIPT_NUMBER_LAST, last_billid);
        }
    }

    public static String getBillId() {
        if (!StringUtil.isEmpty(billId)) {
            return billId;
        } else {
            billId = SpSaveUtils.read(MyApplication.context, ConstantData.RECEIPT_NUMBER, "");
            return billId;
        }

    }

    public static void setBillId(String billId) {
        if (!StringUtil.isEmpty(billId)) {
            MyApplication.billId = billId;
            SpSaveUtils.write(MyApplication.context, ConstantData.RECEIPT_NUMBER, billId);
        }

    }
}
