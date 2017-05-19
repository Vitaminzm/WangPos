package com.symboltech.wangpos.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.PaymentActivity;
import com.symboltech.wangpos.activity.ReturnGoodsByNormalActivity;
import com.symboltech.wangpos.activity.ReturnGoodsByOrderActivity;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.result.BillResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.util.HashMap;
import java.util.Map;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class OfflineReturnDialog extends BaseDialog implements View.OnClickListener {
	public Context context;
	private TextView text_sale, text_return;
	private ImageView imageview_close;

	private HorizontalKeyBoard keyboard;

	public OfflineReturnDialog(Context context) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_offline_return);
		this.setCanceledOnTouchOutside(true);
		initUI();
	}

	private void initUI() {
		text_sale = (TextView) findViewById(R.id.text_sale);
		text_return = (TextView) findViewById(R.id.text_return);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		imageview_close.setOnClickListener(this);
		text_sale.setOnClickListener(this);
		text_return.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.text_sale:
				AppConfigFile.setOffLineMode(true);
				Intent intentSale = new Intent(context, PaymentActivity.class);
				context.startActivity(intentSale);
				dismiss();
				break;
			case R.id.text_return:
				AppConfigFile.setOffLineMode(true);
				Intent intentJob = new Intent(context, ReturnGoodsByNormalActivity.class);
				context.startActivity(intentJob);
				dismiss();
				break;
			case R.id.imageview_close:
				dismiss();
				break;
		}
	}
}
