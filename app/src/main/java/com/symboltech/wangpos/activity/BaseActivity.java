package com.symboltech.wangpos.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.symboltech.wangpos.app.MyApplication;
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
		MyApplication.addActivity(this);
		initData();
	}

	/**
	 * 开启dialog
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	protected void closewaitdialog() {
		if (waitdialog != null && waitdialog.isShowing()) {
			try {
				waitdialog.dismiss();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 关闭dialog
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	protected void startwaitdialog() {
		if (waitdialog != null) {
			try {
				waitdialog.show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: 初始化数据
	 */
	protected abstract void initData();

	/**
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: 初始化UI
	 */
	protected abstract void initView();

	/**
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO 用于资源回收
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
		MyApplication.delActivity(this);
	}

}
