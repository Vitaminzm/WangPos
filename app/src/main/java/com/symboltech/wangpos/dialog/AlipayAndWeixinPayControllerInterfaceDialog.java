package com.symboltech.wangpos.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.MainActivity;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.log.OperateLog;
import com.symboltech.wangpos.msg.entity.ThirdPay;
import com.symboltech.wangpos.result.ThirdPayCancelResult;
import com.symboltech.wangpos.result.ThirdPayQueryResult;
import com.symboltech.wangpos.result.ThirdPayResult;
import com.symboltech.wangpos.result.ThirdPaySalesReturnResult;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.OptLogEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.TextScrollView;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信or支付宝 等第三方支付 Controller
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月3日 下午8:25:57
 * @version 1.0
 */
public class AlipayAndWeixinPayControllerInterfaceDialog extends BaseDialog implements View.OnClickListener {

	/** 确认支付支付途径 */
	private int paymode;
	private String paymodeId;
	private Context context;
	/** 扫码内容 */
	private String qrmsg;
	/** 执行操作码 */
	private int operationCode;
	private LinearLayout ll_thirdpay_money_input, ll_thirdpay_hint, ll_thirdpay_result_hint, ll_thirdpay_paying_result,
			ll_paying_btn;

	private TextView tv_thirdpay_title, tv_thirdpay_cancel, tv_thirdpay_enter, tv_thirdpay_paying_title,
			tv_paying_enter, tv_thirdpay_paying_cancel, tv_thirdpay_paying_enter, tv_thirdpay_result_msg;

	private SpinKitView tv_thirdpay_result_img;
	private TextScrollView tv_thirdpay_paying_msg;

	private EditText et_thirdpay_input;

	private boolean Isinputmoney = false;
	private boolean isPay = false;

	private String trade_no;

	private double money;
	private boolean isrunning = true;

	private GetPayValue callback;
	/** refresh UI By handler */
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(context, msg);
				break;

			case 2:
				trade_no = et_thirdpay_input.getText().toString().trim();
				et_thirdpay_input.setText("");
				tv_thirdpay_title.setText(R.string.please_input_money);
				et_thirdpay_input.setHint(R.string.please_input_money);
				Isinputmoney = true;
				break;
			case 3:
				tv_thirdpay_title.setText(R.string.please_input_trade_no);
				et_thirdpay_input.setHint(R.string.please_input_trade_no);
				break;

			case 4:
				AlipayAndWeixinPayControllerInterfaceDialog.this.dismiss();
				break;
			case 5://失败
				ll_thirdpay_money_input.setVisibility(View.GONE);
				ll_thirdpay_hint.setVisibility(View.VISIBLE);
				ll_thirdpay_paying_result.setVisibility(View.VISIBLE);
				ll_thirdpay_result_hint.setVisibility(View.GONE);
				ll_paying_btn.setVisibility(View.GONE);
				tv_thirdpay_paying_msg.setVisibility(View.GONE);
				tv_paying_enter.setVisibility(View.VISIBLE);
				tv_thirdpay_paying_title.setText((String) msg.obj);
				break;
			case 6://成功
				ll_thirdpay_money_input.setVisibility(View.GONE);
				ll_thirdpay_hint.setVisibility(View.VISIBLE);
				ll_thirdpay_paying_result.setVisibility(View.GONE);
				stopPay();
				handler.sendEmptyMessageDelayed(4, 1000 * 2);
				if(callback != null)
					callback.getPayValue((ThirdPay) msg.obj);
				break;
			case 7://查询
				trade_no = (String) msg.obj;
				ll_thirdpay_money_input.setVisibility(View.GONE);
				ll_thirdpay_hint.setVisibility(View.VISIBLE);
				stopPay();
				ll_thirdpay_paying_result.setVisibility(View.VISIBLE);
				tv_paying_enter.setVisibility(View.GONE);
				tv_thirdpay_paying_title.setText(getContext().getString(R.string.thirdpay_wait_pay));
				tv_thirdpay_paying_msg.setText(getContext().getString(R.string.thirdpay_succeed_enter));

