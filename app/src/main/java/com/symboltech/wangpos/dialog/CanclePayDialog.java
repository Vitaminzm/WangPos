package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.adapter.CanclePayAdapter;
import com.symboltech.wangpos.interfaces.CancleCallback;
import com.symboltech.wangpos.msg.entity.PayMentsCancleInfo;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.ToastUtils;

import java.util.List;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class CanclePayDialog extends Dialog implements View.OnClickListener {
	private Context context;
	private ImageView imageview_close;
	private TextView text_cancle_pay;
	private TextView text_submit_order;
	private TextView text_money;
	private TextView text_info;
	private ListView listview_canclepay;
	private CancleCallback callback;
	private List<PayMentsCancleInfo> payments;
	private CanclePayAdapter canclePayAdapter;
	/** refresh UI By handler */
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;
			case 3:
				CanclePayDialog.this.dismiss();
				break;
			default:
				break;
			}
		};
	};

	public CanclePayDialog(Context context,  List<PayMentsCancleInfo> payments, CancleCallback callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.payments = payments;
		this.callback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_cancle_pay);
		this.setCanceledOnTouchOutside(false);
		initUI();
		setdata();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}
	private void setdata() {
		double totalMoney = 0;
		double cash = 0;
		canclePayAdapter = new CanclePayAdapter(payments, context);
		listview_canclepay.setAdapter(canclePayAdapter);
		if(payments!=null){
			for(int i=0;i<payments.size();i++){
				if(!payments.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())){
					totalMoney = ArithDouble.add(totalMoney, ArithDouble.parseDouble(payments.get(i).getMoney()));
				}else{
					if(payments.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype())
							|| payments.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype())
							||payments.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype())
							||payments.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype())){
						cash = ArithDouble.sub(cash, ArithDouble.parseDouble(payments.get(i).getMoney()));
					}
					totalMoney = ArithDouble.sub(totalMoney, ArithDouble.parseDouble(payments.get(i).getMoney()));
				}
			}
		}
		text_money.setText(totalMoney+"");
		if(cash > 0)
			text_info.setText("(现金类 "+cash+")元");
	}

	private void initUI() {
		text_cancle_pay = (TextView) findViewById(R.id.text_cancle_pay);
		text_submit_order = (TextView) findViewById(R.id.text_submit_order);
		text_money = (TextView) findViewById(R.id.text_money);
		text_info = (TextView) findViewById(R.id.text_info);
		imageview_close =  (ImageView) findViewById(R.id.imageview_close);
		listview_canclepay =  (ListView) findViewById(R.id.listview_canclepay);
		imageview_close.setOnClickListener(this);
		text_cancle_pay.setOnClickListener(this);
		text_submit_order.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.imageview_close:
			case R.id.text_cancle_pay:
				if(canclePayAdapter.getIsCancleCount() != 0){
					ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.waring_msg_pay_cancle_failed));
					return;
				}
				if(callback != null){
					callback.doResult();
				}
				this.dismiss();
				break;
			case R.id.text_submit_order:
				if(canclePayAdapter.getIsCancleCount() != 0){
					ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.waring_msg_pay_cancle_failed));
					return;
				}
				for(int i=0;i<payments.size();i++){
					if(payments.get(i).getType().equals(PaymentTypeEnum.BANK.getStyletype()) || payments.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype()) ||
							payments.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype()) ||payments.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype()) ||
							payments.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype()) ||payments.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())){
						payments.get(i).setIsCancle(true);
					}
				}
				if(callback != null){
					callback.doResult();
				}
				this.dismiss();
				break;
		}
	}
}
