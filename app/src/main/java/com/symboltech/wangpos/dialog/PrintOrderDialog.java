package com.symboltech.wangpos.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.MainActivity;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

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
	private ImageView imageview_close;
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
	private HorizontalKeyBoard keyboard;

	public PrintOrderDialog(Context context, GeneralEditListener gel) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.gel = gel;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_print_order);
		this.setCanceledOnTouchOutside(true);
		initUI();
		initData();
	}

	private void initData() {
		keyboard = new HorizontalKeyBoard(context, this, edit_input_order_no, null);
		if (!StringUtil.isEmpty(AppConfigFile.getLast_billid())) {
			edit_input_order_no.setHint(AppConfigFile.getLast_billid());
		}
	}

	private void initUI() {
		edit_input_order_no = (EditText) findViewById(R.id.edit_input_order_no);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
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
		imageview_close.setOnClickListener(this);

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
				if(keyboard.isShowing()){
					keyboard.dismiss();
				}
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
						ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.waring_format_msg));
					}
				} else {
					if (!StringUtil.isEmpty(AppConfigFile.getLast_billid())) {
						gel.editinput(AppConfigFile.getLast_billid());
						this.dismiss();
					} else {
						ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.pleae_order_number));
					}
				}
				break;
			case R.id.text_print_order:
				text_title.setText(R.string.please_input_order_no);
				ll_function.setVisibility(View.GONE);
				ll_function_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_print_slip:
//				String bankId = SpSaveUtils.read(context, ConstantData.LAST_BANK_TRANS, "");
//				if (!bankId.equals("")){
					((MainActivity)context).print_last(null);
					dismiss();
//				}else{
//					ToastUtils.sendtoastbyhandler(handler,"没有银行交易记录");
//				}
				break;
			case R.id.imageview_close:
				dismiss();
				break;
		}
	}
}