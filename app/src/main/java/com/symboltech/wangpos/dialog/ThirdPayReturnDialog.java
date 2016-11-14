package com.symboltech.wangpos.dialog;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.symboltech.koolcloud.aidl.AidlRequestManager;
import com.symboltech.koolcloud.interfaces.RemoteServiceStateChangeListerner;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.BaseActivity;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.thirdparty.aidl.IKuYunThirdPartyService;
import cn.koolcloud.engine.thirdparty.aidlbean.LoginRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.SaleVoidRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;

public class ThirdPayReturnDialog extends BaseActivity{
	@Bind(R.id.ll_result)
	LinearLayout ll_result;
	@Bind(R.id.text_result)
	TextView text_result;
	@Bind(R.id.text_confirm)
	TextView text_confirm;

	@Bind(R.id.text_title)
	TextView text_title;
	@Bind(R.id.imageview_close)
	ImageView imageview_close;

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
				JSONObject rspJsonObject = new JSONObject(rspString);
				if (rspJsonObject.optString("responseCode").equals("00")) {
					SpSaveUtils.write(ThirdPayReturnDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
					SpSaveUtils.write(ThirdPayReturnDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
					SpSaveUtils.write(ThirdPayReturnDialog.this, ConstantData.PWD, "111111");
					returnMoney();
					return;
				}else{
					login();
				}
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
	private void login() {
		LoginRequest request = new LoginRequest(SpSaveUtils.read(ThirdPayReturnDialog.this, ConstantData.CUSTOMERID,""), SpSaveUtils.read(ThirdPayReturnDialog.this, ConstantData.USERID,""), SpSaveUtils.read(ThirdPayReturnDialog.this, ConstantData.PWD,""));
		AidlRequestManager aidlManager = AidlRequestManager.getInstance();
		aidlManager.aidlLoginRequest(mYunService, request,
				new AidlRequestManager.AidlRequestCallBack() {

					@Override
					public void onTaskStart() {
						ThirdPayReturnDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ll_result.setVisibility(View.GONE);
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
							SpSaveUtils.write(ThirdPayReturnDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
							SpSaveUtils.write(ThirdPayReturnDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
							SpSaveUtils.write(ThirdPayReturnDialog.this, ConstantData.PWD, "111111");
							returnMoney();
							return;
						} else {
							ToastUtils.sendtoastbyhandler(handler, rspJsonObject.optString("errorMsg"));
							ThirdPayReturnDialog.this.finish();
						}
					}

					@Override
					public void onTaskCancelled() {
						ThirdPayReturnDialog.this.finish();
					}

					@Override
					public void onException(Exception e) {
						ToastUtils.sendtoastbyhandler(handler, e.toString());
						ThirdPayReturnDialog.this.finish();
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
	public static final int  TRANS_FINISH= 2;
	public static final int  TRANS_FAILED= 3;
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
					ToastUtils.showtaostbyhandler(theActivity, msg);
					break;
				case TRANS_AGAIN:
					((ThirdPayReturnDialog)theActivity).initUi();
					break;
				case TRANS_FINISH:
					((ThirdPayReturnDialog) theActivity).handleTransResult(true, (String) msg.obj);
					break;
				case TRANS_FAILED:
					theActivity.finish();
					break;
			}
		}
	}

	MyHandler handler = new MyHandler(this);

	public void initUi(){
		imageview_close.setVisibility(View.VISIBLE);
		ll_result.setVisibility(View.VISIBLE);
		ll_paying_status.setVisibility(View.GONE);
		text_result.setText(R.string.repeat);
	}

	private Double money;
	private String Type;
	private String txnId;
	@Override
	protected void initData() {
		this.money = getIntent().getDoubleExtra(ConstantData.PAY_MONEY, 0);
		this.Type = getIntent().getStringExtra(ConstantData.PAY_TYPE);
		this.txnId = getIntent().getStringExtra(ConstantData.PAY_ID);
		switch (PaymentTypeEnum.getpaymentstyle(Type)){
			case WECHAT:
				text_title.setText(getString(R.string.weichat)+"退款"+money);
				break;
			case ALIPAY:
				text_title.setText(getString(R.string.alipay)+"退款"+money);
				break;
			case BANK:
				text_title.setText(getString(R.string.bank)+"退款"+money);
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
					ThirdPayReturnDialog.this);
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
			LogUtil.i("lgs","服务已绑定");
		}
	};

	public void setServiceStateChangeListerner(
			RemoteServiceStateChangeListerner serviceStateChangeListerner) {
		this.serviceStateChangeListerner = serviceStateChangeListerner;
	}
	@Override
	protected void initView() {
		setContentView(R.layout.dialog_third_pay_return);
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

	@OnClick({R.id.text_cancle, R.id.text_confirm, R.id.imageview_close})
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		switch (id){
			case R.id.text_cancle:
			case R.id.imageview_close:
				finish();
				break;
			case R.id.text_confirm:
				if(!text_confirm.getText().equals(getString(R.string.query)) && mYunService != null){
					returnMoney();
				}
				break;
		}
	}

	public void returnMoney(){
		SaleVoidRequest saleVoidRequest = new SaleVoidRequest(txnId);
		AidlRequestManager.getInstance().aidlSaleVoidRequest(mYunService,
				saleVoidRequest, new AidlRequestManager.AidlRequestCallBack() {

					@Override
					public void onTaskStart() {
						ThirdPayReturnDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageview_close.setVisibility(View.GONE);
								ll_result.setVisibility(View.GONE);
								ll_paying_status.setVisibility(View.VISIBLE);
								text_status.setText(R.string.returning_pay);
							}
						});
					}

					@Override
					public void onTaskFinish(JSONObject rspJSON) {
						if (!rspJSON.optString("responseCode").equals("00")) {
							ThirdPayReturnDialog.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									text_result.setText(R.string.returning_pay_failed);
									ll_result.setVisibility(View.VISIBLE);
									ll_paying_status.setVisibility(View.GONE);
								}
							});
						}
					}

