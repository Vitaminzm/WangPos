package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.CancleAndConfirmback;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class YuxfReturnDialog extends Dialog implements View.OnClickListener {
	public Context context;
	private TextView text_return, text_cancle;
	private LinearLayout ll_function_print_order, ll_function;
	private ImageView imageview_close;
	private CancleAndConfirmback gel;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(context, msg);
					break;
				case 3:
					YuxfReturnDialog.this.dismiss();
					break;
				default:
					break;
			}
		};
	};

	public YuxfReturnDialog(Context context, CancleAndConfirmback gel) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.gel = gel;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_yuxfreturn);
		this.setCanceledOnTouchOutside(true);
		initUI();
		initData();
	}

	private void initData() {
	}

	private void initUI() {
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		text_return = (TextView) findViewById(R.id.text_return);
		text_cancle = (TextView) findViewById(R.id.text_cancle);
		ll_function_print_order = (LinearLayout) findViewById(R.id.ll_function_print_order);
		ll_function = (LinearLayout) findViewById(R.id.ll_function);
		text_return.setOnClickListener(this);
		text_cancle.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		handler.removeCallbacksAndMessages(null);
	}


	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.text_return:
				if(gel != null){
					gel.doCancle();
				}
				dismiss();
				break;
			case R.id.text_cancle:
				if(gel != null){
					gel.doConfirm("");
				}
				dismiss();
				break;
			case R.id.imageview_close:
				dismiss();
				break;
		}
	}
}
