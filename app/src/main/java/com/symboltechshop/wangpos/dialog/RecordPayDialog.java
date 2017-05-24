package com.symboltechshop.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.interfaces.GeneralEditListener;
import com.symboltechshop.wangpos.utils.PaymentTypeEnum;
import com.symboltechshop.wangpos.utils.StringUtil;

/**
 *
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class RecordPayDialog extends BaseDialog implements View.OnClickListener {
	private Context context;
	private LinearLayout ll_weichat_record, ll_alipay_record, ll_bankcode_record, ll_hand_record;
	private GeneralEditListener finishcallback;

	private String payId;
	public RecordPayDialog(Context context,GeneralEditListener callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.finishcallback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_record_pay);
		this.setCanceledOnTouchOutside(true);
		initUI();
		setdata();
	}

	private void setdata() {
	}

	private void initUI() {
		ll_weichat_record = (LinearLayout) findViewById(R.id.ll_weichat_record);
		ll_weichat_record.setOnClickListener(this);
		ll_alipay_record = (LinearLayout) findViewById(R.id.ll_alipay_record);
		ll_alipay_record.setOnClickListener(this);
		ll_bankcode_record = (LinearLayout) findViewById(R.id.ll_bankcode_record);
		ll_bankcode_record.setOnClickListener(this);
		ll_hand_record = (LinearLayout) findViewById(R.id.ll_hand_record);
		ll_hand_record.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.ll_weichat_record:
				payId = PaymentTypeEnum.WECHATRECORDED.getStyletype();
				break;
			case R.id.ll_alipay_record:
				payId = PaymentTypeEnum.ALIPAYRECORDED.getStyletype();
				break;
			case R.id.ll_bankcode_record:
				payId = PaymentTypeEnum.BANK_CODE.getStyletype();
				break;
			case R.id.ll_hand_record:
				payId = PaymentTypeEnum.HANDRECORDED.getStyletype();
				break;
		}
		if(!StringUtil.isEmpty(payId)){
			finishcallback.editinput(payId);
		}else{
			finishcallback.editinput(null);
		}
		dismiss();
	}
}
