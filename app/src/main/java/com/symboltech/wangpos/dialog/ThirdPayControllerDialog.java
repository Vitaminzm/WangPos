package com.symboltech.wangpos.dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.gson.reflect.TypeToken;
import com.symboltech.koolcloud.aidl.AidlRequestManager;
import com.symboltech.koolcloud.interfaces.RemoteServiceStateChangeListerner;
import com.symboltech.koolcloud.transmodel.AidlPaymentInfo;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.BaseActivity;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.msg.entity.PayMentsInfo;
import com.symboltech.wangpos.msg.entity.WposBankRefundInfo;
import com.symboltech.wangpos.msg.entity.WposPayInfo;
import com.symboltech.wangpos.result.ThirdPayCancelResult;
import com.symboltech.wangpos.result.ThirdPayQueryResult;
import com.symboltech.wangpos.result.ThirdPayResult;
import com.symboltech.wangpos.result.ThirdPaySalesReturnResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.CashierSign;
import com.symboltech.wangpos.utils.CodeBitmap;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.symboltech.wangpos.view.HorizontalKeyBoard;
import com.symboltech.zxing.app.CaptureActivity;
import com.ums.AppHelper;
import com.wangpos.pay.UnionPay.PosConfig;
import com.wangpos.poscore.PosCore;
import com.wangpos.poscore.impl.PosCoreFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.thirdparty.aidl.IKuYunThirdPartyService;
import cn.koolcloud.engine.thirdparty.aidlbean.LoginRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.SaleRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;
import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

public class ThirdPayControllerDialog extends BaseActivity{

	@Bind(R.id.ll_function)
	LinearLayout ll_function;
	@Bind(R.id.tv_query)
	TextView tv_query;

	@Bind(R.id.ll_input_money)
	LinearLayout ll_input_money;
	@Bind(R.id.edit_money)
	EditText edit_money;

	@Bind(R.id.ll_paying_by_code)
	LinearLayout ll_paying_by_code;
	@Bind(R.id.imageview_close)
	ImageView imageview_close;
	@Bind(R.id.image_paying_code)
	ImageView image_paying_code;

	@Bind(R.id.ll_paying_msg)
	LinearLayout ll_paying_msg;
	@Bind(R.id.text_paying_msg)
	TextView text_paying_msg;
	@Bind(R.id.text_confirm_query)
	TextView text_confirm_query;
	@Bind(R.id.text_title)
	TextView text_title;



	@Bind(R.id.ll_function1)
	LinearLayout ll_function1;
	@Bind(R.id.ll_functions_type)
	LinearLayout ll_functions_type;


	@Bind(R.id.ll_paying_status)
	LinearLayout ll_paying_status;
	@Bind(R.id.text_status)
	TextView text_status;
	@Bind(R.id.spin_kit)
	SpinKitView spin_kit;


	@Bind(R.id.ll_pay_signin)
	LinearLayout ll_pay_signin;
	@Bind(R.id.ll_pay_clear)
	LinearLayout ll_pay_clear;
	private String trade_no = null;

	private String bankType;
	protected IKuYunThirdPartyService mYunService;
	protected RemoteServiceStateChangeListerner serviceStateChangeListerner = null;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mYunService = IKuYunThirdPartyService.Stub.asInterface(service);
			if (serviceStateChangeListerner != null) {
				serviceStateChangeListerner.onServiceConnected(name, service);
			}
			try {
				String rspString = mYunService.isLogin();
				LogUtil.d("lgs","==Login rsp is :" + rspString);
				final JSONObject rspJsonObject = new JSONObject(rspString);
				new Thread(new Runnable() {
					@Override
					public void run() {
						if (rspJsonObject.optString("responseCode").equals("00")) {
							SpSaveUtils.write(ThirdPayControllerDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
							SpSaveUtils.write(ThirdPayControllerDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
							SpSaveUtils.write(ThirdPayControllerDialog.this, ConstantData.PWD, "111111");
							JSONArray payments = null;
							try {
								String rspStr = mYunService.getPaymentList();
								JSONObject rspJSON = new JSONObject(rspStr);
								payments = rspJSON.optJSONArray("paymentlList");
								LogUtil.i("lgs", payments.toString());
								SpSaveUtils.saveObject(ThirdPayControllerDialog.this, ConstantData.PAY_TYPE_LIST, GsonUtil.jsonToArrayList(payments.toString(), AidlPaymentInfo.class));
							} catch (RemoteException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							return;
						}else{
							login();
						}
					}
				}).start();
			} catch (JSONException | RemoteException e1) {
				e1.printStackTrace();
			}

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mYunService = null;
			if (serviceStateChangeListerner != null) {
				serviceStateChangeListerner.onServiceDisconnected(name);
			}
		}
	};
	private String qrcode = "";
	private String type_function;
	private HorizontalKeyBoard keyboard;
	private double money;
	private boolean isrunning = false;
	private PosCore pCore;

	private void login() {
		LoginRequest request = new LoginRequest(SpSaveUtils.read(ThirdPayControllerDialog.this, ConstantData.CUSTOMERID,""), SpSaveUtils.read(ThirdPayControllerDialog.this, ConstantData.USERID,""), SpSaveUtils.read(ThirdPayControllerDialog.this, ConstantData.PWD,""));
		AidlRequestManager aidlManager = AidlRequestManager.getInstance();
		aidlManager.aidlLoginRequest(mYunService, request,
				new AidlRequestManager.AidlRequestCallBack() {

					@Override
					public void onTaskStart() {
						ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								setFinishOnTouchOutside(false);
								ll_function.setVisibility(View.GONE);
								ll_paying_status.setVisibility(View.VISIBLE);
								spin_kit.setVisibility(View.VISIBLE);
								text_status.setText(R.string.thirdpay_loading);
							}
						});
					}

					@Override
					public void onTaskFinish(JSONObject rspJsonObject) {
						if (rspJsonObject.optString("responseCode").equals(
								"00")) {
							SpSaveUtils.write(ThirdPayControllerDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
							SpSaveUtils.write(ThirdPayControllerDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
							SpSaveUtils.write(ThirdPayControllerDialog.this, ConstantData.PWD, "111111");
							JSONArray payments = null;
							try {
								String rspStr = mYunService.getPaymentList();
								JSONObject rspJSON = new JSONObject(rspStr);
								payments = rspJSON.optJSONArray("paymentlList");
								LogUtil.i("lgs",payments.toString());
								SpSaveUtils.saveObject(ThirdPayControllerDialog.this, ConstantData.PAY_TYPE_LIST, GsonUtil.jsonToArrayList(payments.toString(), AidlPaymentInfo.class));
							} catch (RemoteException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									setFinishOnTouchOutside(true);
									ll_function.setVisibility(View.VISIBLE);
									ll_paying_status.setVisibility(View.GONE);
								}
							});
							return;
						} else {
							setFinishOnTouchOutside(true);
							ToastUtils.sendtoastbyhandler(handler, rspJsonObject.optString("errorMsg"));
							ThirdPayControllerDialog.this.finish();
						}
					}

					@Override
					public void onTaskCancelled() {
						ThirdPayControllerDialog.this.finish();
					}

					@Override
					public void onException(Exception e) {
						ToastUtils.sendtoastbyhandler(handler, e.toString());
						ThirdPayControllerDialog.this.finish();
					}
				});
	}

	private Timer timer = new Timer();
	public int times = 60;
	TimerTask timerTask = new TimerTask() {
		@Override
		public void run() {
			if (times > 1) {
				if (this != null) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							text_status.setText(getString(R.string.thirdpay_input_pwd)+times + "s");
							times -- ;
						}
					});
				}
			} else {
				cancel();
			}

		}

	};
	public static final int  TRANS_AGAIN = 1;
	public static final int  TRANS_CANCLE = 2;
	public static final int  TRANS_FINISH = 3;
	static class MyHandler extends Handler {
		WeakReference<BaseActivity> mActivity;

		MyHandler(BaseActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseActivity theActivity = mActivity.get();
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(theActivity,msg);
					break;
				case TRANS_AGAIN:
					((ThirdPayControllerDialog)theActivity).initUi();
					break;
				case TRANS_CANCLE:
					theActivity.finish();
					break;
				case TRANS_FINISH:
					((ThirdPayControllerDialog)theActivity).handleTransResult(true, (String) msg.obj);
					break;
			}
		}
	}

	MyHandler handler = new MyHandler(this);

	public void initUi(){
		trade_no = null;
		setFinishOnTouchOutside(true);
		ll_function.setVisibility(View.VISIBLE);
		ll_paying_by_code.setVisibility(View.GONE);
		ll_paying_msg.setVisibility(View.GONE);
		ll_input_money.setVisibility(View.GONE);
		edit_money.setText("");
		ll_paying_status.setVisibility(View.GONE);
		imageview_close.setVisibility(View.GONE);
	}

	private String Type;
	private String payMode;
	@Override
	protected void initData() {
		this.Type = getIntent().getStringExtra(ConstantData.PAY_TYPE);
		this.payMode = getIntent().getStringExtra(ConstantData.PAY_MODE);
		if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
			initPosCore();
			tv_query.setText("查余");
			ll_function.setVisibility(View.GONE);
			ll_functions_type.setVisibility(View.VISIBLE);
		}else if(Type.equals(ConstantData.YXLM_ID) || Type.equals(PaymentTypeEnum.WECHAT.getStyletype())){
			if(Type.equals(ConstantData.YXLM_ID)){
				tv_query.setText("查余");
				text_title.setText(R.string.yxlm);
			}else{
				text_title.setText(R.string.codepay);
			}
			ll_functions_type.setVisibility(View.GONE);
		}