					@Override
					public void onTaskCancelled() {
						ThirdPayReturnDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageview_close.setVisibility(View.VISIBLE);
								text_result.setText(R.string.returning_pay_failed);
								ll_result.setVisibility(View.VISIBLE);
								ll_paying_status.setVisibility(View.GONE);
							}
						});
					}

					@Override
					public void onException(Exception e) {
						ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
						ThirdPayReturnDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								imageview_close.setVisibility(View.VISIBLE);
								text_result.setText(R.string.returning_pay_failed);
								ll_result.setVisibility(View.VISIBLE);
								ll_paying_status.setVisibility(View.GONE);
							}
						});
					}
				});
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
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_reverseing);
					break;
				}
				case TransState.State_Reverse_Success: {
					// 冲正成功
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
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
					ll_result.setVisibility(View.GONE);
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
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_signing);
					break;
				}
				case TransState.State_Finish_Sign: {
					// 签到完成
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
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
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_swip_card);
					break;
				}
				case TransState.State_Finish_Swipe: {
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					// 刷卡完成
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_swip_card);
					break;
				}
				case TransState.State_Waiting_Pinpad: {
					// 等待输密
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
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
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.GONE);
					text_status.setText(R.string.thirdpay_input_pwd_done);
					break;
				}
				case TransState.State_Waiting_Trans: {
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_transing);
					break;
				}
				case TransState.State_Neeed_Query: {
					// 需要查询
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.VISIBLE);
					ll_paying_status.setVisibility(View.GONE);
					text_confirm.setText(R.string.query);
					text_result.setText(R.string.query);
					ToastUtils.sendtoastbyhandler(handler, "需要查询");
					break;
				}
				case TransState.State_Pinpad_Input: {
					// 键盘输入信息（已输入密码长度）
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
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
					ToastUtils.sendtoastbyhandler(handler, "暂不支持降级交易！");
					handler.sendEmptyMessageDelayed(TRANS_AGAIN, 500);
					break;
				}
				case TransState.State_Abord_Trans: {
					isWaitSwip = false;
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
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
					ll_result.setVisibility(View.GONE);
					spin_kit.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					Message msg = Message.obtain();
					msg.what = TRANS_FINISH;
					msg.obj = dataString;
					text_status.setText("交易完成");
					handler.sendMessageDelayed(msg, 2000);
					// 银行卡交易完成（成功）
					break;
				}
				case TransState.STATE_NO_CARD_TRANS_START: {
					imageview_close.setVisibility(View.GONE);
					ll_result.setVisibility(View.GONE);
					ll_paying_status.setVisibility(View.VISIBLE);
					spin_kit.setVisibility(View.VISIBLE);
					text_status.setText(R.string.thirdpay_transing);
					break;
				}
				case TransState.STATE_CASH_FINISH:
				case TransState.STATE_CASH_FAIL:
				case TransState.STATE_BSC_FINISH:
				case TransState.STATE_BSC_FAIL:
				case TransState.STATE_CSB_FINISH:
				case TransState.STATE_CSB_FAILURE:
					break;
				case TransState.STATE_NO_CARD_VOID_FINISH: {
					if (dataString != null && !dataString.isEmpty()) {
						imageview_close.setVisibility(View.GONE);
						ll_result.setVisibility(View.GONE);
						spin_kit.setVisibility(View.GONE);
						ll_paying_status.setVisibility(View.VISIBLE);
						try {
							JSONObject dataJson = new JSONObject(dataString);
							String code  = dataJson.optString("resultCode");
							if(code == null || "".equals(code) || !"00".equals(code)){
								text_status.setText("撤销失败" + dataJson.optString("resultMsg"));
								handler.sendEmptyMessageDelayed(TRANS_FAILED, 2000);
								return;
							}
							Message msg = Message.obtain();
							msg.what = TRANS_FINISH;
							msg.obj = dataString;
							text_status.setText("撤销完成");
							handler.sendMessageDelayed(msg, 2000);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						imageview_close.setVisibility(View.VISIBLE);
						ll_result.setVisibility(View.VISIBLE);
						ll_paying_status.setVisibility(View.GONE);
						text_result.setText("交易撤销结果处理异常");
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

	public void handleTransResult(boolean isSuccess, String data) {
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
			LogUtil.e("lgs", resultBean.toString());
			if(Type.equals(PaymentTypeEnum.BANK.getStyletype())){
				SpSaveUtils.write(getApplicationContext(),ConstantData.LAST_BANK_TRANS, resultBean.getTxnId());
			}
			Intent intent = new Intent();
			if (resultBean.getTransType().equals(ConstantData.SALE_VOID)) {
				if (resultBean.getResultCode().equals("00")) {
					resultBean.setOrderState("0");
					intent.putExtra(ConstantData.ORDER_BEAN, resultBean);
					setResult(ConstantData.THRID_CANCLE_RESULT_CODE, intent);
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
		if(newBean != null){
			if (newBean.getOrderState().isEmpty()) {
				newBean.setOrderState(isSuccess ? "0" : "");
			}
		}
//		if (newBean.getPaymentName().isEmpty()) {
//			newBean.setPaymentName(DevDemoSPEdit.getInstance(this)
//					.getPaymentName(newBean.getPaymentId()));
//		}
		// newBean.setSuccess(isSuccess);
		return newBean;
	}
}
