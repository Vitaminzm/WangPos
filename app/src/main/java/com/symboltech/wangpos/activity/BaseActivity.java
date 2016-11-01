package com.symboltech.wangpos.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.utils.HttpWaitDialogUtils;

import java.lang.reflect.Method;

/**
 * 
 * simple introduction
 *
 * <p>
 * detailed comment Description BaseActivity 基础activity
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月6日
 * @see
 * @since 1.0
 */
public abstract class BaseActivity extends Activity {

	public Context mContext;
	public HttpWaitDialogUtils waitdialog;
	private View decorView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mContext = getApplicationContext();
		waitdialog = new HttpWaitDialogUtils(this);
		initView();
		initData();
		//hideBottomUIMenu();
	}


//	@Override
//	public void onWindowFocusChanged(boolean hasFocus) {
//		super.onWindowFocusChanged(hasFocus);
//		if(hasFocus){
//			decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//					| View.SYSTEM_UI_FLAG_IMMERSIVE);
//		}
//	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
	Window window;
	protected void hideBottomUIMenu() {
//		window = getWindow();
//		WindowManager.LayoutParams params = window.getAttributes();
//		params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
//		window.setAttributes(params);

		decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_IMMERSIVE);

//	    int uiOptions =View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE;
//		decorView.setSystemUiVisibility(uiOptions);
		//隐藏虚拟按键，并且全屏
//		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
//			View v = this.getWindow().getDecorView();
//			v.setSystemUiVisibility(View.GONE);
//		} else if (Build.VERSION.SDK_INT >= 19) {
//			//for new api versions.
//			View decorView = getWindow().getDecorView();
//			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY ;
//			decorView.setSystemUiVisibility(uiOptions);
//		}
	}
	/**
	 * 开启dialog
	 */
	protected void closewaitdialog() {
		if (waitdialog != null && waitdialog.isShowing()) {
			try {
				waitdialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭dialog
	 *
	 */
	protected void startwaitdialog() {
		if (waitdialog != null && !waitdialog.isShowing()) {
			try {
				waitdialog.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * 初始化数据
	 */
	protected abstract void initData();

	/**
	 * 
	 *  初始化UI
	 */
	protected abstract void initView();

	/**
	 * 
	 *  用于资源回收
	 */
	protected abstract void recycleMemery();

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycleMemery();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
//			case KeyEvent.KEYCODE_HOME:
//				return true;
			case KeyEvent.KEYCODE_BACK:
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onAttachedToWindow(){
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		super.onAttachedToWindow();
	}
}
