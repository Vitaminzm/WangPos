package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class PrintOrderDialog extends Dialog implements View.OnClickListener {
	public Context context;
	private EditText edit_input_order_no;
	private TextView text_title, text_print_order, text_print_slip, text_cancle, text_confirm;
	private LinearLayout ll_function_print_order, ll_function;
	private GeneralEditListener gel;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(context, msg);
					break;
				case 3:
					PrintOrderDialog.this.dismiss();
					break;
				default:
					break;
			}
		};
	};
	public PrintOrderDialog(Context context, GeneralEditListener gel) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.gel = gel;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_login_failed);
		this.setCanceledOnTouchOutside(true);
		initUI();
		initData();
	}

	private void initData() {
		if (!StringUtil.isEmpty(MyApplication.getLast_billid())) {
			edit_input_order_no.setHint(MyApplication.getLast_billid());
		}
	}

	private void initUI() {
		edit_input_order_no = (EditText) findViewById(R.id.edit_input_order_no);
		text_title = (TextView) findViewById(R.id.text_title);
		text_print_order = (TextView) findViewById(R.id.text_print_order);
		text_print_slip = (TextView) findViewById(R.id.text_print_slip);
		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		ll_function_print_order = (LinearLayout) findViewById(R.id.ll_function_print_order);
		ll_function = (LinearLayout) findViewById(R.id.ll_function);
		text_print_order.setOnClickListener(this);
		text_print_slip.setOnClickListener(this);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);

	}

	@Override
	public void dismiss() {
		super.dismiss();
		handler.removeCallbacksAndMessages(null);
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.text_cancle:
				text_title.setText(R.string.print_order);
				ll_function.setVisibility(View.VISIBLE);
				ll_function_print_order.setVisibility(View.GONE);
				break;
			case R.id.text_confirm:
				if (!StringUtil.isEmpty(edit_input_order_no.getText().toString().trim())) {
					boolean result=edit_input_order_no.getText().toString().trim().matches("[0-9]{1,10}");
					if(result){
						gel.editinput(edit_input_order_no.getText().toString().trim());
						this.dismiss();
					}else{
						Toast.makeText(context, R.string.waring_format_msg, Toast.LENGTH_SHORT).show();
					}
				} else {
					if (!StringUtil.isEmpty(MyApplication.getLast_billid())) {
						gel.editinput(MyApplication.getLast_billid());
						this.dismiss();
					} else {
						Toast.makeText(context, R.string.pleae_order_number, Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case R.id.text_print_order:
				text_title.setText(R.string.please_input_order_no);
				ll_function.setVisibility(View.GONE);
				ll_function_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_print_slip:
				break;
		}
	}
}