//		Intent yunIntent = new Intent(IKuYunThirdPartyService.class.getName());
//		yunIntent = AndroidUtils.getExplicitIntent(this, yunIntent);
//		setServiceStateChangeListerner(serviceStateChangeListerner1);
//		if (yunIntent == null) {
//		} else {
//			bindService(yunIntent, connection, Context.BIND_AUTO_CREATE);
//		}
	}

	private RemoteServiceStateChangeListerner serviceStateChangeListerner1 = new RemoteServiceStateChangeListerner() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ThirdPayControllerDialog.this);
			// 发生在支付服务异常关闭（升级或其他情况）
			builder.setTitle("提示");
			builder.setMessage("支付服务已断开，请检查是否安装支付服务后，重新打开应用");
			builder.setCancelable(false);
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
							dialog.dismiss();
						}
					});
			builder.create().show();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.e("lgs","服务已绑定");
		}
	};

	public void setServiceStateChangeListerner(
			RemoteServiceStateChangeListerner serviceStateChangeListerner) {
		this.serviceStateChangeListerner = serviceStateChangeListerner;
	}
	@Override
	protected void initView() {
		setContentView(R.layout.dialog_thirdpay_controller);
		setFinishOnTouchOutside(true);
		ButterKnife.bind(this);
		keyboard = new HorizontalKeyBoard(this, this, edit_money, null);
	}

	@Override
	protected void recycleMemery() {
		if(isWaitSwip){
			try {
				mYunService.cancelSwipeCard();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		handler.removeCallbacksAndMessages(null);
		try {
			unbindService(connection);
		} catch (Exception e) {
			// 如果重复解绑会抛异常
		}
	}

	@OnClick({R.id.text_confirm_query, R.id.text_cancle, R.id.text_confirm, R.id.imageview_close, R.id.ll_pay_jiaoyi, R.id.ll_pay_search, R.id.ll_pay_repealdeal, R.id.ll_pay_returngoods, R.id.ll_pay_clear, R.id.ll_pay_signin, R.id.text_bank, R.id.text_store})
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.text_cancle:
				keyboard.dismiss();
				setFinishOnTouchOutside(true);
				initUi();
				break;
			case R.id.imageview_close:
				finish();
				break;
			case R.id.text_confirm:
				keyboard.dismiss();
				switch (type_function){
					case ConstantData.TRANS_SALE:
						doTrans();
						break;
					case ConstantData.TRANS_RETURN:
						doReturn();
						break;
					case ConstantData.TRANS_REVOKE:
						doRevoke();
						break;
				}
				break;
			case R.id.text_confirm_query:
				if (text_confirm_query.getText().toString().equals(getString(R.string.confirm))){
					//handleTransResult(true, dataString);
					text_confirm_query.setText(R.string.query);
				}else if(text_confirm_query.getText().toString().equals(getString(R.string.query))){
					if(isrunning){
						ToastUtils.sendtoastbyhandler(handler, "查询中,请稍候");
						return;
					}
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_querying);
					text_confirm_query.setText(R.string.confirm);
					doSearch();
				}
				break;
			case R.id.ll_pay_jiaoyi:
				setFinishOnTouchOutside(false);
				type_function = ConstantData.TRANS_SALE;
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.VISIBLE);
				edit_money.setHint("请输入支付金额");
				break;
			case R.id.ll_pay_signin:
				doSignin();
				break;
			case R.id.ll_pay_clear:
				doClear();
				break;
			case R.id.ll_pay_search:
				if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
					ll_function.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.VISIBLE);
					doChaXunYuE();
				}else if(Type.equals(ConstantData.YXLM_ID)){
					ll_function.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.VISIBLE);
					doChaXunYuE();
				}else{
					ToastUtils.sendtoastbyhandler(handler,"暂不支持");
				}
//				type_function = ConstantData.TRANS_QUERY;
//				ll_function.setVisibility(View.GONE);
//				ll_input_money.setVisibility(View.VISIBLE);
				break;
			case R.id.ll_pay_repealdeal:
				setFinishOnTouchOutside(false);
				type_function = ConstantData.TRANS_REVOKE;
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.VISIBLE);
				if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
					edit_money.setHint("请输入原交易凭证号");
				}else{
					edit_money.setHint("请输入撤销交易号");
				}
				break;
			case R.id.ll_pay_returngoods:
				setFinishOnTouchOutside(false);
				type_function = ConstantData.TRANS_RETURN;
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.VISIBLE);
				if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
					edit_money.setHint("请输入原交易参考号");
				}else{
					edit_money.setHint("请输入退货交易号");
				}
				break;
			case R.id.text_bank:
				bankType = "bank";
				ll_function.setVisibility(View.VISIBLE);
				ll_functions_type.setVisibility(View.GONE);
				text_title.setText(R.string.bank);
//				if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
//					ll_function.setVisibility(View.GONE);
//					ll_functions_clear.setVisibility(View.GONE);
//					ll_input_money.setVisibility(View.GONE);
//					ll_paying_status.setVisibility(View.VISIBLE);
//					spin_kit.setVisibility(View.VISIBLE);
//					text_status.setText(R.string.thirdpay_requesting);
//					JSONObject json = new JSONObject();
//					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.POS_TONG, ConstantData.YHK_JS, json);
//				}else{
//					ToastUtils.sendtoastbyhandler(handler, "暂不支持");
//				}
				break;
			case R.id.text_store:
				bankType = "store";
				ll_function.setVisibility(View.VISIBLE);
				ll_functions_type.setVisibility(View.GONE);
				text_title.setText(R.string.store);
