package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.interfaces.DialogFinishCallBack;
import com.symboltech.wangpos.utils.Utils;

import java.lang.reflect.Field;


/**
 * 脱机选择框
 * @author so
 *
 */
public class PaySearchDialog extends BaseDialog implements OnClickListener {

	private Context context;
	private ImageView imageview_close;
	private TextView text_bank, text_thirdpay;

	private DialogFinishCallBack callback;
	public PaySearchDialog(Context context, DialogFinishCallBack callback) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.dialog_pay_search);
		initView();
		initEvent();
	}

	private void initView() {

		text_bank = (TextView) findViewById(R.id.text_bank);
		text_thirdpay = (TextView) findViewById(R.id.text_thirdpay);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);

	}

	
	public void initEvent(){
		text_bank.setOnClickListener(this);
		text_thirdpay.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		switch (v.getId()) {
		case R.id.text_bank:
		case R.id.imageview_close:
			// 取消
			if(callback!= null){
				callback.finish(0);
			}
			break;
		case R.id.text_thirdpay:
			// 确定
			if(callback!= null){
				callback.finish(1);
			}
			break;
		default:
			break;
		}
		this.dismiss();
	}
	

	
	public void setDropDownHeight(Spinner mSpinner, int pHeight){
		try {
			Field field=Spinner.class.getDeclaredField("mPopup");
			field.setAccessible(true);
			ListPopupWindow popUp=(ListPopupWindow)field.get(mSpinner);
			popUp.setHeight(pHeight);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
