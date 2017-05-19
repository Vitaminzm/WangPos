package com.symboltechshop.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.interfaces.DialogFinishCallBack;
import com.symboltechshop.wangpos.utils.ToastUtils;
import com.symboltechshop.wangpos.utils.Utils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class BankreturnDialog extends BaseDialog implements View.OnClickListener {
	public Context context;
	private LinearLayout ll_function_print_order, ll_function;
	private TextView  text_cardno, text_money, text_cancle, text_confirm, text_title, text_now_cancle, text_tom_return;
	private ImageView imageview_close;
	private DialogFinishCallBack callback;
	private String cardNo,  money, title;
	private String type;
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
			text_cardno.setVisibility(View.GONE);
		}else if(context.getString(R.string.alipay_return).equals(title)){
			name = "支付宝：";
			text_cardno.setVisibility(View.GONE);
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
		text_now_cancle = (TextView) findViewById(R.id.text_now_cancle);
		text_tom_return = (TextView) findViewById(R.id.text_tom_return);
		ll_function_print_order = (LinearLayout) findViewById(R.id.ll_function_print_order);
		ll_function = (LinearLayout) findViewById(R.id.ll_function);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
		text_now_cancle.setOnClickListener(this);
		text_tom_return.setOnClickListener(this);

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
					if("1".equals(type)){
						callback.finish(1);
					}else if("2".equals(type)){
						callback.finish(2);
					}
				}
				dismiss();
				break;
			case R.id.text_tom_return:
				ll_function_print_order.setVisibility(View.VISIBLE);
				ll_function.setVisibility(View.GONE);
				type = "1";
				break;
			case R.id.text_now_cancle:
				ll_function_print_order.setVisibility(View.VISIBLE);
				ll_function.setVisibility(View.GONE);
				type = "2";
				break;
		}
	}
}