//				if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
//					ll_function.setVisibility(View.GONE);
//					ll_functions_clear.setVisibility(View.GONE);
//					ll_input_money.setVisibility(View.GONE);
//					ll_paying_status.setVisibility(View.VISIBLE);
//					spin_kit.setVisibility(View.VISIBLE);
//					text_status.setText(R.string.thirdpay_requesting);
//					JSONObject json = new JSONObject();
//					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_JS, json);
//				}else{
//					ToastUtils.sendtoastbyhandler(handler,"暂不支持");
//				}
				break;
		}
	}

	private void doSignin() {
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			JSONObject json = new JSONObject();
			if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				if("bank".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.YHK_SK, ConstantData.YHK_SIGN, json);
				}else if("store".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.STORE, ConstantData.YHK_SIGN, json);
				}
			}else if(Type.equals(PaymentTypeEnum.WECHAT.getStyletype())){
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.POS_TONG, ConstantData.YHK_SIGN, json);
			}else if(Type.equals(ConstantData.YXLM_ID)){
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_SIGN, json);
			}
		}else {
			ToastUtils.sendtoastbyhandler(handler,"暂不支持");
		}

	}

	private void doClear() {
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){

			JSONObject json = new JSONObject();
			if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				if("bank".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.YHK_SK, ConstantData.YHK_JS, json);
				}else if("store".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.STORE, ConstantData.YHK_JS, json);
				}
			}else if(Type.equals(PaymentTypeEnum.WECHAT.getStyletype())){
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.POS_TONG, ConstantData.YHK_JS, json);
			}else if(Type.equals(ConstantData.YXLM_ID)){
				ll_function.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_JS, json);
			}
		}else{
			ToastUtils.sendtoastbyhandler(handler,"暂不支持");
		}
	}

	public void doSearch(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("old_trade_no", trade_no);
		map.put("pay_type", payMode);
		HttpRequestUtil.getinstance().thirdpayquery(map, ThirdPayQueryResult.class,
				new HttpActionHandle<ThirdPayQueryResult>() {

					@Override
					public void handleActionStart() {
						// TODO Auto-generated method stub
						isrunning = true;
					}

					@Override
					public void handleActionFinish() {
						isrunning = false;
					}

					@Override
					public void handleActionError(String actionName, final String errmsg) {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageview_close.setVisibility(View.VISIBLE);
								ll_paying_by_code.setVisibility(View.GONE);
								ll_paying_msg.setVisibility(View.VISIBLE);
								ll_input_money.setVisibility(View.GONE);
								ll_paying_status.setVisibility(View.GONE);
								text_paying_msg.setText(errmsg);
								text_confirm_query.setText(R.string.query);
							}
						});
					}

					@Override
					public void handleActionSuccess(String actionName, final ThirdPayQueryResult result) {
						// TODO Auto-generated method stub
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imageview_close.setVisibility(View.GONE);
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.VISIBLE);
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.GONE);
									text_confirm_query.setVisibility(View.GONE);
									text_paying_msg.setText("顾客已支付完成");
								}
							});
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									ThirdPayControllerDialog.this.finish();
								}
							}, 1000);
						} else if (ConstantData.HTTP_RESPONSE_THIRDPAY_WAIT.equals(result.getCode())) {
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imageview_close.setVisibility(View.GONE);
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.VISIBLE);
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.GONE);
									text_paying_msg.setText("需要查询");
									text_confirm_query.setText(R.string.query);
									ToastUtils.sendtoastbyhandler(handler, "需要查询");
								}
							});
						} else {
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imageview_close.setVisibility(View.VISIBLE);
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.VISIBLE);
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.GONE);
									text_paying_msg.setText(result.getMsg());
									text_confirm_query.setText(R.string.query);
								}
							});
						}
					}
				});
	}
	public void doReturn(){
		if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
			if(!MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				doRevoke();
				return;
			}
		}
		if(trade_no == null){
			if(edit_money.getText() == null || "".equals(edit_money.getText().toString())){
				if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
					ToastUtils.sendtoastbyhandler(handler, "请先原交易参考号");
				}else{
					ToastUtils.sendtoastbyhandler(handler, "请先输入订单号");
				}
				return;
			}else{
				trade_no = edit_money.getText().toString();
				edit_money.setText("");
				edit_money.setHint("请输入金额");
				return;
			}
		}else{
			if(edit_money.getText() == null || "".equals(edit_money.getText().toString())){
				ToastUtils.sendtoastbyhandler(handler, "请先输入金额");
				return;
			}else{
				if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
					if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
						JSONObject json = new JSONObject();
						String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
						try {
							json.put("amt",CurrencyUnit.yuan2fenStr(edit_money.getText().toString()));
							json.put("refNo",trade_no);
							json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
							json.put("extOrderNo",tradeNo);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.VISIBLE);
						spin_kit.setVisibility(View.VISIBLE);
						text_status.setText(R.string.thirdpay_requesting);
						if("bank".equals(bankType)){
							AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.YHK_SK, ConstantData.YHK_TH, json);
						}else if("store".equals(bankType)){
							AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.STORE, ConstantData.YHK_TH, json);
						}
					}else if(Type.equals(PaymentTypeEnum.WECHAT.getStyletype())){
						JSONObject json = new JSONObject();
						String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
						try {
							json.put("amt",CurrencyUnit.yuan2fenStr(edit_money.getText().toString()));
							json.put("refNo",trade_no);
							json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
							json.put("extOrderNo",tradeNo);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.VISIBLE);
						spin_kit.setVisibility(View.VISIBLE);
						text_status.setText(R.string.thirdpay_requesting);
						AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.POS_TONG, ConstantData.YHK_TH, json);
					}else if(Type.equals(ConstantData.YXLM_ID)){
						JSONObject json = new JSONObject();
						String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
						try {
							json.put("amt",CurrencyUnit.yuan2fenStr(edit_money.getText().toString()));
							json.put("refNo",trade_no);
							json.put("date",Utils.formatDate(new Date(System.currentTimeMillis()), "MMdd"));
							json.put("extOrderNo",tradeNo);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.VISIBLE);
						spin_kit.setVisibility(View.VISIBLE);
						text_status.setText(R.string.thirdpay_requesting);
						AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_TH, json);
					}
				}else{
					Map<String, String> map = new HashMap<String, String>();
					map.put("operater", SpSaveUtils.read(getApplicationContext(), ConstantData.CASHIER_CODE, ""));
					map.put("pay_type", payMode);
					map.put("old_trade_no", trade_no);
					map.put("billid", "-" + AppConfigFile.getBillId());
					map.put("total_fee", MoneyAccuracyUtils.thirdpaymoneydealbyinput(edit_money.getText().toString()));
					LogUtil.i("lgs", "old_trade_no===" + trade_no + "==billid==" + map.get("billid") + "===" + map.get("total_fee")
							+ "==" + map.get("pay_type"));
					HttpRequestUtil.getinstance().thirdpaysalesreturn(map, ThirdPaySalesReturnResult.class,
							new HttpActionHandle<ThirdPaySalesReturnResult>() {

								@Override
								public void handleActionStart() {
									ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											ll_input_money.setVisibility(View.GONE);
											ll_paying_status.setVisibility(View.VISIBLE);
											spin_kit.setVisibility(View.VISIBLE);
											text_status.setText(R.string.thirdpay_requesting);
										}
									});
								}

								@Override
								public void handleActionError(String actionName, String errmsg) {
									ToastUtils.sendtoastbyhandler(handler, errmsg);
									handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
								}

								@Override
								public void handleActionSuccess(String actionName, final ThirdPaySalesReturnResult result) {
									// TODO Auto-generated method stub
									if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
										ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												imageview_close.setVisibility(View.GONE);
												ll_paying_by_code.setVisibility(View.GONE);
												ll_paying_msg.setVisibility(View.VISIBLE);
												ll_input_money.setVisibility(View.GONE);
												ll_paying_status.setVisibility(View.GONE);
												text_confirm_query.setVisibility(View.GONE);
												text_paying_msg.setText("退货成功");
											}
										});
										handler.postDelayed(new Runnable() {
											@Override
											public void run() {
												ThirdPayControllerDialog.this.finish();
											}
										}, 1000);
									} else {
										ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
											@Override
											public void run() {
												imageview_close.setVisibility(View.GONE);
												ll_paying_by_code.setVisibility(View.GONE);
												ll_paying_msg.setVisibility(View.GONE);
												ll_input_money.setVisibility(View.GONE);
												ll_paying_status.setVisibility(View.VISIBLE);
												spin_kit.setVisibility(View.GONE);
												// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
												text_status.setText(result.getMsg());
												handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
											}
										});
									}
								}
							});
				}
			}
		}


//		SaleVoidRequest saleVoidRequest = new SaleVoidRequest(edit_money.getText().toString());
//		AidlRequestManager.getInstance().aidlSaleVoidRequest(mYunService,
//				saleVoidRequest, new AidlRequestManager.AidlRequestCallBack() {
//
//					@Override
//					public void onTaskStart() {
//						ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
//							@Override
//							public void run() {
//								ll_input_money.setVisibility(View.GONE);
//								ll_paying_status.setVisibility(View.VISIBLE);
//								spin_kit.setVisibility(View.VISIBLE);
//								text_status.setText(R.string.thirdpay_requesting);
//							}
//						});
//					}
//
//					@Override
//					public void onTaskFinish(JSONObject rspJSON) {
//						LogUtil.e("lgs", "onTaskFinish");
//						if (!rspJSON.optString("responseCode").equals("00")) {
//							ToastUtils.sendtoastbyhandler(handler, "调用交易失败：" + rspJSON.optString("errorMsg"));
//							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
//						}
//					}
//
//					@Override
//					public void onTaskCancelled() {
//						ToastUtils.sendtoastbyhandler(handler, "交易取消");
//						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
//					}
//
//					@Override
//					public void onException(Exception e) {
//						LogUtil.e("lgs", "onException");
//						ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
//						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
//					}
//				});
	}


	public void doRevoke(){
		if(edit_money.getText() == null || "".equals(edit_money.getText().toString())){
			ToastUtils.sendtoastbyhandler(handler, "输入不能为空");
			return;
		}
		if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				JSONObject json = new JSONObject();
				String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
				try {
					json.put("orgTraceNo",edit_money.getText().toString());
					json.put("extOrderNo", tradeNo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if("bank".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.YHK_SK, ConstantData.YHK_CX, json);
				}else if("store".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.STORE, ConstantData.YHK_CX, json);
				}
			}else{
				requestRefundCashier(edit_money.getText().toString());
			}
		}else if((Type.equals(PaymentTypeEnum.WECHAT.getStyletype()))){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				JSONObject json = new JSONObject();
				String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
				try {
					json.put("oldTraceNo",edit_money.getText().toString());
					json.put("extOrderNo", tradeNo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.POS_TONG, ConstantData.POS_XFCX, json);
			}else{
				Map<String, String> map = new HashMap<String, String>();
				map.put("operater", SpSaveUtils.read(getApplicationContext(), ConstantData.CASHIER_CODE, ""));
				map.put("pay_type", payMode);
				map.put("old_trade_no", edit_money.getText().toString());
				map.put("billid", "-" + AppConfigFile.getBillId());
				LogUtil.i("lgs", "old_trade_no===" + map.get("old_trade_no") + "==billid==" + map.get("billid") + "===" + "=="
						+ map.get("pay_type"));
				HttpRequestUtil.getinstance().thirdpaycancel(map, ThirdPayCancelResult.class,
						new HttpActionHandle<ThirdPayCancelResult>() {

							@Override
							public void handleActionStart() {
								ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										ll_input_money.setVisibility(View.GONE);
										ll_paying_status.setVisibility(View.VISIBLE);
										spin_kit.setVisibility(View.VISIBLE);
										text_status.setText(R.string.thirdpay_requesting);
									}
								});
							}

							@Override
							public void handleActionError(String actionName, String errmsg) {
								ToastUtils.sendtoastbyhandler(handler, errmsg);
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
							}

							@Override
							public void handleActionSuccess(String actionName, final ThirdPayCancelResult result) {
								// TODO Auto-generated method stub
								if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
									ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											imageview_close.setVisibility(View.GONE);
											ll_paying_by_code.setVisibility(View.GONE);
											ll_paying_msg.setVisibility(View.VISIBLE);
											ll_input_money.setVisibility(View.GONE);
											ll_paying_status.setVisibility(View.GONE);
											text_confirm_query.setVisibility(View.GONE);
											text_paying_msg.setText("撤销成功");
										}
									});
									handler.postDelayed(new Runnable() {
										@Override
										public void run() {
											ThirdPayControllerDialog.this.finish();
										}
									}, 1000);
								} else {
									ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											imageview_close.setVisibility(View.GONE);
											ll_paying_by_code.setVisibility(View.GONE);
											ll_paying_msg.setVisibility(View.GONE);
											ll_input_money.setVisibility(View.GONE);
											ll_paying_status.setVisibility(View.VISIBLE);
											spin_kit.setVisibility(View.GONE);
											// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
											text_status.setText(result.getMsg());
											handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
										}
									});
								}
							}
						});
			}
		}else if((Type.equals(ConstantData.YXLM_ID))){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				JSONObject json = new JSONObject();
				String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
				try {
					json.put("orgTraceNo",edit_money.getText().toString());
					json.put("extOrderNo", tradeNo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_CX, json);
			}else{
				ToastUtils.sendtoastbyhandler(handler,"暂不支持");
			}
		}
	}

	public void doTrans(){
		if(edit_money.getText() == null || "".equals(edit_money.getText().toString())){
			ToastUtils.sendtoastbyhandler(handler, "请先输入支付金额");
			return;
		}
		money = ArithDouble.parseDouble(edit_money.getText().toString());
		if (money <= 0) {
			ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_money_not_zero));
			return;
		}
		if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
				requestCashier(CurrencyUnit.yuan2fenStr(money + ""));
			}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				JSONObject json = new JSONObject();
				String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
				try {
					json.put("amt",CurrencyUnit.yuan2fenStr(money + ""));//TODO 金额格式
					json.put("extOrderNo",tradeNo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				if("bank".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.YHK_SK, ConstantData.YHK_XF, json);
				}else if("store".equals(bankType)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.STORE, ConstantData.YHK_XF, json);
				}
			}
		}else if(Type.equals(PaymentTypeEnum.WECHAT.getStyletype())){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				JSONObject json = new JSONObject();
				String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
				try {
					json.put("amt",CurrencyUnit.yuan2fenStr(money + ""));//TODO 金额格式
					json.put("extOrderNo",tradeNo);
					json.put("tradeType", "useScan");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.POS_TONG, ConstantData.POS_TONG_XF, json);
			}else{
				Intent intent_qr = new Intent(this, CaptureActivity.class);
				startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
			}
		}else if(Type.equals(ConstantData.YXLM_ID)){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				JSONObject json = new JSONObject();
				String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
				try {
					json.put("amt",CurrencyUnit.yuan2fenStr(money + ""));//TODO 金额格式
					json.put("extOrderNo",tradeNo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_XF, json);
			}else{
				Intent intent_qr = new Intent(this, CaptureActivity.class);
				startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
			}
		}
