package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.utils.Utils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class CouponWaringDialog extends BaseDialog implements View.OnClickListener {
	private Context context;
	private String couponMoney, money;
	private ImageView imageview_close;
	private TextView text_cancle;
	private TextView text_confirm;
	private TextView text_msg;
	private DialogFinishCallBack finishcallback;

	public CouponWaringDialog(Context context, String couponMoney, String money,  DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.couponMoney = couponMoney;
		this.money = money;
		this.finishcallback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_overrage_waring);
		this.setCanceledOnTouchOutside(false);
		initUI();
		setdata();
	}

	@Override
	public void dismiss() {
		super.dismiss();
		finishcallback.finish(0);
	}
	private void setdata() {
		text_msg.setText(context.getString(R.string.waring_coupons_over_msg1)+couponMoney+context.getString(R.string.yuan)+"\n"
				+context.getString(R.string.waring_coupons_over_msg2)+money+context.getString(R.string.yuan));
	}

	private void initUI() {
		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		text_msg = (TextView) findViewById(R.id.text_msg);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		imageview_close.setOnClickListener(this);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		if(id == R.id.text_cancle || id == R.id.imageview_close){
			dismiss();
		}else if(id == R.id.text_confirm){
			if(finishcallback != null){
				finishcallback.finish(0);
			}
		}
	}
}
