package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.Utils;

/**
 * 交班选择框
 * @author so
 *
 */
public class AppAboutDialog extends BaseDialog {

	private Context context;

	private TextView tv_update_time, app_version_code;

	public AppAboutDialog(Context context) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.dialog_app_about);
		initView();
	}

	private void initView() {
		tv_update_time = (TextView) findViewById(R.id.tv_updateTime);
		app_version_code = (TextView) findViewById(R.id.tv_version);
		if (!StringUtil.isEmpty(Utils.getMetaValue(MyApplication.context, ConstantData.POS_UPDATE_TIME))) {
			tv_update_time.setText(Utils.getMetaValue(MyApplication.context, ConstantData.POS_UPDATE_TIME));
		}
		app_version_code.setText("V" + Utils.getAppVersionName(MyApplication.context));
	}

}
