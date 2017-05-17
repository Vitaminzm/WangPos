package com.symboltechshop.wangpos.dialog;


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

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.interfaces.CancleAndConfirmback;
import com.symboltechshop.wangpos.utils.ToastUtils;
import com.symboltechshop.wangpos.utils.Utils;
import com.symboltechshop.wangpos.view.HorizontalKeyBoard;
import com.symboltechshop.wangpos.utils.StringUtil;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class VerifyMemberDialog extends Dialog implements View.OnClickListener {
	public Context context;
	private EditText edit_input_order_no;
	private TextView text_title, text_print_order, text_print_slip, text_cancle, text_confirm;
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
					VerifyMemberDialog.this.dismiss();
					break;
				default:
					break;
			}
		};
	};
	private HorizontalKeyBoard keyboard;

	public VerifyMemberDialog(Context context, CancleAndConfirmback gel) {
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
		text_title.setText("验证会员");
		keyboard = new HorizontalKeyBoard(context, this, edit_input_order_no, null);
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
		text_cancle.setText("扫描验证");
		text_confirm.setText("手机号验证");
		imageview_close.setOnClickListener(this);
		ll_function.setVisibility(View.GONE);
		ll_function_print_order.setVisibility(View.VISIBLE);
		edit_input_order_no.setHint("请输入手机号");
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
				if(keyboard.isShowing()){
					keyboard.dismiss();
				}
				if(gel != null){
					gel.doCancle();
				}
				dismiss();
				break;
			case R.id.text_confirm:
				if (!StringUtil.isEmpty(edit_input_order_no.getText().toString().trim())) {
					if(Utils.isMobileNO(edit_input_order_no.getText().toString().trim())){
						gel.doConfirm(edit_input_order_no.getText().toString().trim());
						this.dismiss();
					}else{
						ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.please_input_right_phoneNo));
					}
				} else {
					ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.please_input_phoneNo));
				}
				break;
			case R.id.text_print_order:
				text_title.setText(R.string.please_input_order_no);
				ll_function.setVisibility(View.GONE);
				ll_function_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_print_slip:
				break;
			case R.id.imageview_close:
				dismiss();
				break;
		}
	}
}
