package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;

/**
 * Description
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class AddScoreGoodDialog extends Dialog implements View.OnClickListener {
	private Context context;
	private TextView text_confirm;
	private DialogFinishCallBack finishcallback;

	/**
	 *
	 * @param context
	 */
	public AddScoreGoodDialog(Context context, DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.finishcallback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_universal_hint);
		this.setCanceledOnTouchOutside(true);
		initUI();
		setdata();
	}

	@Override
	public void dismiss() {
		super.dismiss();
		finishcallback.finish();
	}
	private void setdata() {

	}

	private void initUI() {
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		text_confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

	}
}