				break;
			default:
				break;
			}
		};
	};

	public AlipayAndWeixinPayControllerInterfaceDialog(Context context, String paymodeId, int mpaymode, int moperationCode, String mqrmsg, double money, boolean isPay) {
		super(context, R.style.dialog_login_bg);
		// TODO Auto-generated constructor stub
		this.paymode = mpaymode;
		this.context = context;
		this.qrmsg = mqrmsg;
		this.operationCode = moperationCode;
		this.money = money;
		this.isPay = isPay;
		this.paymodeId = paymodeId;
	}

	public AlipayAndWeixinPayControllerInterfaceDialog(Context context, String paymodeId, int mpaymode, int moperationCode, String mqrmsg, double money, boolean isPay, GetPayValue callback) {
		super(context, R.style.dialog_login_bg);
		// TODO Auto-generated constructor stub
		this.paymode = mpaymode;
		this.context = context;
		this.qrmsg = mqrmsg;
		this.operationCode = moperationCode;
		this.callback = callback;
		this.money = money;
		this.isPay = isPay;
		this.paymodeId = paymodeId;
	}
	
	public AlipayAndWeixinPayControllerInterfaceDialog(Context context, String paymodeId, int mpaymode, int moperationCode, double money, boolean isPay) {
		super(context, R.style.dialog_login_bg);
		// TODO Auto-generated constructor stub
		this.paymode = mpaymode;
		this.context = context;
		this.operationCode = moperationCode;
		this.money = money;
		this.isPay = isPay;
		this.paymodeId = paymodeId;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_alipay_weixin_pay);
		initUI();
		switchUI();
		setCanceledOnTouchOutside(false);
		et_thirdpay_input.setText(String.valueOf(money));
	}

	/**
	 * 根据操作初始化UI
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void switchUI() {
		// TODO Auto-generated method stub
		switch (operationCode) {
		case ConstantData.THIRD_OPERATION_PAY:
			// 支付操作

			break;
		case ConstantData.THIRD_OPERATION_CANCEL:
			// 支付撤销操作
			handler.sendEmptyMessage(3);
			break;
		case ConstantData.THIRD_OPERATION_QUERY:
			// 支付状态查询
			handler.sendEmptyMessage(3);
			break;
		case ConstantData.THIRD_OPERATION_SALES_RETURN:
			// 支付退货操作
			handler.sendEmptyMessage(3);
			break;

		default:
			break;
		}
	}

	private void initUI() {
		// TODO Auto-generated method stub
		ll_thirdpay_money_input = (LinearLayout) findViewById(R.id.ll_thirdpay_money_input);
		ll_thirdpay_hint = (LinearLayout) findViewById(R.id.ll_thirdpay_hint);
		ll_thirdpay_result_hint = (LinearLayout) findViewById(R.id.ll_thirdpay_result_hint);
		ll_thirdpay_paying_result = (LinearLayout) findViewById(R.id.ll_thirdpay_paying_result);
		ll_paying_btn = (LinearLayout) findViewById(R.id.ll_paying_btn);
		et_thirdpay_input = (EditText) findViewById(R.id.et_thirdpay_input);
		tv_thirdpay_title = (TextView) findViewById(R.id.tv_thirdpay_title);
		tv_thirdpay_cancel = (TextView) findViewById(R.id.tv_thirdpay_cancel);
		tv_thirdpay_enter = (TextView) findViewById(R.id.tv_thirdpay_enter);
		tv_thirdpay_paying_title = (TextView) findViewById(R.id.tv_thirdpay_paying_title);
		tv_thirdpay_paying_msg = (TextScrollView) findViewById(R.id.tv_thirdpay_paying_msg);
		tv_paying_enter = (TextView) findViewById(R.id.tv_paying_enter);
		tv_thirdpay_paying_cancel = (TextView) findViewById(R.id.tv_thirdpay_paying_cancel);
		tv_thirdpay_paying_enter = (TextView) findViewById(R.id.tv_thirdpay_paying_enter);
		tv_thirdpay_result_msg = (TextView) findViewById(R.id.tv_thirdpay_result_msg);
		tv_thirdpay_result_img = (SpinKitView) findViewById(R.id.tv_thirdpay_result_img);
		tv_thirdpay_cancel.setOnClickListener(this);
		tv_thirdpay_enter.setOnClickListener(this);
		tv_paying_enter.setOnClickListener(this);
		tv_thirdpay_paying_cancel.setOnClickListener(this);
		tv_thirdpay_paying_enter.setOnClickListener(this);
		et_thirdpay_input.setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}

		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tv_thirdpay_cancel:
			if (!isrunning) {
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.thirdpay_paying_running));
				return;
			} 
			if(operationCode == ConstantData.THIRD_OPERATION_PAY){
				if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
					OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_TRADE_CANCLE.getOptLogCode(), context.getString(R.string.alipay_trade_cancle));
				}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
					OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_TRADE_CANCLE.getOptLogCode(), context.getString(R.string.wechat_trade_cancle));
				}
			}else if(operationCode == ConstantData.THIRD_OPERATION_CANCEL){
				if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
					OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_REPEAL_CANCLE.getOptLogCode(), context.getString(R.string.alipay_repeal_cancle));
				}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
					OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_REPEAL_CANCLE.getOptLogCode(), context.getString(R.string.wechat_repeal_cancle));
				}			}else if(operationCode == ConstantData.THIRD_OPERATION_QUERY){
			}else if(operationCode == ConstantData.THIRD_OPERATION_SALES_RETURN){
				if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
					OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_RETURN_CANCLE.getOptLogCode(), context.getString(R.string.alipay_return_cancle));
				}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
					OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_RETURN_CANCLE.getOptLogCode(), context.getString(R.string.wechat_return_cancle));
				}			
			}else if(operationCode == ConstantData.THIRD_OPERATION_QUERY){
				if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
					OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_QUERY_CANCLE.getOptLogCode(), context.getString(R.string.alipay_query_cancle));
				}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
					OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_QUERY_CANCLE.getOptLogCode(), context.getString(R.string.wechat_query_cancle));
				}			
			}
			// 输入金额取消btn
			this.dismiss();
			break;
		case R.id.tv_thirdpay_enter:
			// 输入金额确认btn
			if (isrunning) {
				if (!StringUtil.isEmpty(et_thirdpay_input.getText().toString())) {
					executepayoperation();
				} else {
					ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.please_input_correct_money));
				}
			} else {
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.thirdpay_paying_running));
			}
			break;
		case R.id.tv_paying_enter:
			if (!isrunning) {
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.thirdpay_paying_running));
				return;
			} 
			// 支付失败返回信息确认btn
			this.dismiss();
			break;
		case R.id.tv_thirdpay_paying_cancel:
			if (!isrunning) {
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.thirdpay_paying_running));
				return;
			} 
			// 支付等待返回信息取消btn
			this.dismiss();
			break;
		case R.id.tv_thirdpay_paying_enter:
			// 支付等待返回信息重试btn
			if (isrunning) {
				if (!StringUtil.isEmpty(trade_no)) {
					thirdquery(trade_no);
				} else {
					ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.thirdpay_query_trade_no_null));
				}
			} else {
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.thirdpay_paying_running));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 执行相关操作
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void executepayoperation() {
		// TODO Auto-generated method stub
		switch (operationCode) {
		case ConstantData.THIRD_OPERATION_PAY:
			// 支付操作
			try {
				if (Double.parseDouble(et_thirdpay_input.getText().toString()) > 0) {
					thirdpay();
				} else {
					ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.please_input_correct_money));
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.please_input_correct_money));
				e.printStackTrace();
			}

			break;
		case ConstantData.THIRD_OPERATION_CANCEL:
			// 支付撤销操作
			thirdcancle();
			break;
		case ConstantData.THIRD_OPERATION_QUERY:
			// 支付状态查询
			thirdquery(et_thirdpay_input.getText().toString().trim());
			break;
		case ConstantData.THIRD_OPERATION_SALES_RETURN:
			// 支付退货操作
			if (Isinputmoney) {
				try {
					if (Double.parseDouble(et_thirdpay_input.getText().toString()) > 0) {
						thirdsalesreturn();
					} else {
						ToastUtils.sendtoastbyhandler(handler,
								getContext().getString(R.string.please_input_correct_money));
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					ToastUtils.sendtoastbyhandler(handler, getContext().getString(R.string.please_input_correct_money));
					e.printStackTrace();
				}

			} else {
				handler.sendEmptyMessage(2);
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 支付查询
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void thirdquery(String trade_no) {
		startPay();
		isrunning = false;
		this.trade_no = trade_no;
		// TODO Auto-generated method stub
		Map<String, String> map = new HashMap<String, String>();
		map.put("old_trade_no", trade_no);
		//map.put("pay_type", paymode + "");
		HttpRequestUtil.getinstance().thirdpayquery(map, ThirdPayQueryResult.class,
				new HttpActionHandle<ThirdPayQueryResult>() {

					@Override
					public void handleActionStart() {
						// TODO Auto-generated method stub

					}

					@Override
					public void handleActionFinish() {
						// TODO Auto-generated method stub
						isrunning = true;
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						Message message=Message.obtain();
						message.what = 7;
						message.obj = errmsg;
						handler.sendMessage(message);
					}

					@Override
					public void handleActionSuccess(String actionName, ThirdPayQueryResult result) {
						// TODO Auto-generated method stub
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_QUERY_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_query_success));
//							if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_QUERY_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_query_success));
//							}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_QUERY_SUCCESS.getOptLogCode(), context.getString(R.string.wechat_query_success));
//							}
							ToastUtils.sendtoastbyhandler(handler,"支付完成");
							AlipayAndWeixinPayControllerInterfaceDialog.this.dismiss();
							if(callback != null){
								ThirdPay thirdPay = new ThirdPay();
								thirdPay.setPay_total_fee(result.getThirdpayquery().getTotal_fee());
								thirdPay.setTrade_no(result.getThirdpayquery().getTrade_no());
								thirdPay.setSkfsid(result.getThirdpayquery().getSkfsid());
								callback.getPayValue(thirdPay);
							}
								
						} else if (ConstantData.HTTP_RESPONSE_THIRDPAY_WAIT.equals(result.getCode())) {
							Message message=Message.obtain();
							message.what = 7;
							message.obj = result.getThirdpayquery().getTrade_no();
							handler.sendMessage(message);
						} else {
								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_QUERY_FAILED.getOptLogCode(), context.getString(R.string.alipay_query_failed));
//							if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_QUERY_FAILED.getOptLogCode(), context.getString(R.string.alipay_query_failed));
//							}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_QUERY_FAILED.getOptLogCode(), context.getString(R.string.wechat_query_failed));
//							}
							Message message=Message.obtain();
							message.what = 5;
							message.obj = result.getMsg();
							handler.sendMessage(message);
						}
					}

					@Override
					public void handleActionOffLine() {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.offline_waring));
					}

					@Override
					public void handleActionChangeToOffLine() {
						// TODO Auto-generated method stub
						Intent intent = new Intent(context, MainActivity.class);
						context.startActivity(intent);
					}
				});
	}

	/**
	 * 支付退货
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void thirdsalesreturn() {
		// TODO Auto-generated method stub
		startPay();
		isrunning = false;
		Map<String, String> map = new HashMap<String, String>();
		map.put("operater", SpSaveUtils.read(context, ConstantData.CASHIER_CODE, ""));
		//map.put("pay_type", paymode + "");
		map.put("old_trade_no", trade_no);
		map.put("billid", "-" + AppConfigFile.getBillId());
		map.put("total_fee", CurrencyUnit.yuan2fenStr(et_thirdpay_input.getText().toString()));
		LogUtil.i("lgs", "old_trade_no===" + trade_no + "==billid==" + map.get("billid") + "===" + map.get("total_fee")
				+ "==" + map.get("pay_type"));
		HttpRequestUtil.getinstance().thirdpaysalesreturn(map, ThirdPaySalesReturnResult.class,
				new HttpActionHandle<ThirdPaySalesReturnResult>() {

					@Override
					public void handleActionStart() {
						// TODO Auto-generated method stub

					}

					@Override
					public void handleActionFinish() {
						// TODO Auto-generated method stub
						isrunning = true;
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						Message message=Message.obtain();
						message.what = 5;
						message.obj = errmsg;
						handler.sendMessage(message);
					}

					@Override
					public void handleActionSuccess(String actionName, ThirdPaySalesReturnResult result) {
						// TODO Auto-generated method stub
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
//							if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_RETURN_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_return_success));
//							}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_RETURN_SUCCESS.getOptLogCode(), context.getString(R.string.wechat_return_success));
//							}
							OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_RETURN_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_return_success));
							ll_thirdpay_money_input.setVisibility(View.GONE);
							ll_thirdpay_hint.setVisibility(View.VISIBLE);
							stopPay();
							ll_thirdpay_paying_result.setVisibility(View.GONE);
							tv_thirdpay_result_msg
									.setText(getContext().getString(R.string.thirdpay_salesreturn_succeed));
							handler.sendEmptyMessageDelayed(4, 1000 * 2);
						} else {
								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_RETURN_FAILED.getOptLogCode(), context.getString(R.string.alipay_return_failed));
//							if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_RETURN_FAILED.getOptLogCode(), context.getString(R.string.alipay_return_failed));
//							}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_RETURN_FAILED.getOptLogCode(), context.getString(R.string.wechat_return_failed));
//							}
							ll_thirdpay_money_input.setVisibility(View.GONE);
							ll_thirdpay_hint.setVisibility(View.VISIBLE);
							ll_thirdpay_paying_result.setVisibility(View.VISIBLE);
							ll_thirdpay_result_hint.setVisibility(View.GONE);
							ll_paying_btn.setVisibility(View.GONE);
							tv_thirdpay_paying_title.setText(R.string.thirdpay_returngood_err);
							tv_thirdpay_paying_msg.setText(result.getMsg());
						}
					}

					@Override
					public void handleActionOffLine() {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.offline_waring));
					}

					@Override
					public void handleActionChangeToOffLine() {
						// TODO Auto-generated method stub
						Intent intent = new Intent(context, MainActivity.class);
						context.startActivity(intent);
					}
				});
	}

	/**
	 * 支付撤销
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void thirdcancle() {
		// TODO Auto-generated method stub
		startPay();
		isrunning = false;
		Map<String, String> map = new HashMap<String, String>();
		map.put("operater", SpSaveUtils.read(context, ConstantData.CASHIER_CODE, ""));
		//map.put("pay_type", paymode + "");
		map.put("old_trade_no", et_thirdpay_input.getText().toString().trim());
		map.put("billid", "-" + AppConfigFile.getBillId());
		LogUtil.i("lgs", "old_trade_no===" + map.get("old_trade_no") + "==billid==" + map.get("billid") + "===" + "=="
				+ map.get("pay_type"));
		HttpRequestUtil.getinstance().thirdpaycancel(map, ThirdPayCancelResult.class,
				new HttpActionHandle<ThirdPayCancelResult>() {

					@Override
					public void handleActionStart() {
						// TODO Auto-generated method stub

					}

					@Override
					public void handleActionFinish() {
						// TODO Auto-generated method stub
						isrunning = true;
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						Message message=Message.obtain();
						message.what = 5;
						message.obj = errmsg;
						handler.sendMessage(message);
					}

					@Override
					public void handleActionSuccess(String actionName, ThirdPayCancelResult result) {
						// TODO Auto-generated method stub
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_REPEAL_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_repeal_success));
//							if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_REPEAL_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_repeal_success));
//							}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_REPEAL_SUCCESS.getOptLogCode(), context.getString(R.string.wechat_repeal_success));
//							}
						} else {
								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_REPEAL_FAILED.getOptLogCode(), context.getString(R.string.alipay_repeal_failed));
//							if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_REPEAL_FAILED.getOptLogCode(), context.getString(R.string.alipay_repeal_failed));
//							}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//								OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_REPEAL_FAILED.getOptLogCode(), context.getString(R.string.wechat_repeal_failed));
//							}
							ll_thirdpay_money_input.setVisibility(View.GONE);
							ll_thirdpay_hint.setVisibility(View.VISIBLE);
							ll_thirdpay_paying_result.setVisibility(View.VISIBLE);
							ll_thirdpay_result_hint.setVisibility(View.GONE);
							ll_paying_btn.setVisibility(View.GONE);
							tv_thirdpay_paying_title.setText(R.string.thirdpay_cancle_err);
							tv_thirdpay_paying_msg.setText(result.getMsg());
						}
					}

					@Override
					public void handleActionOffLine() {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.offline_waring));
					}

					@Override
					public void handleActionChangeToOffLine() {
						// TODO Auto-generated method stub
						Intent intent = new Intent(context, MainActivity.class);
						context.startActivity(intent);
					}
				});
	}

	private void startPay(){
		ll_thirdpay_money_input.setVisibility(View.GONE);
		ll_thirdpay_hint.setVisibility(View.VISIBLE);
		ll_thirdpay_paying_result.setVisibility(View.GONE);
		tv_thirdpay_result_msg.setText(R.string.thirdpay_paying);
		tv_thirdpay_result_img.setVisibility(View.VISIBLE);
	}
	
	private void stopPay(){
		tv_thirdpay_result_msg.setText(R.string.thirdpay_succeed);
		tv_thirdpay_result_img.setVisibility(View.GONE);
	}
	/**
	 * 支付操作
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void thirdpay() {
		// TODO Auto-generated method stub
		startPay();
		isrunning = false;
		Map<String, String> map = new HashMap<String, String>();
		String ip = "";
		if(!StringUtil.isEmpty(Utils.getLocalIpAddress())){
			ip = Utils.getLocalIpAddress();
		}
		map.put("posip", ip);
		//map.put("pay_type", paymode + "");
		map.put("total_fee", CurrencyUnit.yuan2fenStr(et_thirdpay_input.getText().toString()));
		if(isPay) {
			map.put("billid", "" + AppConfigFile.getBillId());
		}else {
			map.put("billid", "-" + AppConfigFile.getBillId());
		}
		map.put("skfsid", paymodeId);
		map.put("auth_code", qrmsg);
		LogUtil.i("lgs", map.get("pay_type") + "==" + map.get("total_fee") + "==" + map.get("billid") + "==="
				+ map.get("skfsid") + "==="+ map.get("auth_code"));
		HttpRequestUtil.getinstance().thirdpay(map, ThirdPayResult.class, new HttpActionHandle<ThirdPayResult>() {

			@Override
			public void handleActionStart() {
				// TODO Auto-generated method stub

			}

			@Override
			public void handleActionFinish() {
				// TODO Auto-generated method stub
				isrunning = true;
			}

			@Override
			public void handleActionError(String actionName, String errmsg) {
				// TODO Auto-generated method stub
				ToastUtils.sendtoastbyhandler(handler, errmsg);
				Message message=Message.obtain();
				message.what = 7;
				message.obj = errmsg;
				handler.sendMessage(message);
			}

			@Override
			public void handleActionSuccess(String actionName, ThirdPayResult result) {
				// TODO Auto-generated method stub
				if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
						OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_TRADE_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_trade_success));
//					if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//						OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_TRADE_SUCCESS.getOptLogCode(), context.getString(R.string.alipay_trade_success));
//					}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//						OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_TRADE_SUCCESS.getOptLogCode(), context.getString(R.string.wechat_trade_success));
//					}
					Message message=Message.obtain();
					message.what = 6;
					message.obj = result.getThirdpay();
					handler.sendMessage(message);
				} else if (ConstantData.HTTP_RESPONSE_THIRDPAY_WAIT.equals(result.getCode())) {
					Message message=Message.obtain();
					message.what = 7;
					message.obj = result.getThirdpay().getTrade_no();
					handler.sendMessage(message);
				} else {
						OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_TRADE_FAILED.getOptLogCode(), context.getString(R.string.alipay_trade_failed));
//					if(paymode == ConstantData.PAYMODE_BY_ALIPAY){
//						OperateLog.getInstance().saveLog2File(OptLogEnum.ALIPAY_TRADE_FAILED.getOptLogCode(), context.getString(R.string.alipay_trade_failed));
//					}else if(paymode == ConstantData.PAYMODE_BY_WEIXIN){
//						OperateLog.getInstance().saveLog2File(OptLogEnum.WECHAT_TRADE_FAILED.getOptLogCode(), context.getString(R.string.wechat_trade_failed));
//					}
					Message message=Message.obtain();
					message.what = 5;
					message.obj = result.getMsg();
					handler.sendMessage(message);
				}
			}

			@Override
			public void handleActionOffLine() {
				// TODO Auto-generated method stub
				ToastUtils.sendtoastbyhandler(handler, context.getString(R.string.offline_waring));
			}

			@Override
			public void handleActionChangeToOffLine() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, MainActivity.class);
				context.startActivity(intent);
			}
		});

	}

	public interface GetPayValue{
		public void getPayValue(ThirdPay value);
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		handler.removeCallbacksAndMessages(null);
		super.dismiss();
	}
}
