package com.symboltech.wangpos.exception;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.config.InitializeConfig;
import com.symboltech.wangpos.http.HttpServiceStringClient;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.log.OperateLog;
import com.symboltech.wangpos.utils.OptLogEnum;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 捕获程序异常
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 2015年12月15日
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	private UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler INSTANCE = new CrashHandler();
	private Context mContext;
	private Map<String, String> infos = new HashMap<String, String>();

	private CrashHandler() {
	}

	// 获取实例
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	public void exit(){
		MyApplication.stopTask();
		OperateLog.getInstance().stopUpload();
		InitializeConfig.clearCash(mContext);
		AppConfigFile.exit();
		HttpServiceStringClient.getinstance().cancleRequest();
		System.exit(1);
	}
	public void uncaughtException(Thread t, Throwable ex) {
		if (!handlerException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(t, ex);
			exit();
		} else {
			// 退出应用
//			android.os.Process.killProcess(android.os.Process.myPid());
//			System.exit(1);
			exit();
		}

	}

	private boolean handlerException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		StringBuffer sb = new StringBuffer();
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		LogUtil.i("lgs", sb.toString());
		OperateLog.getInstance().saveLog2File(OptLogEnum.ERROR_OPT.getOptLogCode(), sb.toString());
		return true;
	}

	// 收集设备信息
	private void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			// Log.e(TAG, "an error occured when collection package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
			} catch (Exception e) {
				Log.d(TAG, "an error occured when collect package info ", e);
			}
		}
	}

}
