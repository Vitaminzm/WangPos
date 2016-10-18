package com.symboltech.wangpos.dialog;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.ReturnGoodsByNormalActivity;
import com.symboltech.wangpos.activity.ReturnGoodsByOrderActivity;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.KeyBoardListener;
import com.symboltech.wangpos.msg.entity.BillInfo;
import com.symboltech.wangpos.result.BillResult;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.util.HashMap;
import java.util.Map;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class ReturnDialog extends Dialog implements View.OnClickListener {
	public Context context;
	private EditText edit_input_order_no;
	private TextView text_return_normal, text_return_order, text_cancle, text_confirm, text_status;
	private ImageView imageview_close;
	private LinearLayout ll_function_return_order, ll_function, ll_status;
	private SpinKitView spin_kit;
	private final int STATUS_CODE_SUCCESS = 0;
	private final int STATUS_CODE_FAIL = -1;

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(context, msg);
					break;
				case STATUS_CODE_SUCCESS:
					dismiss();
					Intent orderIntent = new Intent(context, ReturnGoodsByOrderActivity.class);
					orderIntent.putExtra(ConstantData.BILL, (BillInfo) msg.obj);
					context.startActivity(orderIntent);
					break;
				case STATUS_CODE_FAIL:
					ReturnDialog.this.setCanceledOnTouchOutside(false);
					text_status.setText(R.string.order_searching);
					text_status.setTextColor(context.getResources().getColor(R.color.green));
					spin_kit.setVisibility(View.VISIBLE);
					ll_status.setVisibility(View.GONE);
					ll_function_return_order.setVisibility(View.VISIBLE);
					if (!StringUtil.isEmpty(MyApplication.getLast_billid())) {
						edit_input_order_no.setHint(MyApplication.getLast_billid());
					}
					break;
				default:
					break;
			}
		}

		;
	};
	private HorizontalKeyBoard keyboard;

	public ReturnDialog(Context context) {
		super(context, R.style.dialog_login_bg);
		this.context = context;
	}

	@Override
	public void dismiss() {
		super.dismiss();
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_return);
		this.setCanceledOnTouchOutside(true);
		initUI();
		keyboard = new HorizontalKeyBoard(context, this, edit_input_order_no, null);
		if (!StringUtil.isEmpty(MyApplication.getLast_billid())) {
			edit_input_order_no.setHint(MyApplication.getLast_billid());
		}
	}

	private void initUI() {
		edit_input_order_no = (EditText) findViewById(R.id.edit_input_order_no);
		text_return_normal = (TextView) findViewById(R.id.text_return_normal);
		text_return_order = (TextView) findViewById(R.id.text_return_order);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		text_cancle = (TextView) findViewById(R.id.text_cancle);
		text_confirm = (TextView) findViewById(R.id.text_confirm);
		text_status = (TextView) findViewById(R.id.text_status);
		spin_kit = (SpinKitView) findViewById(R.id.spin_kit);
		ll_function_return_order = (LinearLayout) findViewById(R.id.ll_function_return_order);
		ll_function = (LinearLayout) findViewById(R.id.ll_function);
		ll_status = (LinearLayout) findViewById(R.id.ll_status);
		text_return_normal.setOnClickListener(this);
		text_return_order.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
		text_cancle.setOnClickListener(this);
		text_confirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id){
			case R.id.text_return_normal:
				dismiss();
				Intent intentJob = new Intent(context, ReturnGoodsByNormalActivity.class);
				context.startActivity(intentJob);
				break;
			case R.id.text_return_order:
				ll_function.setVisibility(View.GONE);
				ll_function_return_order.setVisibility(View.VISIBLE);
				break;
			case R.id.text_cancle:
				if(keyboard.isShowing()){
					keyboard.dismiss();
				}
				ll_function.setVisibility(View.VISIBLE);
				ll_function_return_order.setVisibility(View.GONE);
				break;
			case R.id.text_confirm:
				if (!StringUtil.isEmpty(edit_input_order_no.getText().toString().trim())) {
					boolean result=edit_input_order_no.getText().toString().trim().matches("[0-9]{1,10}");
					if(result){
						getOrderInfo(SpSaveUtils.read(context, ConstantData.CASHIER_DESK_CODE, ""), edit_input_order_no.getText().toString().trim());
					}else{
						ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.waring_format_msg));
					}
				} else {
					if (!StringUtil.isEmpty(MyApplication.getLast_billid())) {
						getOrderInfo(SpSaveUtils.read(context, ConstantData.CASHIER_DESK_CODE, ""), MyApplication.getLast_billid());
					} else {
						ToastUtils.sendtoastbyhandler(handler,context.getString(R.string.pleae_order_number));
					}
				}
				break;
			case R.id.imageview_close:
				dismiss();
				break;
		}
	}

	/**
	 * 获取订单信息
	 * @param posno pos  id
	 * @param billId  订单号
	 */
	private void getOrderInfo(final String posno, final String billId) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("posno", posno);
		map.put("billid", billId);
		HttpRequestUtil.getinstance().getOrderInfo(map, BillResult.class, new HttpActionHandle<BillResult>() {
			@Override
			public void handleActionStart() {
				ll_function_return_order.setVisibility(View.GONE);
				ll_status.setVisibility(View.VISIBLE);
				super.handleActionStart();
			}

			@Override
			public void handleActionFinish() {
				super.handleActionFinish();
				ReturnDialog.this.setCanceledOnTouchOutside(true);
			}

			@Override
			public void handleActionError(String actionName, final String errmsg) {
				((Activity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						text_status.setText(errmsg);
						spin_kit.setVisibility(View.GONE);
						text_status.setTextColor(context.getResources().getColor(R.color.orange));
						handler.sendEmptyMessageDelayed(STATUS_CODE_FAIL, 1500);
					}
				});

			}

			@Override
			public void handleActionSuccess(String actionName, final BillResult result) {
				((Activity)context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						spin_kit.setVisibility(View.GONE);
						Message msg = Message.obtain();
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
							text_status.setText(context.getResources().getString(R.string.order_seach_success));
							text_status.setTextColor(context.getResources().getColor(R.color.green));
							msg.what = STATUS_CODE_SUCCESS;
							BillInfo bill = result.getTicketInfo().getBillinfo();
							bill.setOldposno(posno);
							bill.setOldbillid(billId);
							msg.obj = result.getTicketInfo().getBillinfo();
							handler.sendMessageDelayed(msg, 1500);
						} else {
							text_status.setText(result.getMsg());
							text_status.setTextColor(context.getResources().getColor(R.color.orange));
							handler.sendEmptyMessageDelayed(STATUS_CODE_FAIL, 1500);
						}
					}
				});

			}
		});
	}
}
