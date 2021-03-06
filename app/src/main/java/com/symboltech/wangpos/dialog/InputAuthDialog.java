package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.MainActivity;
import com.symboltech.wangpos.interfaces.GeneralEditListener;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class InputAuthDialog extends BaseDialog implements View.OnClickListener {
	public Context context;
	private EditText edit_input_order_no;
	private TextView text_title, text_print_order, text_print_slip, text_cancle, text_confirm;
	private LinearLayout ll_function_print_order, ll_function;
	private ImageView imageview_close;
	private GeneralEditListener gel;
	private String title;
	private String hit = null;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(context, msg);
					break;
				case 3:
					InputAuthDialog.this.dismiss();
					break;
				default:
					break;
			}
		};
	};
	private HorizontalKeyBoard keyboard;

	boolean flag = true;
	public InputAuthDialog(Context context, String title, GeneralEditListener gel) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.title = title;
		this.gel = gel;
	}

	public InputAuthDialog(Context context, String title, String hit, GeneralEditListener gel) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
		this.title = title;
		this.gel = gel;
		this.hit = hit;
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
		text_title.setText(title);
		edit_input_order_no.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		if(!StringUtil.isEmpty(hit)){
			edit_input_order_no.setHint(hit);
		}
		if(!StringUtil.isEmpty(hit)){
			edit_input_order_no.setHint(hit);
		}
		keyboard = new HorizontalKeyBoard(context, this, edit_input_order_no, null, new KeyBoardListener() {
			@Override
			public void onComfirm() {

			}

			@Override
			public void onCancel() {

			}

			@Override
			public void onValue(String value) {
				if (!StringUtil.isEmpty(edit_input_order_no.getText().toString().trim())) {
					gel.editinput(edit_input_order_no.getText().toString().trim());
					dismiss();
				} else {
					ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.pleae_verify_auth));
				}
			}
		});
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				keyboard.show();
			}
		}, 500);
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
		ll_function.setVisibility(View.GONE);
		ll_function_print_order.setVisibility(View.VISIBLE);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if( keyboard!= null && keyboard.isShowing()){
			keyboard.dismiss();
		}
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
				dismiss();
				break;
			case R.id.text_confirm:
				if (!StringUtil.isEmpty(edit_input_order_no.getText().toString().trim())) {
					gel.editinput(edit_input_order_no.getText().toString().trim());
					this.dismiss();
				} else {
					ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.pleae_verify_auth));
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
					((MainActivity)context).print_last(null,null);
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