//		if("1".equals(SpSaveUtils.read(getApplicationContext(), ConstantData.MALL_ALIPAY_IS_INPUT, "0"))){
//			Intent intent_qr = new Intent(this, CaptureActivity.class);
//			startActivityForResult(intent_qr, ConstantData.QRCODE_REQURST_QR_PAY);
//		}else{
//			doPay();
//		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == ConstantData.QRCODE_RESULT_MEMBER_VERIFY) {
			switch (requestCode) {
				case ConstantData.QRCODE_REQURST_QR_PAY:
					if (!StringUtil.isEmpty(data.getExtras().getString("QRcode"))) {
						qrcode = data.getExtras().getString("QRcode");
						doPay();
					}
					break;
				default:
					break;
			}
		}else if(Activity.RESULT_OK == resultCode) {
			if (AppHelper.TRANS_REQUEST_CODE == requestCode) {
				if (null != data) {
					StringBuilder result = new StringBuilder();
					Map<String, String> map = AppHelper.filterTransResult(data);
					LogUtil.i("lgs",map.toString());
					String transId  = map.get("transId");
					if ("0".equals(map.get(AppHelper.RESULT_CODE))) {
						java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
						}.getType();
						try {
							Map<String, String> transData = GsonUtil.jsonToObect(map.get(AppHelper.TRANS_DATA), type);
							if ("00".equals(transData.get("resCode"))) {
								imageview_close.setVisibility(View.GONE);
								ll_paying_by_code.setVisibility(View.GONE);
								ll_paying_msg.setVisibility(View.VISIBLE);
								ll_input_money.setVisibility(View.GONE);
								ll_paying_status.setVisibility(View.GONE);
								text_confirm_query.setVisibility(View.GONE);
								if(transId.equals(ConstantData.YHK_XF)){
									text_paying_msg.setText("消费："+transData.get("amt")+"元成功");
								}else if(transId.equals(ConstantData.YHK_CX)){
									text_paying_msg.setText("撤销："+transData.get("amt")+"元成功");
								}else if(transId.equals(ConstantData.YHK_CXYE)){
									text_paying_msg.setText("余额为："+ArithDouble.div(ArithDouble.parseDouble(transData.get("amt")), 100));
								}else if(transId.equals(ConstantData.YHK_TH)){
									text_paying_msg.setText("退货退款："+transData.get("amt")+"元成功");
								}else if(transId.equals(ConstantData.YHK_SIGN)){
									text_paying_msg.setText("签到成功");
								}else if(transId.equals(ConstantData.YHK_JS)){
									text_paying_msg.setText("结算成功");
								}else if(transId.equals(ConstantData.POS_TONG)){
									text_paying_msg.setText("消费："+transData.get("amt")+"元成功");
								}else if(transId.equals(ConstantData.POS_XFCX)){
									text_paying_msg.setText("撤销："+transData.get("amt")+"元成功");
								}
								setFinishOnTouchOutside(true);
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
							} else {
								imageview_close.setVisibility(View.GONE);
								ll_paying_by_code.setVisibility(View.GONE);
								ll_paying_msg.setVisibility(View.GONE);
								ll_input_money.setVisibility(View.GONE);
								ll_paying_status.setVisibility(View.VISIBLE);
								spin_kit.setVisibility(View.GONE);
								// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
								text_status.setText(transId+":"+transData.get("resDesc"));
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
								ToastUtils.sendtoastbyhandler(handler, transData.get("resDesc"));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						String msg = "银行卡返回信息异常";
						if (!StringUtil.isEmpty(map.get(AppHelper.RESULT_MSG))) {
							msg = map.get(AppHelper.RESULT_MSG);
						}
						imageview_close.setVisibility(View.GONE);
						ll_paying_by_code.setVisibility(View.GONE);
						ll_paying_msg.setVisibility(View.GONE);
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.VISIBLE);
						spin_kit.setVisibility(View.GONE);
						// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
						text_status.setText(msg);
						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					}
				} else {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
					text_status.setText("银行卡支付异常！");
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					ToastUtils.sendtoastbyhandler(handler, "银行卡支付异常！");
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void doPay() {
		List<AidlPaymentInfo> paymentlist = (List<AidlPaymentInfo>)SpSaveUtils.getObject(ThirdPayControllerDialog.this,ConstantData.PAY_TYPE_LIST);
		AidlPaymentInfo paymentInfo;
		switch (PaymentTypeEnum.getpaymentstyle(Type)){
			case WECHAT:
				Map<String, String> map = new HashMap<String, String>();
				map.put("posip", Utils.getLocalIpAddress());
				map.put("pay_type", payMode + "");
				map.put("total_fee", MoneyAccuracyUtils.thirdpaymoneydealbyinput(money+""));
				map.put("billid", "-" + AppConfigFile.getBillId());
				map.put("auth_code", qrcode);
				LogUtil.i("lgs", map.get("pay_type") + "==" + map.get("total_fee") + "==" + map.get("billid") + "==="
						+ map.get("auth_code"));
				HttpRequestUtil.getinstance().thirdpay(map, ThirdPayResult.class, new HttpActionHandle<ThirdPayResult>(){

					@Override
					public void handleActionStart() {
						ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.VISIBLE);
									spin_kit.setVisibility(View.VISIBLE);
									text_status.setText(R.string.thirdpay_requesting);
								}
							});
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					}

					@Override
					public void handleActionSuccess(String actionName, final ThirdPayResult result) {
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imageview_close.setVisibility(View.GONE);
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.VISIBLE);
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.GONE);
									text_confirm_query.setVisibility(View.GONE);
									text_paying_msg.setText("顾客已支付完成");
								}
							});
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									ThirdPayControllerDialog.this.finish();
								}
							}, 1000);
						}else if (ConstantData.HTTP_RESPONSE_THIRDPAY_WAIT.equals(result.getCode())){
							trade_no = result.getThirdpay().getTrade_no();
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imageview_close.setVisibility(View.GONE);
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.VISIBLE);
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.GONE);
									text_paying_msg.setText("需要查询");
									text_confirm_query.setText(R.string.query);
									ToastUtils.sendtoastbyhandler(handler, "需要查询");
								}
							});
						}else{
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									imageview_close.setVisibility(View.GONE);
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.GONE);
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.VISIBLE);
									spin_kit.setVisibility(View.GONE);
									// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
									text_status.setText(result.getMsg());
									handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
								}
							});
						}
					}
				});
