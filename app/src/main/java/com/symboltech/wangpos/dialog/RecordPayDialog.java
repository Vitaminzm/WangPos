package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;

import java.util.List;

/**
 *
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class RecordPayDialog extends BaseDialog implements View.OnClickListener {
	private Context context;
	private LinearLayout ll_weichat_record, ll_alipay_record, ll_bankcode_record, ll_yipay_record,
						ll_bank_record, ll_store_record, ll_yxlm_record;
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
		ll_yipay_record = (LinearLayout) findViewById(R.id.ll_yipay_record);
		ll_yipay_record.setOnClickListener(this);
		ll_bank_record = (LinearLayout) findViewById(R.id.ll_bank_record);
		ll_bank_record.setOnClickListener(this);
		ll_store_record = (LinearLayout) findViewById(R.id.ll_store_record);
		ll_store_record.setOnClickListener(this);
		ll_yxlm_record = (LinearLayout) findViewById(R.id.ll_yxlm_record);
		ll_yxlm_record.setOnClickListener(this);
		if(!isContain(ConstantData.WECHAT_ID)){
			ll_weichat_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_weichat_record.setEnabled(false);
		}
		if(!isContain(ConstantData.ALPAY_ID)){
			ll_alipay_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_alipay_record.setEnabled(false);
		}
		if(!isContain(ConstantData.BANKCODE_ID)){
			ll_bankcode_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_bankcode_record.setEnabled(false);
		}
		if(!isContain(ConstantData.YIPAY_ID)){
			ll_yipay_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_yipay_record.setEnabled(false);
		}
		if(!isContain(getPayIdByType(PaymentTypeEnum.BANK.getStyletype()))){
			ll_bank_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_bank_record.setEnabled(false);
		}
		if(!isContain(getPayIdByType(PaymentTypeEnum.STORE.getStyletype()))){
			ll_store_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_store_record.setEnabled(false);
		}
		if(!isContain(ConstantData.YXLM_ID)){
			ll_yxlm_record.setBackgroundResource(R.drawable.btn_gray_bg);
			ll_yxlm_record.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.ll_weichat_record:
				payId = ConstantData.WECHAT_ID;
				break;
			case R.id.ll_alipay_record:
				payId = ConstantData.ALPAY_ID;
				break;
			case R.id.ll_bankcode_record:
				payId = ConstantData.BANKCODE_ID;
				break;
			case R.id.ll_yipay_record:
				payId = ConstantData.YIPAY_ID;
				break;
			case R.id.ll_bank_record:
				payId = getPayIdByType(PaymentTypeEnum.BANK.getStyletype());
				break;
			case R.id.ll_store_record:
				payId = getPayIdByType(PaymentTypeEnum.STORE.getStyletype());
				break;
			case R.id.ll_yxlm_record:
				payId = ConstantData.YXLM_ID;
				break;
		}
		if(isContain(payId)){
			finishcallback.editinput(payId);
		}else{
			finishcallback.editinput(null);
		}
		dismiss();
	}

	private boolean isContain(String id){
		boolean ret = false;
		List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(context, ConstantData.PAYMENTSLIST);
		if(StringUtil.isEmpty(id) || paymentslist == null){
			return ret;
		}
		for (int i = 0; i < paymentslist.size(); i++) {
			if (paymentslist.get(i).getId().equals(id)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	private String getPayIdByType(String type){
		List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(context, ConstantData.PAYMENTSLIST);
		if(paymentslist == null)
			return null;
		for (int i = 0; i < paymentslist.size(); i++) {
			if (paymentslist.get(i).getType().equals(type)) {
				return paymentslist.get(i).getId();
			}
		}
		return null;
	}
}
