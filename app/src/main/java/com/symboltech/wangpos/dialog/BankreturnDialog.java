package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class BankreturnDialog extends Dialog implements View.OnClickListener {
	public Context context;
	private TextView  text_cardno, text_money, text_cancle, text_confirm, text_title;
	private ImageView imageview_close;
	private DialogFinishCallBack callback;
	private String cardNo,  money, title;
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(context, msg);
					break;
				case 3:
					BankreturnDialog.this.dismiss();
					break;
				default:
					break;
			}
		};
	};

	public BankreturnDialog(Context context,String title, String cardNo, String money, DialogFinishCallBack gel) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.callback = gel;
		this.cardNo = cardNo;
		this.money = money;
		this.title = title;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_bank_return);
		this.setCanceledOnTouchOutside(true);
		initUI();
		initData();
	}

	private void initData() {
		String name="";
		if(context.getString(R.string.wechat_return).equals(title)){
			name = "微信：";
		}else if(context.getString(R.string.alipay_return).equals(title)){
			name = "支付宝：";
		}else if(context.getString(R.string.qmh_return).equals(title)){
			name = "全名惠：";
		}else{
			name = "卡号：";
		}
		text_cardno.setText(name + cardNo);
		text_money.setText("金额："+money+"元");
		text_title.setText(title);
	}

	private void initUI() {
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		text_money = (TextView) findViewById(R.id.text_money);
		text_cardno = (TextView) findViewById(R.id.text_cardno);
		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		text_title = (TextView) findViewById(R.id.text_title);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
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
			case R.id.text_cancle:
			case R.id.imageview_close:
				dismiss();
				break;
			case R.id.text_confirm:
				if(callback != null){
					callback.finish(0);
				}
				dismiss();
				break;
		}
	}
}
