package com.symboltech.wangpos.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.MainActivity;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.interfaces.OnReturnFinishListener;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.result.ThirdPaySalesReturnResult;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;

import java.util.HashMap;
import java.util.Map;


/**
 * 第三方支付退款dialog
 * @author so
 *
 */
public class AlipayAndWeixinPayReturnDialog extends Dialog implements View.OnClickListener {

	private Context mContext;
	private LinearLayout serialTable, statusTable, resultTable;
	private EditText serialNumber;
	private ImageView imageview_close;
	private TextView serialTips, confirm, statusTips, resultTips, no, yes, typeTips;
	private SpinKitView statusIcon;
	private String type, billid, money;
	private String tradeNo;
	private final int STATUS_CODE_SUCCESS = 200;
	private final int STATUS_CODE_FAIL = -1;
	private final int STATUS_CODE_ERROR = -2;
	private OnReturnFinishListener onReturnFinishListener;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == STATUS_CODE_SUCCESS) {
				statusIcon.setVisibility(View.VISIBLE);
				if("1".equals(type)) {
					statusTips.setText("支付宝"+mContext.getResources().getString(R.string.thirdpay_salesreturn_succeed));
				}else if("3".equals(type)) {
					statusTips.setText("微信"+mContext.getResources().getString(R.string.thirdpay_salesreturn_succeed));
				}
				if(onReturnFinishListener != null) {
					onReturnFinishListener.finish(true);
				}
				dismiss();
			}else if(msg.what == STATUS_CODE_FAIL) {
				if("1".equals(type)) {
					statusTips.setText("支付宝"+mContext.getResources().getString(R.string.thirdpay_salesreturn_fail));
				}else if("3".equals(type)) {
					statusTips.setText("微信"+mContext.getResources().getString(R.string.thirdpay_salesreturn_fail));
				}
				statusTips.setText((String) msg.obj);
				statusTable.setVisibility(View.GONE);
				resultTable.setVisibility(View.VISIBLE);
			}else if(msg.what == STATUS_CODE_ERROR) {
				dismiss();
			}
		};
	};

	/**
	 * 
	 * @param context
	 * @param type  1是支付宝 3是微信
	 * @param tradeNo  交易流水 输入null代表需要手动输入
	 * @param billid
	 * @param money
	 */
	public AlipayAndWeixinPayReturnDialog(Context context, String type, String tradeNo, String billid, String money) {
		super(context, R.style.dialog_login_bg);
		mContext = context;
		this.type = type;
		this.tradeNo = tradeNo;
		this.billid = billid;
		this.money = money;
	}
	
	public AlipayAndWeixinPayReturnDialog(Context context, String type, String tradeNo, String billid, String money, OnReturnFinishListener listener) {
		super(context, R.style.dialog_login_bg);
		mContext = context;
		this.type = type;
		this.tradeNo = tradeNo;
		this.billid = billid;
		this.money = money;
		onReturnFinishListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alipay_weixin_return);
		initView();
	}

	private void initView() {
		serialTable = (LinearLayout) findViewById(R.id.dialog_alipay_weixin_return_serial);
		typeTips = (TextView) findViewById(R.id.dialog_alipay_tip_type);
		serialTips = (TextView) findViewById(R.id.dialog_alipay_weixin_return_serial_tips);
		serialNumber = (EditText) findViewById(R.id.dialog_alipay_weixin_return_serial_et);
		confirm = (TextView) findViewById(R.id.dialog_alipay_weixin_return_serial_confirm);
		statusTable = (LinearLayout) findViewById(R.id.dialog_alipay_weixin_return_status);
		statusIcon = (SpinKitView) findViewById(R.id.dialog_alipay_weixin_return_status_icon);
		statusTips = (TextView) findViewById(R.id.dialog_alipay_weixin_return_status_tips);
		resultTable = (LinearLayout) findViewById(R.id.dialog_alipay_weixin_return_result);
		resultTips = (TextView) findViewById(R.id.dialog_alipay_weixin_return_result_tips);
		no = (TextView) findViewById(R.id.dialog_alipay_weixin_return_result_cancel);
		yes = (TextView) findViewById(R.id.dialog_alipay_weixin_return_result_confirm);
		imageview_close = (ImageView) findViewById(R.id.imageview_close);
		new HorizontalKeyBoard(mContext, this, serialNumber, null);
		initEvent();
		if((ConstantData.PAYMODE_BY_ALIPAY+"").equals(type)) {
			if(tradeNo == null) {
				//没输入交易流水号，代表是普通退货
				typeTips.setText(mContext.getResources().getString(R.string.please_input_alipay_trade_no));
			}else {
				//选单退货需要遵循动态配置
				if(ConstantData.THIRD_NEED_INPUT.equals(SpSaveUtils.read(mContext, ConstantData.MALL_ALIPAY_IS_INPUT, "0"))) {
					//不需要输入
					serialTable.setVisibility(View.GONE);
					statusTips.setText("支付宝"+mContext.getResources().getString(R.string.thirdpay_salesreturn_succee_ing));
					statusTable.setVisibility(View.VISIBLE);
					getBackResult(tradeNo);
				}else {
					//需要输入
					typeTips.setText(mContext.getResources().getString(R.string.please_input_alipay_trade_no));
				}
			}
		}else if((ConstantData.PAYMODE_BY_WEIXIN+"").equals(type)) {
			if(tradeNo == null) {
				typeTips.setText(mContext.getResources().getString(R.string.please_input_weixin_trade_no));
			}else {
				if(ConstantData.THIRD_NEED_INPUT.equals(SpSaveUtils.read(mContext, ConstantData.MALL_WEIXIN_IS_INPUT, "0"))) {
					serialTable.setVisibility(View.GONE);
					statusTips.setText("微信"+mContext.getResources().getString(R.string.thirdpay_salesreturn_succee_ing));
					statusTable.setVisibility(View.VISIBLE);
					getBackResult(tradeNo);
				}else {
					typeTips.setText(mContext.getResources().getString(R.string.please_input_weixin_trade_no));
				}
			}
		}
		serialTips.setText(mContext.getResources().getString(R.string.thirdpay_deal_time) + money + mContext.getResources().getString(R.string.yuan));
	}

	private void initEvent() {
		confirm.setOnClickListener(this);
		no.setOnClickListener(this);
		yes.setOnClickListener(this);
		imageview_close.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == confirm.getId()) {
			String number = serialNumber.getText().toString();
			if(TextUtils.isEmpty(number) || "".equals(number)) {
				Toast.makeText(mContext, mContext.getResources().getString(R.string.please_input_trade_no), Toast.LENGTH_SHORT).show();
				return;
			}
			serialTable.setVisibility(View.GONE);
			if("1".equals(type)) {
				statusTips.setText("支付宝"+mContext.getResources().getString(R.string.thirdpay_salesreturn_succee_ing));
			}else if("3".equals(type)) {
				statusTips.setText("微信"+mContext.getResources().getString(R.string.thirdpay_salesreturn_succee_ing));
			}
			statusTable.setVisibility(View.VISIBLE);
			getBackResult(number);
		}
		if(v.getId() == no.getId() || v.getId() == imageview_close.getId()) {
			dismiss();
		}
		if(v.getId() == yes.getId()) {
			if(onReturnFinishListener != null) {
				onReturnFinishListener.finish(false);
			}
			dismiss();
		}
	}

	/**
	 * 获取退款结果
	 * @param number
	 */
	private void getBackResult(String number) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("operater", SpSaveUtils.read(mContext, ConstantData.CASHIER_CODE, ""));
		map.put("total_fee", MoneyAccuracyUtils.thirdpaymoneydealbyinput(money));
		map.put("billid", billid);
		map.put("old_trade_no", number);
		map.put("pay_type", type);
		LogUtil.i("lgs", "old_trade_no===" + number + "billid==" + billid + "===" + MoneyAccuracyUtils.thirdpaymoneydealbyinput(money) + "==" + type);
		HttpRequestUtil.getinstance().thirdpaysalesreturn(map, ThirdPaySalesReturnResult.class, new HttpActionHandle<ThirdPaySalesReturnResult>() {

			@Override
			public void handleActionStart() {
				
			}

			@Override
			public void handleActionFinish() {
				
			}

			@Override
			public void handleActionError(String actionName, String errmsg) {
				ToastUtils.sendtoastbyhandler(mHandler, errmsg);
				mHandler.sendEmptyMessageDelayed(STATUS_CODE_ERROR, 500);
			}

			@Override
			public void handleActionSuccess(String actionName, ThirdPaySalesReturnResult result) {
				Message msg = Message.obtain();
				if(ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
					msg.what = STATUS_CODE_SUCCESS;
				}else {
					msg.obj = result.getMsg();
					msg.what = STATUS_CODE_FAIL;
				}
				mHandler.sendMessageDelayed(msg, 1500);
			}

			@Override
			public void handleActionOffLine() {
				Toast.makeText(mContext, R.string.offline_waring, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void handleActionChangeToOffLine() {
				Intent intent = new Intent(mContext, MainActivity.class);
				mContext.startActivity(intent);
			}
		});
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		mHandler.removeCallbacksAndMessages(null);
		super.dismiss();
	}
}
