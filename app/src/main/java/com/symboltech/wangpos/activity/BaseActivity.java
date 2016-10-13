package com.symboltech.wangpos.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.symboltech.wangpos.utils.HttpWaitDialogUtils;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mContext = getApplicationContext();
		waitdialog = new HttpWaitDialogUtils(this);
		initView();
		initData();
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
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_MENU) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onAttachedToWindow(){
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		super.onAttachedToWindow();
	}
}
