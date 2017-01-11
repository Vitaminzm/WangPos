package com.symboltech.wangpos.app;

import android.app.Activity;

import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;

import java.util.LinkedList;
import java.util.List;

import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2016年1月19日 下午3:09:59 
* @version 1.0 
* 用于配置APP运行相关参数
*/
public class AppConfigFile {

	/**监测网络状态发送间隔*/
	public static final long NETWORK_STATUS_INTERVAL = 2 * 60 * 1000;
	
	/**用户登录信息保存时间*/
	public static final long USER_LOGIN_SAVE_TIME = 1000 * 60 * 60 * 24 * 15;
	
	/**操作日志上传时间间隔*/
	public static final long OPT_LOG_INTERVAL = 1000 * 10;
	
	/**离线数据保存时间*/
	public static final int OFFLINE_DATA_TIME = -7;
	
	/**离线数据单次上传条数*/
	public static final int OFFLINE_DATA_COUNT = 15;
	
	/**单个操作日志文件保存条数*/
	public static final int OPERATE_LOG_SIZE = 50;

	/** activity manange list */
	public static List<Activity> activityList = new LinkedList<>();
	//private static  String host_config = "192.168.28.234:82";//239
	 private static  String host_config = "210.14.129.42:82";  //41
	private static String billId;
	private static String last_billid;

    /*----------------------------------------------------------------*/
	/**
	 * activity
	 *
	 */
	public static void addActivity(Activity activity) {
		if (activityList != null)
			activityList.add(activity);
	}

	public static void delActivity(Activity activity) {
		if (activityList != null)
			activityList.remove(activity);
	}

	public static void exit() {
		if (activityList != null){
			for (Activity activity : activityList) {
				if (activity != null) {
					activity.finish();
					activity = null;
				}
			}
		}
		if(MyApplication.posType.equals("WPOS")){
			try {
				WeiposImpl.as().destroy();
			} catch (Exception e) {
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
			AppConfigFile.host_config = host_config.trim();
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
			AppConfigFile.last_billid = last_billid;
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
			AppConfigFile.billId = billId;
			SpSaveUtils.write(MyApplication.context, ConstantData.RECEIPT_NUMBER, billId);
		}
	}

	public static int getUploadStatus() {
		return SpSaveUtils.readInt(MyApplication.context, ConstantData.UP_STATUS, ConstantData.UPLOAD_SUCCESS);
	}

	public static void setUploadStatus(int uploadStatus) {
		SpSaveUtils.writeInt(MyApplication.context, ConstantData.UP_STATUS, uploadStatus);
	}

	public static boolean isOffLineMode() {
		return SpSaveUtils.readboolean(MyApplication.context, ConstantData.IS_OFFLINE, false);
	}

	public static void setOffLineMode(boolean isOffLineMode) {
		SpSaveUtils.writeboolean(MyApplication.context, ConstantData.IS_OFFLINE, isOffLineMode);
	}

	public static boolean isNetConnect() {
		return SpSaveUtils.readboolean(MyApplication.context, ConstantData.IS_NETCONNECT, true);
	}

	public static void setNetConnect(boolean isNetConnect) {
		SpSaveUtils.writeboolean(MyApplication.context, ConstantData.IS_NETCONNECT, isNetConnect);
	}

}
