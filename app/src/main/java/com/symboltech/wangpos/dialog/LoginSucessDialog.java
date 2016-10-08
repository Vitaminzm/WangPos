package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
public class LoginSucessDialog extends Dialog {
	private Context context;
	private DialogFinishCallBack finishcallback;
	/** refresh UI By handler */
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;
			case 3:
				LoginSucessDialog.this.dismiss();
				break;
			default:
				break;
			}
		};
	};

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
