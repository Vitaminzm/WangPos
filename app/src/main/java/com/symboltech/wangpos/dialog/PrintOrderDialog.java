package com.symboltech.wangpos.dialog;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.MainActivity;
import com.symboltech.wangpos.app.AppConfigFile;
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
public class PrintOrderDialog extends BaseDialog implements View.OnClickListener {
	public Context context;
	private EditText edit_input_order_no, edit_third_input_order_no;
	private TextView text_title, text_print_order, text_print_slip, text_cancle, text_confirm, text_bank, text_yxlm, text_wf, text_store, text_third_cancle, text_third_confirm;
	private LinearLayout ll_function_print_order, ll_function, ll_thirdprint, ll_third_print_order;
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

	private String type;
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
					boolean result=edit_input_order_no.getText().toString().trim().matches("[0-9]{1,10}");
					if(result){
						gel.editinput(edit_input_order_no.getText().toString().trim());
						dismiss();
					}else{
						ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.waring_format_msg));
					}
				} else {
					if (!StringUtil.isEmpty(AppConfigFile.getLast_billid())) {
						gel.editinput(AppConfigFile.getLast_billid());
						dismiss();
					} else {
						ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.pleae_order_number));
					}
				}
			}
		});
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
		text_bank = (TextView) findViewById(R.id.text_bank);
		text_yxlm = (TextView) findViewById(R.id.text_yxlm);
		text_wf = (TextView) findViewById(R.id.text_wf);
		text_store = (TextView) findViewById(R.id.text_store);
		ll_function_print_order = (LinearLayout) findViewById(R.id.ll_function_print_order);
		ll_function = (LinearLayout) findViewById(R.id.ll_function);
		ll_thirdprint = (LinearLayout) findViewById(R.id.ll_thirdprint);

		edit_third_input_order_no = (EditText) findViewById(R.id.edit_third_input_order_no);
		ll_third_print_order = (LinearLayout) findViewById(R.id.ll_third_print_order);
		text_third_cancle = (TextView) findViewById(R.id.text_third_cancle);
		text_third_confirm = (TextView) findViewById(R.id.text_third_confirm);
		text_third_cancle.setOnClickListener(this);
		text_third_confirm.setOnClickListener(this);
		text_print_order.setOnClickListener(this);
		text_print_slip.setOnClickListener(this);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
		text_bank.setOnClickListener(this);
		text_yxlm.setOnClickListener(this);
		text_wf.setOnClickListener(this);
		text_store.setOnClickListener(this);
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
				ll_function.setVisibility(View.GONE);
				ll_thirdprint.setVisibility(View.VISIBLE);
//				String bankId = SpSaveUtils.read(context, ConstantData.LAST_BANK_TRANS, "");
//				if (!bankId.equals("")){
//					((MainActivity)context).print_last("1");
//					dismiss();
//				}else{
//					ToastUtils.sendtoastbyhandler(handler,"没有银行交易记录");
//				}
				break;
			case R.id.imageview_close:
				dismiss();
				break;
			case R.id.text_bank:
				type = "1";
				ll_thirdprint.setVisibility(View.GONE);
				ll_third_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_yxlm:
				type = "2";
				ll_thirdprint.setVisibility(View.GONE);
				ll_third_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_wf:
				type = "3";
				ll_thirdprint.setVisibility(View.GONE);
				ll_third_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_store:
				type = "4";
				ll_thirdprint.setVisibility(View.GONE);
				ll_third_print_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_third_cancle:
				ll_thirdprint.setVisibility(View.VISIBLE);
				ll_third_print_order.setVisibility(View.GONE);
				break;
			case R.id.text_third_confirm:
				String no = edit_third_input_order_no.getText().toString();
				closeKeyboard();
				((MainActivity)context).print_last(type, no);
				dismiss();
				break;
		}
	}

	private void closeKeyboard() {
		View view = getWindow().peekDecorView();
		if (view != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}
