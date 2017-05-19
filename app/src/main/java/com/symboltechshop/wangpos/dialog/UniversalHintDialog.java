package com.symboltechshop.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.utils.ToastUtils;
import com.symboltechshop.wangpos.interfaces.DialogFinishCallBack;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class UniversalHintDialog extends BaseDialog {
	private Context context;
	private String title, info;
	private TextView tv_universalhint_title;
	private TextView tv_universalhint_msg;
	private DialogFinishCallBack finishcallback;
	/** refresh UI By handler */
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;
			case 3:
				UniversalHintDialog.this.dismiss();
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 
	 * @param context
	 * @param mtitle
	 *            标题
	 * @param minfo
	 *            内容
	 */
	public UniversalHintDialog(Context context, String mtitle, String minfo, DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.title = mtitle;
		this.info = minfo;
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
		finishcallback.finish(0);
	}
	private void setdata() {
		handler.sendEmptyMessageDelayed(3, 1000 * 3);
//		if (!StringUtil.isEmpty(title)) {
//			tv_universalhint_title.setVisibility(View.VISIBLE);
//			tv_universalhint_title.setText(title);
//		} else {
//			tv_universalhint_title.setVisibility(View.GONE);
//		}
//
//		if (!StringUtil.isEmpty(info)) {
//			tv_universalhint_msg.setVisibility(View.VISIBLE);
//			tv_universalhint_msg.setText(info);
//		} else {
//			tv_universalhint_msg.setVisibility(View.GONE);
//		}
//		if (isfinish) {
//
//		}
	}

	private void initUI() {
		tv_universalhint_title = (TextView) findViewById(R.id.tv_universalhint_title);
		tv_universalhint_msg = (TextView) findViewById(R.id.tv_universalhint_msg);
	}

}
