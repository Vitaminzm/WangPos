package com.symboltech.wangpos.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class LoginSucessDialog extends Dialog {
	private Context context;
	private DialogFinishCallBack finishcallback;
	/** refresh UI By handler */
	public Handler handler = new Handler();

	public LoginSucessDialog(Context context, DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.finishcallback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_login_seccess);
		this.setCanceledOnTouchOutside(false);
		handler.sendEmptyMessageDelayed(3, 2000);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if(finishcallback != null)
			finishcallback.finish(0);
		handler.removeCallbacksAndMessages(null);
	}
}