//				paymentInfo = getAidlPaymentInfoByName(paymentlist,getString(R.string.weichat));
//				if(paymentInfo != null && mYunService != null){
//					String transAmount = CurrencyUnit.yuan2fenStr(money+"");
//					SaleRequest request = new SaleRequest(paymentInfo
//							.getPaymentId(), transAmount, qrcode, "", "");// 可以自行传入订单号、订单描述
//					AidlRequestManager aidlManager = AidlRequestManager.getInstance();
//					aidlManager.aidlSaleRequest(mYunService, request, new AidlRequestManager.AidlRequestCallBack() {
//
//						@Override
//						public void onTaskStart() {
//							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
//								@Override
//								public void run() {
//									ll_input_money.setVisibility(View.GONE);
//									ll_paying_status.setVisibility(View.VISIBLE);
//									spin_kit.setVisibility(View.VISIBLE);
//									text_status.setText(R.string.thirdpay_requesting);
//								}
//							});
//							LogUtil.e("lgs","onTaskStart");
//						}
//
//						@Override
//						public void onTaskFinish(JSONObject rspJSON) {
//							// 如有dialog，此处终止
//							// dismissLoadingDialog();
//							LogUtil.e("lgs","onTaskFinish");
//							if (!rspJSON.optString("responseCode").equals("00")) {
//								ToastUtils.sendtoastbyhandler(handler, "调用交易失败：" + rspJSON.optString("errorMsg"));
//								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
//							}
//
//						}
//
//						@Override
//						public void onTaskCancelled() {
//							ToastUtils.sendtoastbyhandler(handler, "交易取消");
//							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
//							LogUtil.e("lgs","onTaskCancelled");
//							// dismissLoadingDialog();
//
//						}
//
//						@Override
//						public void onException(Exception e) {
//							LogUtil.e("lgs", "onException");
//							ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
//							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
//						}
//					});
//				}else {
//					ToastUtils.sendtoastbyhandler(handler, "暂不支持！");
//				}
				break;
			case ALIPAY:
				paymentInfo = getAidlPaymentInfoByName(paymentlist,getString(R.string.alipay));
				if(paymentInfo != null && mYunService != null){
					String transAmount = CurrencyUnit.yuan2fenStr(money+"");
					SaleRequest request = new SaleRequest(paymentInfo
							.getPaymentId(), transAmount, qrcode, "", "");// 可以自行传入订单号、订单描述
					AidlRequestManager aidlManager = AidlRequestManager.getInstance();
					aidlManager.aidlSaleRequest(mYunService, request, new AidlRequestManager.AidlRequestCallBack() {

						@Override
						public void onTaskStart() {
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.VISIBLE);
									spin_kit.setVisibility(View.VISIBLE);
									text_status.setText(R.string.thirdpay_requesting);
								}
							});
							LogUtil.e("lgs","onTaskStart");
						}

						@Override
						public void onTaskFinish(JSONObject rspJSON) {
							// 如有dialog，此处终止
							// dismissLoadingDialog();
							LogUtil.e("lgs","onTaskFinish");
							if (!rspJSON.optString("responseCode").equals("00")) {
								ToastUtils.sendtoastbyhandler(handler, "调用交易失败：" + rspJSON.optString("errorMsg"));
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
							}

						}

						@Override
						public void onTaskCancelled() {
							ToastUtils.sendtoastbyhandler(handler, "交易取消");
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
							LogUtil.e("lgs","onTaskCancelled");
							// dismissLoadingDialog();

						}

						@Override
						public void onException(Exception e) {
							// dismissLoadingDialog();
							LogUtil.e("lgs", "onException");
							ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
						}
					});
				}else {
					ToastUtils.sendtoastbyhandler(handler, "暂不支持！");
				}
				break;
			case BANK:
				paymentInfo = getAidlPaymentInfoBank(paymentlist);
				if(paymentInfo != null && mYunService != null){
					String transAmount = CurrencyUnit.yuan2fenStr(money+"");
					SaleRequest request = new SaleRequest(paymentInfo
							.getPaymentId(), transAmount, qrcode, "", "");// 可以自行传入订单号、订单描述
					AidlRequestManager aidlManager = AidlRequestManager.getInstance();
					aidlManager.aidlSaleRequest(mYunService, request, new AidlRequestManager.AidlRequestCallBack() {

						@Override
						public void onTaskStart() {
							ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ll_input_money.setVisibility(View.GONE);
									ll_paying_status.setVisibility(View.VISIBLE);
									spin_kit.setVisibility(View.VISIBLE);
									text_status.setText(R.string.thirdpay_requesting);
								}
							});
							LogUtil.e("lgs","onTaskStart");
						}

						@Override
						public void onTaskFinish(JSONObject rspJSON) {
							// 如有dialog，此处终止
							// dismissLoadingDialog();
							LogUtil.e("lgs","onTaskFinish");
							if (!rspJSON.optString("responseCode").equals("00")) {
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
								ToastUtils.sendtoastbyhandler(handler, "调用交易失败：" + rspJSON.optString("errorMsg"));
							}

						}

						@Override
						public void onTaskCancelled() {
							LogUtil.e("lgs","onTaskCancelled");
							ToastUtils.sendtoastbyhandler(handler, "交易取消");
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
							// dismissLoadingDialog();

						}

						@Override
						public void onException(Exception e) {
							// dismissLoadingDialog();
							LogUtil.e("lgs", "onException");
							ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
						}
					});
				}else {
					ToastUtils.sendtoastbyhandler(handler, "暂不支持！");
				}
				break;
		}
	}

	public AidlPaymentInfo getAidlPaymentInfoByName(List<AidlPaymentInfo> paymentlist, String name){
		if(name == null || name.equals("") || paymentlist == null){
			return null;
		}
		for(AidlPaymentInfo info:paymentlist){
			if(info.getPaymentName().contains(name))
				return info;
		}
		return null;
	}
	public AidlPaymentInfo getAidlPaymentInfoBank(List<AidlPaymentInfo> paymentlist){
		if(paymentlist == null)
			return null;
		for(AidlPaymentInfo info:paymentlist){
			if(info.getProductNo().equals("0079"))
				return info;
		}
		return null;
	}
	@Override
	public void onResume() {
		registerReceiver(broadcastReceiver, new IntentFilter(
				"cn.koolcloud.engine.ThirdPartyTrans"));
		super.onResume();
	}

	@Override
	public void onPause() {
		unregisterReceiver(broadcastReceiver);
		super.onPause();
	}

	private boolean isWaitSwip = false;
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// transProgressDialog.setCancelable(false);
			final Message message = intent.getParcelableExtra(Message.class
					.getName());
			LogUtil.d("lgs", "handleMessage" + message.what + ":" + message.toString());
			String dataString = "";
			if (message.getData() != null) {
				dataString = message.getData().getString("data");
				// statusTV.append(dataString+"\n");
				try {
					LogUtil.d("lgs", "data:" + dataString);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			switch (message.what) {
				case TransState.State_Waiting_Reverse: {
					// 存在冲正-冲正中
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_reverseing);
					break;
				}
				case TransState.State_Reverse_Success: {
					// 冲正成功
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_reverse_success);
					LogUtil.i("lgs", "-------------冲正成功-------------");
					// 冲正成功后该笔交易失效，需要重新手动发起交易（此处需要UI提示）
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					break;
				}
				case TransState.State_Reverse_Failed: {
					// 冲正失败
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_reverse_failed);
					LogUtil.i("lgs", "-------------冲正失败-------------");
					// 冲正失败后该笔交易失效，需要重新手动发起交易（此处需要UI提示）
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					break;
				}

				case TransState.State_Waiting_Sign: {
					// 签到中
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_signing);
					break;
				}
				case TransState.State_Finish_Sign: {
					// 签到完成
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					try {
						JSONObject object = new JSONObject(dataString);
						if (object.optString("responseCode").equals("00")) {
							text_status.setText(R.string.thirdpay_sign_success);
						} else {
							text_status.setText(R.string.thirdpay_sign_failed);
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				}
				case TransState.State_Waiting_Swipe: {
					isWaitSwip = true;
					imageview_close.setVisibility(View.VISIBLE);
					// 等待刷卡
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_swip_card);
					break;
				}
				case TransState.State_Finish_Swipe: {
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					// 刷卡完成
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_swip_card);
					break;
				}
				case TransState.State_Waiting_Pinpad: {
					// 等待输密
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					timer.cancel();
					timerTask.cancel();
					times = 60;
					timer = new Timer();
					timer.schedule(timerTask, 0, 1000);
					break;
				}
				case TransState.State_Finish_Pinpad: {
					// 输密结束
					imageview_close.setVisibility(View.GONE);
					timer.cancel();
					timerTask.cancel();
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_input_pwd_done);
					break;
				}
				case TransState.State_Waiting_Trans: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_transing);
					break;
				}
				case TransState.State_Neeed_Query: {
					// 需要查询
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.VISIBLE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.GONE);
					text_confirm_query.setText(R.string.query);
					ToastUtils.sendtoastbyhandler(handler, "需要查询");
					break;
				}
				case TransState.State_Pinpad_Input: {
					// 键盘输入信息（已输入密码长度）
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					try {
						JSONObject object = new JSONObject(dataString);
						String pinLength = object.optString("pinLength");
						int length = Integer.valueOf(pinLength);
						if (length == 0) {
							text_status.setText("密码已清空，重新输入:");
						} else {
							StringBuilder builder = new StringBuilder();
							for (int i = 0; i < length; i++) {
								builder.append(" * ");
							}
							text_status.setText("已输入 " + length + " 位密码 ："
									+ builder.toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				}
				case TransState.State_Catch_DOWN_TRANS: {
					// 是否降级交易
					// TODO 应用层需要处理选择
					imageview_close.setVisibility(View.GONE);
					try {
						mYunService.stopDownTrade();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					ToastUtils.sendtoastbyhandler(handler,"暂不支持降级交易！");
					ThirdPayControllerDialog.this.finish();
//					statusTV.append("\n遇到降级交易询问");
//					AlertDialog.Builder builder = new AlertDialog.Builder(
//							AidlPosActivity.this);
//					builder.setTitle("提示");
//					builder.setMessage("请检查卡片是否是IC卡（卡正面有芯片）\n有芯片请插入芯片\n无芯片请继续交易");
//					builder.setPositiveButton("继续交易",
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//													int which) {
//									try {
//										mYunService.continueDownTrade();
//									} catch (RemoteException e) {
//										e.printStackTrace();
//									}
//									dialog.dismiss();
//								}
//							});
//					builder.setNegativeButton("重新刷卡",
//							new DialogInterface.OnClickListener() {
//
//								@Override
//								public void onClick(DialogInterface dialog,
//													int which) {
//									try {
//										mYunService.stopDownTrade();
//									} catch (RemoteException e) {
//										e.printStackTrace();
//									}
//									dialog.dismiss();
//								}
//							});
//					builder.create().show();

					break;
				}
				case TransState.State_Abord_Trans: {
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
					if (dataString != null && !dataString.isEmpty()) {
						try {
							JSONObject dataJson = new JSONObject(dataString);
							text_status.setText(getString(R.string.thirdpay_trans_failed) + dataJson.optString("resultMsg"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						text_status.setText(getString(R.string.thirdpay_trans_failed) + "数据异常");
					}
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					break;
				}
				case TransState.State_Finish_Trans: {
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.VISIBLE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.GONE);
					text_confirm_query.setVisibility(View.GONE);
					text_paying_msg.setText("银行卡交易成功");
					Message msg = Message.obtain();
					msg.what = TRANS_FINISH;
					msg.obj = dataString;
					handler.sendMessageDelayed(msg, 2000);
					// 银行卡交易完成（成功）
					break;
				}
				case TransState.STATE_NO_CARD_TRANS_START: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_transing);
					break;
				}
				case TransState.STATE_CASH_FINISH: {
					imageview_close.setVisibility(View.GONE);
					break;
				}
				case TransState.STATE_CASH_FAIL: {
					imageview_close.setVisibility(View.GONE);
					break;
				}
				case TransState.STATE_BSC_FINISH: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.VISIBLE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.GONE);
					text_confirm_query.setVisibility(View.GONE);
					text_paying_msg.setText("支付完成");
					Message msg = Message.obtain();
					msg.what = TRANS_FINISH;
					msg.obj = dataString;
					handler.sendMessageDelayed(msg, 2000);
					break;
				}
				case TransState.STATE_BSC_FAIL: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_transing);
					if (dataString != null && !dataString.isEmpty()) {
						try {
							JSONObject dataJson = new JSONObject(dataString);
							text_status.setText("扫码支付失败" + dataJson.optString("resultMsg"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						text_status.setText("扫码支付失败, 数据异常");
					}
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					break;
				}
				case TransState.STATE_CSB_GET_CODE: {
					// TODO 二维码一分钟内有效，一分钟之后自动消失，如果客户手机显示已完成支付，但未收到支付完成通知，请一分钟后查询此交易
					imageview_close.setVisibility(View.VISIBLE);
					ll_paying_by_code.setVisibility(View.VISIBLE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.GONE);
					JSONObject jsonResponse;
					String qrcode = "";
					try {
						jsonResponse = new JSONObject(dataString);
						qrcode = jsonResponse.optString("eWalletQrCodeUrl");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Bitmap qrCodeBitmap = CodeBitmap.createQrBitmap(
							ThirdPayControllerDialog.this, qrcode,
							image_paying_code.getWidth(),
							image_paying_code.getHeight());
					image_paying_code.setImageBitmap(qrCodeBitmap);
					// mDialog = new QrcodeDialog();
					// mDialog.setCancelable(false);
					//
					// Bundle bundle = new Bundle();
					// bundle.putString("qrCode", qrcode);
					// mDialog.setArguments(bundle);
					// mDialog.show(getFragmentManager(), null);

					LogUtil.e("lgs", "message.what is :" + message.what);
					break;
				}
				case TransState.STATE_CSB_LOSE_CODE: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					try {
						JSONObject dataJson = new JSONObject(dataString);
						text_status.setText("获取二维码失败" + dataJson.optString("resultMsg"));
					} catch (JSONException e) {
						e.printStackTrace();
					}

					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					break;
				}
				case TransState.STATE_CSB_FINISH: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.VISIBLE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.GONE);
					text_confirm_query.setVisibility(View.GONE);
					text_paying_msg.setText("顾客已支付完成");
					Message msg = Message.obtain();
					msg.what = TRANS_FINISH;
					msg.obj = dataString;
					handler.sendMessageDelayed(msg, 2000);
					break;
				}
				case TransState.STATE_CSB_FAILURE: {
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_transing);
					if (dataString != null && !dataString.isEmpty()) {
						try {
							JSONObject dataJson = new JSONObject(dataString);
							text_status.setText("扫码支付失败" + dataJson.optString("resultMsg"));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						text_status.setText("扫码支付失败, 数据异常");
					}
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					break;
				}
				case TransState.STATE_NO_CARD_VOID_FINISH: {
					if (dataString != null && !dataString.isEmpty()) {
						imageview_close.setVisibility(View.GONE);
						ll_paying_by_code.setVisibility(View.GONE);
						ll_paying_msg.setVisibility(View.VISIBLE);
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.GONE);
						text_confirm_query.setVisibility(View.GONE);
						JSONObject dataJson = null;
						try {
							dataJson = new JSONObject(dataString);
							String code  = dataJson.optString("resultCode");
							if(code == null || "".equals(code) || !"00".equals(code)){
								text_paying_msg.setText("撤销失败" + dataJson.optString("resultMsg"));
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						text_paying_msg.setText("撤销成功");
						Message msg = Message.obtain();
						msg.what = TRANS_FINISH;
						msg.obj = dataString;
						handler.sendMessageDelayed(msg, 2000);
					} else {
						imageview_close.setVisibility(View.GONE);
						ll_paying_by_code.setVisibility(View.GONE);
						ll_paying_msg.setVisibility(View.VISIBLE);
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.GONE);
						text_paying_msg.setText("交易撤销结果处理异常");
						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					}

					break;
				}

				default:
					break;
			}

		}
	};

	/**
	 * 处理交易结果
	 *
	 * @param isSuccess
	 * @param data
	*/

	private void handleTransResult(boolean isSuccess, String data) {
		if (isSuccess) {
			AidlRequestManager.getInstance().aidlLastTransPrintRequest(mYunService, new AidlRequestManager.AidlRequestCallBack() {

				@Override
				public void onTaskStart() {
				}

				@Override
				public void onTaskFinish(JSONObject rspJSON) {
					if (!rspJSON.optString("responseCode").equals("00")) {
						ToastUtils.sendtoastbyhandler(handler, "打印失败：" + rspJSON.optString("errorMsg"));
					}
				}

				@Override
				public void onTaskCancelled() {
				}

				@Override
				public void onException(Exception e) {
					ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
				}
			});
		}
		// 待嫁接
		if (isSuccess) {
			OrderBean resultBean = parse(data, isSuccess);
			LogUtil.e("lgs", resultBean.toString());
			if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
				SpSaveUtils.write(getApplicationContext(),ConstantData.LAST_BANK_TRANS, resultBean.getTxnId());
			}
			if (resultBean.getTransType().equals(ConstantData.SALE)) {
				resultBean.setPaymentId(getPayTypeId(Type));
				resultBean.setTransType(ConstantData.TRANS_SALE);
				resultBean.setTraceId("-" + AppConfigFile.getBillId());
				Intent serviceintent = new Intent(mContext, RunTimeService.class);
				serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
				serviceintent.putExtra(ConstantData.THIRD_DATA, resultBean);
				startService(serviceintent);
				finish();
			} else if (resultBean.getTransType().equals(ConstantData.SALE_VOID)) {
				if (resultBean.getResultCode().equals("00")) {
					resultBean.setPaymentId(getPayTypeId(Type));
					resultBean.setTransType(ConstantData.TRANS_REVOKE);
					resultBean.setTraceId("-"+AppConfigFile.getBillId());
					Intent serviceintent = new Intent(mContext, RunTimeService.class);
					serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
					serviceintent.putExtra(ConstantData.THIRD_DATA, resultBean);
					startService(serviceintent);
					finish();
				}else{
					LogUtil.e("lgs", "退款失败"+data);
					finish();
				}
			} else {
				LogUtil.e("lgs", data);
				finish();
				return;
			}
		}
	}

	/**
	 * 解析交易结果
	 *
	 * @param jsonStr
	 * @param isSuccess
	 * @return
	*/

	private OrderBean parse(String jsonStr, boolean isSuccess) {
		OrderBean newBean = null;
		try {
			newBean = (OrderBean) GsonUtil.jsonToBean(jsonStr, OrderBean.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (newBean.getOrderState().isEmpty()) {
			newBean.setOrderState(isSuccess ? "0" : "");
		}
		return newBean;
	}


	/**
	 * @author zmm emial:mingming.zhang@symboltech.com 2015年11月17日
	 * @Description: 获取支付方式Id
	 *
	 */
	private String getPayTypeId(String typeEnum) {

		List<PayMentsInfo> paymentslist = (List<PayMentsInfo>) SpSaveUtils.getObject(mContext, ConstantData.PAYMENTSLIST);
		if(paymentslist == null)
			return null;
		for (int i = 0; i < paymentslist.size(); i++) {
			if (paymentslist.get(i).getType().equals(typeEnum)) {
				return paymentslist.get(i).getId();
			}
		}
		return null;
	}

	private BizServiceInvoker mBizServiceInvoker;
	// 1.执行调用之前需要调用WeiposImpl.as().init()方法，保证sdk初始化成功。
	//
	// 2.调用收银支付成功后，收银支付结果页面完成后，BizServiceInvoker.OnResponseListener后收到响应的结果
	//
	// 3.如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
	private void innerRequestCashier(String total_fee) {
		ll_input_money.setVisibility(View.GONE);
		ll_paying_status.setVisibility(View.VISIBLE);
		spin_kit.setVisibility(View.VISIBLE);
		text_status.setText(R.string.thirdpay_requesting);
		// 1001 现金
		// 1003 微信
		// 1004 支付宝
		// 1005 百度钱包
		// 1006 银行卡
		// 1007 易付宝
		// 1009 京东钱包
		// 1011 QQ钱包
		String pay_type = "1006";
		String channel = "POS";//标明是pos调用，不需改变
		String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
		// String total_fee = "1";//支付金额，单位为分，1=0.01元，100=1元，不可空
		// 如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
		// 如果不需要回调页面，则backPkgName和backClassPath需要同时设置为空字符串 ："";
		String backPkgName = null;//，可空
		String backClassPath = null;//，可空
		//指定接收收银结果的url地址默认为："http://apps.weipass.cn/pay/notify"，可填写自己服务器接收地址
		String notifyUrl = null;//，可空
		String body = SpSaveUtils.read(MyApplication.context, ConstantData.SHOP_NAME, "")+"店铺商品";//订单body描述信息 ，不可空
		String attach = "备注信息";//备注信息，可空，订单信息原样返回，可空
		// 第三方订单流水号，非空,发起请求，tradeNo不能相同，相同在收银会提示有存在订单
		String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
		try {
			RequestInvoke cashierReq = new RequestInvoke();
			cashierReq.pkgName = this.getPackageName();
			cashierReq.sdCode = CashierSign.Cashier_sdCode;// 收银服务的sdcode信息
			cashierReq.bpId = AppConfigFile.InvokeCashier_BPID;
			cashierReq.launchType = CashierSign.launchType;

			cashierReq.params = CashierSign.sign(AppConfigFile.InvokeCashier_BPID, AppConfigFile.InvokeCashier_KEY, channel,
					pay_type, tradeNo, body, attach, total_fee, backPkgName, backClassPath, notifyUrl);
			cashierReq.seqNo = seqNo;

			RequestResult r = mBizServiceInvoker.request(cashierReq);
			LogUtil.i("lgs", r.token + "," + r.seqNo + "," + r.result);
			// 发送调用请求
			if (r != null) {
				LogUtil.i("lgs", "request result:" + r.result + "|launchType:" + cashierReq.launchType);
				String err = null;
				switch (r.result) {
					case BizServiceInvoker.REQ_SUCCESS: {
						// 调用成功
						//ToastUtils.sendtoastbyhandler(handler, "收银服务调用成功");
						break;
					}
					case BizServiceInvoker.REQ_ERR_INVAILD_PARAM: {
						ToastUtils.sendtoastbyhandler(handler, "请求参数错误！");
						break;
					}
					case BizServiceInvoker.REQ_ERR_NO_BP: {
						ToastUtils.sendtoastbyhandler(handler, "未知的合作伙伴！");
						break;
					}
					case BizServiceInvoker.REQ_ERR_NO_SERVICE: {
						//调用结果返回，没有订阅对应bp账号中的收银服务，则去调用sdk主动订阅收银服务
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								ToastUtils.sendtoastbyhandler(handler, "正在申请订阅收银服务...");
								// 如果没有订阅，则主动请求订阅服务
								mBizServiceInvoker.subscribeService(CashierSign.Cashier_sdCode,
										AppConfigFile.InvokeCashier_BPID);
							}
						});
						break;
					}
					case BizServiceInvoker.REQ_NONE: {
						ToastUtils.sendtoastbyhandler(handler, "请求未知错误！");
						break;
					}
				}
				if (err != null) {
					LogUtil.i("lgs", "serviceInvoker request err:" + err);
				}
			}else{
				ToastUtils.sendtoastbyhandler(handler, "请求结果对象为空！");
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这个是服务调用完成后的响应监听方法
	 */
	private BizServiceInvoker.OnResponseListener mOnResponseListener = new BizServiceInvoker.OnResponseListener() {

		@Override
		public void onResponse(String sdCode, String token, byte[] data) {
			// 收银服务调用完成后的返回方法
			String result = new String(data);
			WposPayInfo info = null;
			try {
				info = GsonUtil.jsonToBean(result, WposPayInfo.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			LogUtil.i("lgs",
					"sdCode = " + sdCode + " , token = " + token + " , data = " + new String(data));
			if(info != null){
				if(info.getErrCode().equals("0")){
					if(!"1001".equals(info.getPay_type())){
						if("PAY".equals(info.getTrade_status())){
							imageview_close.setVisibility(View.GONE);
							ll_paying_by_code.setVisibility(View.GONE);
							ll_paying_msg.setVisibility(View.VISIBLE);
							ll_input_money.setVisibility(View.GONE);
							ll_paying_status.setVisibility(View.GONE);
							text_confirm_query.setVisibility(View.GONE);
							text_paying_msg.setText("顾客已支付完成");
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									ThirdPayControllerDialog.this.finish();
								}
							}, 1000);
						}else{
							imageview_close.setVisibility(View.GONE);
							ll_paying_by_code.setVisibility(View.GONE);
							ll_paying_msg.setVisibility(View.GONE);
							ll_input_money.setVisibility(View.GONE);
							ll_paying_status.setVisibility(View.VISIBLE);
							spin_kit.setVisibility(View.GONE);
							// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
							text_status.setText(info.getPay_info());
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
							ToastUtils.sendtoastbyhandler(handler, info.getPay_info());
						}
					}else{
						imageview_close.setVisibility(View.GONE);
						ll_paying_by_code.setVisibility(View.GONE);
						ll_paying_msg.setVisibility(View.GONE);
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.VISIBLE);
						spin_kit.setVisibility(View.GONE);
						// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
						text_status.setText("使用了现金交易，该交易不记账，小票无效");
						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
						ToastUtils.sendtoastbyhandler(handler, "使用了现金交易，该交易不记账，小票无效");
					}
				}else{
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
					text_status.setText(info.getErrMsg());
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					ToastUtils.sendtoastbyhandler(handler, info.getErrMsg());
				}
			}else{
				imageview_close.setVisibility(View.GONE);
				ll_paying_by_code.setVisibility(View.GONE);
				ll_paying_msg.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.GONE);
				// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
				text_status.setText("未知错误");
				handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
				ToastUtils.sendtoastbyhandler(handler, "未知错误");
			}
		}

		@Override
		public void onFinishSubscribeService(boolean result, String err) {
			// TODO Auto-generated method stub
			// 申请订阅收银服务结果返回
			// bp订阅收银服务返回结果
			if (!result) {
				//订阅失败
				ToastUtils.sendtoastbyhandler(handler, err);
			}else{
				//订阅成功
				ToastUtils.sendtoastbyhandler(handler, "订阅收银服务成功，请按home键回调主页刷新订阅数据后重新进入调用收银");
			}
		}
	};

	/**
	 * 本地调用收银服务
	 */
	private void requestCashier(String money) {
		try {
			// 初始化服务调用
			mBizServiceInvoker = WeiposImpl.as().getService(BizServiceInvoker.class);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (mBizServiceInvoker == null) {
			ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
			return;
		}
		// 设置请求订阅服务监听结果的回调方法
		mBizServiceInvoker.setOnResponseListener(mOnResponseListener);
		innerRequestCashier(money);
	}

	/**
	 * 本地调用收银服务
	 */
	private void requestRefundCashier(String tradeNo) {
		try {
			// 初始化服务调用
			mBizServiceInvoker = WeiposImpl.as().getService(BizServiceInvoker.class);
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (mBizServiceInvoker == null) {
			ToastUtils.sendtoastbyhandler(handler, "初始化服务调用失败");
			return;
		}
		// 设置请求订阅服务监听结果的回调方法
		mBizServiceInvoker.setOnResponseListener(mOnRefundResponseListener);
		innerRefundRequestCashier(tradeNo);
	}
	private void innerRefundRequestCashier(String tradeNo) {
		String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
		ll_input_money.setVisibility(View.GONE);
		ll_paying_status.setVisibility(View.VISIBLE);
		spin_kit.setVisibility(View.VISIBLE);
		text_status.setText(R.string.thirdpay_requesting);
		try {
			RequestInvoke cashierReq = new RequestInvoke();
			cashierReq.pkgName = this.getPackageName();
			cashierReq.sdCode = CashierSign.Cashier_sdCode;// 收银服务的sdcode信息
			cashierReq.bpId = AppConfigFile.InvokeCashier_BPID;
			cashierReq.launchType = CashierSign.launchType;

			cashierReq.params = CashierSign.refundsign(AppConfigFile.InvokeCashier_BPID, AppConfigFile.InvokeCashier_KEY, tradeNo);
			cashierReq.seqNo = seqNo;

			RequestResult r = mBizServiceInvoker.request(cashierReq);
			LogUtil.i("lgs", r.token + "," + r.seqNo + "," + r.result);
			// 发送调用请求
			if (r != null) {
				LogUtil.i("lgs", "request result:" + r.result + "|launchType:" + cashierReq.launchType);
				String err = null;
				switch (r.result) {
					case BizServiceInvoker.REQ_SUCCESS: {
						// 调用成功
						//ToastUtils.sendtoastbyhandler(handler, "收银服务调用成功");
						break;
					}
					case BizServiceInvoker.REQ_ERR_INVAILD_PARAM: {
						ToastUtils.sendtoastbyhandler(handler, "请求参数错误！");
						break;
					}
					case BizServiceInvoker.REQ_ERR_NO_BP: {
						ToastUtils.sendtoastbyhandler(handler, "未知的合作伙伴！");
						break;
					}
					case BizServiceInvoker.REQ_ERR_NO_SERVICE: {
						//调用结果返回，没有订阅对应bp账号中的收银服务，则去调用sdk主动订阅收银服务
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								ToastUtils.sendtoastbyhandler(handler, "正在申请订阅收银服务...");
								// 如果没有订阅，则主动请求订阅服务
								mBizServiceInvoker.subscribeService(CashierSign.Cashier_sdCode,
										AppConfigFile.InvokeCashier_BPID);
							}
						});
						break;
					}
					case BizServiceInvoker.REQ_NONE: {
						ToastUtils.sendtoastbyhandler(handler, "请求未知错误！");
						break;
					}
				}
				if (err != null) {
					LogUtil.i("lgs", "serviceInvoker request err:" + err);
				}
			}else{
				ToastUtils.sendtoastbyhandler(handler, "请求结果对象为空！");
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 这个是服务调用完成后的响应监听方法
	 */
	private BizServiceInvoker.OnResponseListener mOnRefundResponseListener = new BizServiceInvoker.OnResponseListener() {

		@Override
		public void onResponse(String sdCode, String token, byte[] data) {
			// 收银服务调用完成后的返回方法
			String result = new String(data);
			WposBankRefundInfo info = null;
			try {
				info = GsonUtil.jsonToBean(result, WposBankRefundInfo.class);
			} catch (Exception e) {
				e.printStackTrace();
			}
			LogUtil.i("lgs",
					"sdCode = " + sdCode + " , token = " + token + " , data = " + new String(data));
			if(info != null){
				if(info.getErrCode().equals("0")){
					ThirdPayControllerDialog.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							imageview_close.setVisibility(View.GONE);
							ll_paying_by_code.setVisibility(View.GONE);
							ll_paying_msg.setVisibility(View.VISIBLE);
							ll_input_money.setVisibility(View.GONE);
							ll_paying_status.setVisibility(View.GONE);
							text_confirm_query.setVisibility(View.GONE);
							text_paying_msg.setText("退货成功");
						}
					});
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
				}else{
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
					text_status.setText(info.getErrMsg());
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
					ToastUtils.sendtoastbyhandler(handler, info.getErrMsg());
				}
			}else{
				imageview_close.setVisibility(View.GONE);
				ll_paying_by_code.setVisibility(View.GONE);
				ll_paying_msg.setVisibility(View.GONE);
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.GONE);
				text_status.setText("未知错误");
				handler.sendEmptyMessageDelayed(TRANS_AGAIN, 2000);
				ToastUtils.sendtoastbyhandler(handler, "未知错误");
			}
		}

		@Override
		public void onFinishSubscribeService(boolean result, String err) {
			// TODO Auto-generated method stub
			// 申请订阅收银服务结果返回
			// bp订阅收银服务返回结果
			if (!result) {
				//订阅失败
				ToastUtils.sendtoastbyhandler(handler, err);
			}else{
				//订阅成功
				ToastUtils.sendtoastbyhandler(handler, "订阅收银服务成功，请按home键回调主页刷新订阅数据后重新进入调用收银");
			}
		}
	};

	private void initPosCore() {
		if (pCore == null) {
			// 配置参数表
			HashMap<String, String> ps = new HashMap<>();
			// 核心服务包名
			ps.put(PosConfig.Name_EX + "1100", "cn.weipass.cashier");
			// 核心服务类名
			ps.put(PosConfig.Name_EX + "1101", "com.wangpos.cashiercoreapp.services.CoreAppService");
			{
//				// TODO 这组参数仅用于开发阶段;
//				// 对于POS已经配置好通道的情况下, 不需要配置这些参数;
//				// 这里配置的是一个测试通道,消费不会扣钱,请输密123456;
//				// 配置插件包名
//				ps.put(PosConfig.Name_EX + "1080", "com.wangpos.pos.plugin.unipayplugindemo");
//				// 配置插件启动类
//				ps.put(PosConfig.Name_EX + "1081", ".IBankSerivce4CoreApp");
//				// 配置商户号,为了防止与别人相同,这里请自己修改成一个不同的值
//				ps.put(PosConfig.Name_MerchantNo, "831551148160897");
//				// 配置终端号,为了防止与别人相同,这里请自己修改成一个不同的值
//				ps.put(PosConfig.Name_TerminalNo, "00000459");
//				// 配置主密钥明文
//				ps.put(PosConfig.Name_MainKey, "267CC46D293D4C89FE7C0B73CB388937");
//				// 测试通道服务器
//				ps.put(PosConfig.Name_Server, "115.28.46.12:50505");
//				// 配置通道ID
//				ps.put(PosConfig.Name_EX + "1052", "10001");
//				// 商户名
//				ps.put(PosConfig.Name_MerchantName, "coreApp");
					}
					pCore = PosCoreFactory.newInstance(this, ps);
				}
			}

			/**
			 * 查询余额
			 */
		private void doChaXunYuE() {
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				ll_input_money.setVisibility(View.GONE);
				ll_paying_status.setVisibility(View.VISIBLE);
				spin_kit.setVisibility(View.VISIBLE);
				text_status.setText(R.string.thirdpay_requesting);
				JSONObject json = new JSONObject();
				if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
					if("bank".equals(bankType)){
						AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.YHK_SK, ConstantData.YHK_CXYE, json);
					}else if("store".equals(bankType)){
						AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.STORE, ConstantData.YHK_CXYE, json);
					}
				}else if(Type.equals(ConstantData.YXLM_ID)){
					AppHelper.callTrans(ThirdPayControllerDialog.this, ConstantData.QMH, ConstantData.YHK_CXYE, json);
				}
			}else if (MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
				new Thread() {
					public void run() {
						try {
							PosCore.RChaXunYuE rChaXunYuE = pCore.chaXunYuE(null);
							ToastUtils.sendtoastbyhandler(handler, "余额为:" + rChaXunYuE.amountStr);
						} catch (Exception e) {
							ToastUtils.sendtoastbyhandler(handler, "查询余额:" + e.getLocalizedMessage());
						e.printStackTrace();
					}
				}
			}.start();
		}else if (MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
				ToastUtils.sendtoastbyhandler(handler, "暂不支持");
		}
	}
}
