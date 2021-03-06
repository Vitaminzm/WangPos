package com.symboltech.wangpos.dialog;


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
import com.symboltech.koolcloud.aidl.AidlRequestManager;
import com.symboltech.koolcloud.interfaces.RemoteServiceStateChangeListerner;
import com.symboltech.koolcloud.transmodel.AidlPaymentInfo;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.BaseActivity;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.CodeBitmap;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.thirdparty.aidl.IKuYunThirdPartyService;
import cn.koolcloud.engine.thirdparty.aidlbean.LoginRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.SaleRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;

public class ThirdPayDialog extends BaseActivity{
	@Bind(R.id.ll_input_money)
	LinearLayout ll_input_money;
	@Bind(R.id.edit_money)
	EditText edit_money;
	@Bind(R.id.text_cancle)
	TextView text_cancle;
	@Bind(R.id.text_confirm)
	TextView text_confirm;

	@Bind(R.id.ll_paying_by_code)
	LinearLayout ll_paying_by_code;
	@Bind(R.id.text_title)
	TextView text_title;
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


	@Bind(R.id.ll_paying_status)
	LinearLayout ll_paying_status;
	@Bind(R.id.text_status)
	TextView text_status;
	@Bind(R.id.spin_kit)
	SpinKitView spin_kit;

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
							SpSaveUtils.write(ThirdPayDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
							SpSaveUtils.write(ThirdPayDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
							SpSaveUtils.write(ThirdPayDialog.this, ConstantData.PWD, "111111");
							JSONArray payments = null;
							try {
								String rspStr = mYunService.getPaymentList();
								JSONObject rspJSON = new JSONObject(rspStr);
								payments = rspJSON.optJSONArray("paymentlList");
								LogUtil.i("lgs", payments.toString());
								SpSaveUtils.saveObject(ThirdPayDialog.this, ConstantData.PAY_TYPE_LIST, GsonUtil.jsonToArrayList(payments.toString(), AidlPaymentInfo.class));
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
			} catch (Exception e1) {
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
	private String dataString;
	private String qrcode;

	private void login() {
		LoginRequest request = new LoginRequest(SpSaveUtils.read(ThirdPayDialog.this, ConstantData.CUSTOMERID,""), SpSaveUtils.read(ThirdPayDialog.this, ConstantData.USERID,""), SpSaveUtils.read(ThirdPayDialog.this, ConstantData.PWD,""));
		AidlRequestManager aidlManager = AidlRequestManager.getInstance();
		aidlManager.aidlLoginRequest(mYunService, request,
				new AidlRequestManager.AidlRequestCallBack() {

					@Override
					public void onTaskStart() {
						ThirdPayDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ll_paying_by_code.setVisibility(View.GONE);
								ll_paying_msg.setVisibility(View.GONE);
								ll_input_money.setVisibility(View.GONE);
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
							SpSaveUtils.write(ThirdPayDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
							SpSaveUtils.write(ThirdPayDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
							SpSaveUtils.write(ThirdPayDialog.this, ConstantData.PWD, "111111");
							JSONArray payments = null;
							try {
								String rspStr = mYunService.getPaymentList();
								JSONObject rspJSON = new JSONObject(rspStr);
								payments = rspJSON.optJSONArray("paymentlList");
								LogUtil.i("lgs",payments.toString());
								SpSaveUtils.saveObject(ThirdPayDialog.this, ConstantData.PAY_TYPE_LIST, GsonUtil.jsonToArrayList(payments.toString(), AidlPaymentInfo.class));
							} catch (RemoteException e) {
								e.printStackTrace();
							} catch (JSONException e) {
								e.printStackTrace();
							}
							ThirdPayDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ll_paying_by_code.setVisibility(View.GONE);
									ll_paying_msg.setVisibility(View.GONE);
									ll_input_money.setVisibility(View.VISIBLE);
									edit_money.setFocusable(true);
									ll_paying_status.setVisibility(View.GONE);
								}
							});
							return;
						} else {
							ToastUtils.sendtoastbyhandler(handler, rspJsonObject.optString("errorMsg"));
							ThirdPayDialog.this.finish();
						}
					}

					@Override
					public void onTaskCancelled() {
						ThirdPayDialog.this.finish();
					}

					@Override
					public void onException(Exception e) {
						ToastUtils.sendtoastbyhandler(handler, e.toString());
						ThirdPayDialog.this.finish();
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
	public static final int  TRANS_CANCLE= 2;
	public static final int  TRANS_FINISH= 3;
	static class MyHandler extends Handler {
		WeakReference<BaseActivity> mActivity;

		MyHandler(BaseActivity activity) {
			mActivity = new WeakReference<BaseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseActivity theActivity = mActivity.get();
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(theActivity,msg);
					break;
				case TRANS_AGAIN:
					((ThirdPayDialog)theActivity).initUi();
					break;
				case TRANS_CANCLE:
					theActivity.finish();
					break;
				case TRANS_FINISH:
					((ThirdPayDialog)theActivity).handleTransResult(true, (String) msg.obj);
					break;
			}
		}
	}

	MyHandler handler = new MyHandler(this);

	public void initUi(){
		ll_paying_by_code.setVisibility(View.GONE);
		ll_paying_msg.setVisibility(View.GONE);
		ll_input_money.setVisibility(View.VISIBLE);
		edit_money.setFocusable(true);
		ll_paying_status.setVisibility(View.GONE);
	}

	private Double money;
	private String Type;
	@Override
	protected void initData() {
		this.money = getIntent().getDoubleExtra(ConstantData.PAY_MONEY, 0);
		this.Type = getIntent().getStringExtra(ConstantData.PAY_TYPE);
		this.qrcode = getIntent().getStringExtra(ConstantData.BSC);
		if(qrcode == null){
			qrcode = "";
		}
		edit_money.setText(money + "");
		switch (PaymentTypeEnum.getpaymentstyle(Type)){
			case WECHAT:
				text_title.setText(R.string.weichat);
				break;
			case ALIPAY:
				text_title.setText(R.string.alipay);
				break;
			case BANK:
				text_title.setText(R.string.bank);
				break;
		}
		Intent yunIntent = new Intent(IKuYunThirdPartyService.class.getName());
		yunIntent = AndroidUtils.getExplicitIntent(this, yunIntent);
		setServiceStateChangeListerner(serviceStateChangeListerner1);
		if (yunIntent == null) {
		} else {
			bindService(yunIntent, connection, Context.BIND_AUTO_CREATE);
		}
	}

	private RemoteServiceStateChangeListerner serviceStateChangeListerner1 = new RemoteServiceStateChangeListerner() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ThirdPayDialog.this);
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
		setContentView(R.layout.dialog_third_pay);
		setFinishOnTouchOutside(false);
		ButterKnife.bind(this);
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

	@OnClick({R.id.text_confirm_query, R.id.text_cancle, R.id.text_confirm, R.id.imageview_close})
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.text_cancle:
				finish();
				break;
			case R.id.imageview_close:
				finish();
				break;
			case R.id.text_confirm:
				List<AidlPaymentInfo> paymentlist = (List<AidlPaymentInfo>)SpSaveUtils.getObject(ThirdPayDialog.this,ConstantData.PAY_TYPE_LIST);
				if(paymentlist == null || paymentlist.size()<= 0){
					LogUtil.i("lgs","------------------");
					return;
				}
				AidlPaymentInfo paymentInfo;
				switch (PaymentTypeEnum.getpaymentstyle(Type)){
					case WECHAT:
						 paymentInfo = getAidlPaymentInfoByName(paymentlist,getString(R.string.weichat));
						if(paymentInfo != null && mYunService != null){
							String transAmount = CurrencyUnit.yuan2fenStr(money+"");
							SaleRequest request = new SaleRequest(paymentInfo
									.getPaymentId(), transAmount, qrcode, "", "");// 可以自行传入订单号、订单描述
							AidlRequestManager aidlManager = AidlRequestManager.getInstance();
							aidlManager.aidlSaleRequest(mYunService, request, new AidlRequestManager.AidlRequestCallBack() {

								@Override
								public void onTaskStart() {
									ThirdPayDialog.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											imageview_close.setVisibility(View.GONE);
											ll_paying_by_code.setVisibility(View.GONE);
											ll_paying_msg.setVisibility(View.GONE);
											ll_input_money.setVisibility(View.GONE);
											ll_paying_status.setVisibility(View.VISIBLE);
											spin_kit.setVisibility(View.VISIBLE);
											text_status.setText(R.string.thirdpay_requesting);
										}
									});
									LogUtil.i("lgs", "onTaskStart");
								}

								@Override
								public void onTaskFinish(JSONObject rspJSON) {
									// 如有dialog，此处终止
									// dismissLoadingDialog();
									LogUtil.i("lgs", "onTaskFinish");
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
									LogUtil.e("lgs", "onException");
									ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
									handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
								}
							});
						}else {
							ToastUtils.sendtoastbyhandler(handler, "暂不支持！");
						}
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
									ThirdPayDialog.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											imageview_close.setVisibility(View.GONE);
											ll_paying_by_code.setVisibility(View.GONE);
											ll_paying_msg.setVisibility(View.GONE);
											ll_input_money.setVisibility(View.GONE);
											ll_paying_status.setVisibility(View.VISIBLE);
											spin_kit.setVisibility(View.VISIBLE);
											text_status.setText(R.string.thirdpay_requesting);
										}
									});
								}

								@Override
								public void onTaskFinish(JSONObject rspJSON) {
									// 如有dialog，此处终止
									// dismissLoadingDialog();
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
									ThirdPayDialog.this.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											imageview_close.setVisibility(View.GONE);
											ll_paying_by_code.setVisibility(View.GONE);
											ll_paying_msg.setVisibility(View.GONE);
											ll_input_money.setVisibility(View.GONE);
											ll_paying_status.setVisibility(View.VISIBLE);
											spin_kit.setVisibility(View.VISIBLE);
											text_status.setText(R.string.thirdpay_requesting);
										}
									});
								}

								@Override
								public void onTaskFinish(JSONObject rspJSON) {
									// 如有dialog，此处终止
									// dismissLoadingDialog();
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
				break;
			case R.id.text_confirm_query:
				if (text_confirm_query.getText().toString().equals(getString(R.string.confirm))){
					handleTransResult(true, dataString);
					text_confirm_query.setText(R.string.query);
				}else if(text_confirm_query.getText().toString().equals(getString(R.string.query))){
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_querying);

					text_confirm_query.setText(R.string.confirm);
				}
				break;
		}
	}

	public AidlPaymentInfo getAidlPaymentInfoByName(List<AidlPaymentInfo> paymentlist, String name){
		if(name == null || name.equals("")){
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
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.GONE);
					ll_input_money.setVisibility(View.GONE);
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
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 3000);
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
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 3000);
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
							handler.sendEmptyMessageDelayed(TRANS_AGAIN, 3000);
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
					text_confirm_query.setVisibility(View.VISIBLE);
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
					ThirdPayDialog.this.finish();
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
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 3000);
					break;
				}
				case TransState.State_Finish_Trans: {
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					ll_paying_by_code.setVisibility(View.GONE);
					ll_paying_msg.setVisibility(View.VISIBLE);
					ll_input_money.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.GONE);
					text_paying_msg.setText("银行卡交易成功");
					text_confirm_query.setVisibility(View.GONE);
					Message msg = Message.obtain();
					msg.what = TRANS_FINISH;
					msg.obj = dataString;
					handler.sendMessageDelayed(msg, 3000);
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
					handler.sendMessageDelayed(msg, 3000);
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
					handler.sendEmptyMessageDelayed(TRANS_CANCLE, 3000);
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
							ThirdPayDialog.this, qrcode,
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

					handler.sendEmptyMessageDelayed(TRANS_CANCLE, 3000);
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
					handler.sendMessageDelayed(msg, 3000);
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
					handler.sendEmptyMessageDelayed(TRANS_CANCLE, 3000);
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
								handler.sendEmptyMessageDelayed(TRANS_AGAIN, 3000);
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						text_paying_msg.setText("撤销成功");
						Message msg = Message.obtain();
						msg.what = TRANS_FINISH;
						msg.obj = dataString;
						handler.sendMessageDelayed(msg, 3000);
					} else {
						imageview_close.setVisibility(View.GONE);
						ll_paying_by_code.setVisibility(View.GONE);
						ll_paying_msg.setVisibility(View.VISIBLE);
						ll_input_money.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.GONE);
						text_confirm_query.setVisibility(View.GONE);
						text_paying_msg.setText("交易撤销结果处理异常");
						handler.sendEmptyMessageDelayed(TRANS_AGAIN, 3000);
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
		if (isSuccess && Type.equals(PaymentTypeEnum.BANK.getStyletype())) {
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
			LogUtil.i("lgs", resultBean.toString());
			if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
				SpSaveUtils.write(getApplicationContext(),ConstantData.LAST_BANK_TRANS, resultBean.getTxnId());
			}
			Intent intent = new Intent();
			if (resultBean.getTransType().equals(ConstantData.SALE)) {
				intent.putExtra(ConstantData.ORDER_BEAN, resultBean);
				setResult(ConstantData.THRID_PAY_RESULT_CODE, intent);
				finish();
			} else if (resultBean.getTransType().equals(ConstantData.SALE_VOID)) {
				if (resultBean.getResultCode().equals("00")) {
					resultBean.setOrderState("0");
					intent.putExtra(ConstantData.ORDER_BEAN, resultBean);
					setResult(ConstantData.THRID_PAY_RESULT_CODE, intent);
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
}
