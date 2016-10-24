package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.service.RunTimeService;

/**
 * 交班选择框
 * @author so
 *
 */
public class OfflineUploadDialog extends Dialog {

	private Context context;

	private Handler mHandler = new Handler();
	private boolean isForce = false;
	
	public OfflineUploadDialog(Context context) {
		super(context, R.style.alert_dialog);
		this.context = context;
	}

	public OfflineUploadDialog(Context context,boolean isForce) {
		super(context, R.style.alert_dialog);
		this.context = context;
		this.isForce = isForce;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(isForce){
			setCanceledOnTouchOutside(false);
		}else{
			setCanceledOnTouchOutside(true);
		}
		setContentView(R.layout.dialog_offline_upload);
		initView();
	}

	private void initView() {
		if(!isForce){
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					dismiss();
				}
			}, 2000);
			Intent serviceintent = new Intent(context, RunTimeService.class);
			serviceintent.putExtra(ConstantData.UPLOAD_OFFLINE_DATA, true);
			context.startService(serviceintent);
		}
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		mHandler.removeCallbacksAndMessages(null);
		super.dismiss();
	}
}
