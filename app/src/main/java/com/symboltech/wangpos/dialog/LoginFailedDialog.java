package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.utils.ToastUtils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class LoginFailedDialog extends Dialog {
	private TextView text_confirm;

	/**
	 *
	 * @param context
	 * @param mtitle
	 *            标题
	 * @param minfo
	 *            内容
	 */
	public LoginFailedDialog(Context context, String mtitle, String minfo, DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_universal_hint);
		this.setCanceledOnTouchOutside(true);
		initUI();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	private void initUI() {
		text_confirm = (TextView) findViewById(R.id.tv_universalhint_title);
	}

}
