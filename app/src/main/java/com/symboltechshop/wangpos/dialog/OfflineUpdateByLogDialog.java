package com.symboltechshop.wangpos.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.activity.LoginActivity;
import com.symboltechshop.wangpos.activity.MainActivity;
import com.symboltechshop.wangpos.app.ConstantData;
import com.symboltechshop.wangpos.log.LogUtil;
import com.symboltechshop.wangpos.service.RunTimeService;


public class OfflineUpdateByLogDialog extends BaseDialog implements OnClickListener {

	private TextView bt_yes, bt_no, tips;
	private Context context;
	
	public OfflineUpdateByLogDialog(Context context) {
		super(context, R.style.dialog_login_bg);
		setCanceledOnTouchOutside(false);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_offline_upload_log);
		initView();
	}

	private void initView() {
		bt_yes = (TextView) findViewById(R.id.dialog_log_load_style_yes);
		bt_no = (TextView) findViewById(R.id.dialog_log_load_style_no);
		tips = (TextView) findViewById(R.id.dialog_log_load_style_tips);
		bt_yes.setOnClickListener(this);
		bt_no.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.dialog_log_load_style_yes:
			try {
				((MainActivity)context).ChangeUI(1);
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.v("lgs", "跳转手动上传失败");
			}
			break;
		case R.id.dialog_log_load_style_no:
			Intent serviceintent = new Intent(context, RunTimeService.class);
			serviceintent.putExtra(ConstantData.UPLOAD_OFFLINE_DATA_BYLOG, true);
			serviceintent.putExtra(ConstantData.UPDATE_STATUS, true);
			serviceintent.putExtra(ConstantData.CASHIER_ID, ConstantData.POS_STATUS_LOGOUT);
			context.startService(serviceintent);
			Intent intent = new Intent(context, LoginActivity.class);
			intent.putExtra(ConstantData.LOGIN_WITH_CHOOSE_KEY, ConstantData.LOGIN_WITH_CASHIER);
			context.startActivity(intent);
			((MainActivity)context).finish();
			break;
		}
		dismiss();
	}




}