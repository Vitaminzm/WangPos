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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.symboltech.koolcloud.aidl.AidlRequestManager;
import com.symboltech.koolcloud.interfaces.RemoteServiceStateChangeListerner;
import com.symboltech.koolcloud.transmodel.OrderBean;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.activity.BaseActivity;
import com.symboltech.wangpos.adapter.CanclePayAdapter;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.http.GsonUtil;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.log.OperateLog;
import com.symboltech.wangpos.msg.entity.PayMentsCancleInfo;
import com.symboltech.wangpos.msg.entity.WposBankRefundInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.result.ThirdPayCancelResult;
import com.symboltech.wangpos.service.RunTimeService;
import com.symboltech.wangpos.utils.AndroidUtils;
import com.symboltech.wangpos.utils.ArithDouble;
import com.symboltech.wangpos.utils.CashierSign;
import com.symboltech.wangpos.utils.CurrencyUnit;
import com.symboltech.wangpos.utils.MoneyAccuracyUtils;
import com.symboltech.wangpos.utils.OptLogEnum;
import com.symboltech.wangpos.utils.PaymentTypeEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;
import com.symboltech.wangpos.utils.StringUtil;
import com.symboltech.wangpos.utils.ToastUtils;
import com.symboltech.wangpos.utils.Utils;
import com.ums.AppHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.thirdparty.aidl.IKuYunThirdPartyService;
import cn.koolcloud.engine.thirdparty.aidlbean.LoginRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.SaleVoidRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransState;
import cn.weipass.pos.sdk.BizServiceInvoker;
import cn.weipass.pos.sdk.impl.WeiposImpl;
import cn.weipass.service.bizInvoke.RequestInvoke;
import cn.weipass.service.bizInvoke.RequestResult;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class CanclePayDialog extends BaseActivity{
	@Bind(R.id.imageview_close)
	ImageView imageview_close;
	@Bind(R.id.text_cancle_pay)
	TextView text_cancle_pay;
	@Bind(R.id.text_submit_order)
	TextView text_submit_order;
	@Bind(R.id.text_money)
	TextView text_money;
	@Bind(R.id.text_info)
	TextView text_info;
	@Bind(R.id.listview_canclepay)
	ListView listview_canclepay;

	private List<PayMentsCancleInfo> payments;
	private CanclePayAdapter canclePayAdapter;

	public int isCancleCount = 0;
	protected IKuYunThirdPartyService mYunService;
	protected RemoteServiceStateChangeListerner serviceStateChangeListerner = null;
	private boolean isLogin = false;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mYunService = IKuYunThirdPartyService.Stub.asInterface(service);
			if (serviceStateChangeListerner != null) {
				serviceStateChangeListerner.onServiceConnected(name, service);
			}
			try {
				String rspString = mYunService.isLogin();
				JSONObject rspJsonObject = new JSONObject(rspString);
				if (rspJsonObject.optString("responseCode").equals("00")) {
					isLogin = true;
					return;
				} else {
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
		LoginRequest request = new LoginRequest(SpSaveUtils.read(CanclePayDialog.this, ConstantData.CUSTOMERID,""), SpSaveUtils.read(CanclePayDialog.this, ConstantData.USERID,""), SpSaveUtils.read(CanclePayDialog.this, ConstantData.PWD,""));
		AidlRequestManager aidlManager = AidlRequestManager.getInstance();
		aidlManager.aidlLoginRequest(mYunService, request,
				new AidlRequestManager.AidlRequestCallBack() {

					@Override
					public void onTaskStart() {
					}

					@Override
					public void onTaskFinish(JSONObject rspJsonObject) {
						if (rspJsonObject.optString("responseCode").equals(
								"00")) {
							SpSaveUtils.write(CanclePayDialog.this, ConstantData.CUSTOMERID, rspJsonObject.optString("customerId"));
							SpSaveUtils.write(CanclePayDialog.this, ConstantData.USERID, rspJsonObject.optString("userId"));
							SpSaveUtils.write(CanclePayDialog.this, ConstantData.PWD, "111111");
							isLogin = true;
							return;
						} else {
							ToastUtils.sendtoastbyhandler(handler, rspJsonObject.optString("errorMsg"));
						}
					}

					@Override
					public void onTaskCancelled() {
						CanclePayDialog.this.finish();
					}

					@Override
					public void onException(Exception e) {
						ToastUtils.sendtoastbyhandler(handler, e.toString());
						CanclePayDialog.this.finish();
					}
				});
	}
	/** refresh UI By handler */
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ToastUtils.TOAST_WHAT:
				ToastUtils.showtaostbyhandler(CanclePayDialog.this, msg);
				break;
			default:
				break;
			}
		};
	};
	//撤销位置
	private int position;
	@Override
	protected void initData() {
		payments = (List<PayMentsCancleInfo>) getIntent().getSerializableExtra(ConstantData.CANCLE_LIST);
		double totalMoney = 0;
		double cash = 0;
		canclePayAdapter = new CanclePayAdapter(payments, CanclePayDialog.this);
		listview_canclepay.setAdapter(canclePayAdapter);
		if(payments!=null){
			for(int i=0;i<payments.size();i++){
				if(!payments.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())){
					if(payments.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype())
							|| payments.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype())
							||payments.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype())
							||payments.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype())){
						cash = ArithDouble.add(cash, ArithDouble.parseDouble(payments.get(i).getMoney()));
					}
					totalMoney = ArithDouble.add(totalMoney, ArithDouble.parseDouble(payments.get(i).getMoney()));
				}else{
					cash = ArithDouble.sub(cash, ArithDouble.parseDouble(payments.get(i).getMoney()));
					totalMoney = ArithDouble.sub(totalMoney, ArithDouble.parseDouble(payments.get(i).getMoney()));
				}
			}
		}
		text_money.setText(totalMoney + "");
		if(cash > 0)
			text_info.setText("(现金类 "+cash+")元");
		if(totalMoney > cash){
			if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
				requestCashier();
			}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
				Intent yunIntent = new Intent(IKuYunThirdPartyService.class.getName());
				yunIntent = AndroidUtils.getExplicitIntent(this, yunIntent);
				setServiceStateChangeListerner(serviceStateChangeListerner1);
				if (yunIntent == null) {
				} else {
					bindService(yunIntent, connection, Context.BIND_AUTO_CREATE);
				}
			}
		}
	}

	private RemoteServiceStateChangeListerner serviceStateChangeListerner1 = new RemoteServiceStateChangeListerner() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					CanclePayDialog.this);
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
		setContentView(R.layout.dialog_cancle_pay);
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
		if(mYunService != null){
			try {
				unbindService(connection);
			} catch (Exception e) {
				// 如果重复解绑会抛异常
			}
		}
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

	@OnClick({R.id.imageview_close, R.id.text_cancle_pay, R.id.text_submit_order})
	public void onClick(View v) {
		if(Utils.isFastClick()){
			return;
		}
		int id = v.getId();
		Intent intent = new Intent();
		switch (id){
			case R.id.imageview_close:
			case R.id.text_cancle_pay:
				if(isCancleCount != 0){
					ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_cancle_failed));
					return;
				}
				intent.putExtra(ConstantData.CANCLE_LIST, (Serializable)payments);
				setResult(ConstantData.THRID_CANCLE_RESULT_CODE, intent);
				finish();
				break;
			case R.id.text_submit_order:
				if(isCancleCount != 0){
					ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_msg_pay_cancle_failed));
					return;
				}
				for(int i=0;i<payments.size();i++){
					if( payments.get(i).getType().equals(PaymentTypeEnum.HANDRECORDED.getStyletype()) ||
							payments.get(i).getType().equals(PaymentTypeEnum.ALIPAYRECORDED.getStyletype()) ||payments.get(i).getType().equals(PaymentTypeEnum.WECHATRECORDED.getStyletype()) ||
							payments.get(i).getType().equals(PaymentTypeEnum.CASH.getStyletype()) ||payments.get(i).getType().equals(PaymentTypeEnum.LING.getStyletype())){
						payments.get(i).setIsCancle(true);
					}
				}
				intent.putExtra(ConstantData.CANCLE_LIST, (Serializable)payments);
				setResult(ConstantData.THRID_CANCLE_RESULT_CODE, intent);
				finish();
				break;
		}
	}

	/**
	 * 支付撤销
	 *
	 * @author zmm Email:mingming.zhang@symboltech.com 2015年11月14日
	 * @Description:
	 */
	private void thirdcancle(final int position) {
		if(isCancleCount > 0){
			ToastUtils.sendtoastbyhandler(handler, "撤销中，请稍后再试");
			return;
		}
		//LogUtil.i("lgs", "-----"+position);
		isCancleCount++;
		final PayMentsCancleInfo info = payments.get(position);
		Map<String, String> map = new HashMap<String, String>();
		if(info.getId().equals(ConstantData.ALPAY_ID)) {
			map.put("pay_type", ConstantData.PAYMODE_BY_ALIPAY+"");
		}else if(info.getId().equals(ConstantData.WECHAT_ID)){
			map.put("pay_type", ConstantData.PAYMODE_BY_WEIXIN+"");
		}
		map.put("total_fee", MoneyAccuracyUtils.thirdpaymoneydealbyinput(info.getMoney()));
		map.put("old_trade_no", info.getThridPay().getTrade_no());
		map.put("billid", AppConfigFile.getBillId());
		HttpRequestUtil.getinstance().thirdpaycancel(map, ThirdPayCancelResult.class,
				new HttpActionHandle<ThirdPayCancelResult>() {

					@Override
					public void handleActionStart() {
						info.setDes(getString(R.string.cancleing_pay));
						canclePayAdapter.notifyDataSetChanged();
					}

					@Override
					public void handleActionFinish() {
						isCancleCount--;
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						ToastUtils.sendtoastbyhandler(handler, errmsg);
						CanclePayDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								info.setDes(getString(R.string.cancled_failed));
								canclePayAdapter.notifyDataSetChanged();
							}
						});
					}

					@Override
					public void handleActionSuccess(String actionName, final ThirdPayCancelResult result) {
						CanclePayDialog.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
									OperateLog.getInstance().saveLog2File(OptLogEnum.PAY_CANCLE_PAY_SUCCESS.getOptLogCode(), getString(R.string.pay_cancle_pay_success));
									info.setIsCancle(true);
									info.setDes(getString(R.string.cancled_pay));
									canclePayAdapter.notifyDataSetChanged();
								} else {
									OperateLog.getInstance().saveLog2File(OptLogEnum.PAY_CANCLE_PAY_FAILED.getOptLogCode(), getString(R.string.pay_cancle_pay_failed));
									info.setDes(getString(R.string.cancled_failed));
									canclePayAdapter.notifyDataSetChanged();
								}
							}
						});
					}

					@Override
					public void handleActionOffLine() {
						// TODO Auto-generated method stub
						ToastUtils.sendtoastbyhandler(handler, getString(R.string.offline_waring));
					}

					@Override
					public void handleActionChangeToOffLine() {
						// TODO Auto-generated method stub

					}
				});
	}

	public void saleVoid(int position){
		final PayMentsCancleInfo info = payments.get(position);
		if(info.getType().equals(PaymentTypeEnum.ALIPAY.getStyletype()) || info.getType().equals(PaymentTypeEnum.WECHAT.getStyletype())){
			thirdcancle(position);
		}else if(info.getType().equals(PaymentTypeEnum.YUXF.getStyletype())){
			if(isCancleCount > 0){
				ToastUtils.sendtoastbyhandler(handler, "撤销中，请稍后再试");
				return;
			}
			String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
			Map<String, String> map = new HashMap<String, String>();
			map.put("reason", "交易撤销");
			map.put("refundSign", tradeNo);
			map.put("refundAmount", info.getMoney());
			map.put("sktNo", SpSaveUtils.read(getApplicationContext(), ConstantData.CASHIER_DESK_CODE, ""));
			map.put("jlbh", AppConfigFile.getBillId());
			map.put("njlbh", AppConfigFile.getBillId());
			map.put("nsktNo", SpSaveUtils.read(getApplicationContext(), ConstantData.CASHIER_DESK_CODE, ""));
			HttpRequestUtil.getinstance().msxfRefund(map, BaseResult.class, new HttpActionHandle<BaseResult>() {

				@Override
				public void handleActionStart() {
					isCancleCount++;
					info.setDes(getString(R.string.cancleing_pay));
					canclePayAdapter.notifyDataSetChanged();
				}

				@Override
				public void handleActionFinish() {
					isCancleCount--;
				}

				@Override
				public void handleActionError(String actionName, String errmsg) {
					CanclePayDialog.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							info.setDes(getString(R.string.cancled_failed));
							canclePayAdapter.notifyDataSetChanged();
						}
					});
				}

				@Override
				public void handleActionSuccess(String actionName,
												final BaseResult result) {
					CanclePayDialog.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
								info.setIsCancle(true);
								info.setDes(getString(R.string.cancled_pay));
								canclePayAdapter.notifyDataSetChanged();
							}else{
								info.setDes(getString(R.string.cancled_failed));
								canclePayAdapter.notifyDataSetChanged();
							}
						}
					});
				}
			});
		}else {
			if (MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
				if(isCancleCount > 0){
					ToastUtils.sendtoastbyhandler(handler, "撤销中，请稍后再试");
					return;
				}
				if(payments.get(position).getDes().equals(getString(R.string.cancled_failed))
						||payments.get(position).getDes().equals(getString(R.string.cancled))){
					isCancleCount++;
					this.position = position;
					innerRequestCashier(info.getTxnid());
				}
			}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
				if(!isLogin){
					ToastUtils.sendtoastbyhandler(handler, "收银通未登录");
					return;
				}
				if(isCancleCount > 0){
					ToastUtils.sendtoastbyhandler(handler, "撤销中，请稍后再试");
					return;
				}

				if(payments.get(position).getDes().equals(getString(R.string.cancled_failed))
						||payments.get(position).getDes().equals(getString(R.string.cancled))){
					this.position = position;
					SaleVoidRequest saleVoidRequest = new SaleVoidRequest(info.getTxnid());

					AidlRequestManager.getInstance().aidlSaleVoidRequest(mYunService,
							saleVoidRequest, new AidlRequestManager.AidlRequestCallBack() {

								@Override
								public void onTaskStart() {
									isCancleCount++;
									info.setDes(getString(R.string.cancleing_pay));
									canclePayAdapter.notifyDataSetChanged();
								}

								@Override
								public void onTaskFinish(JSONObject rspJSON) {
									isCancleCount--;
									if (!rspJSON.optString("responseCode").equals("00")) {
										info.setDes(getString(R.string.cancled_failed));
										canclePayAdapter.notifyDataSetChanged();
									}else{
										info.setIsCancle(true);
										info.setDes(getString(R.string.cancled_pay));
										canclePayAdapter.notifyDataSetChanged();
									}
								}

								@Override
								public void onTaskCancelled() {
								}

								@Override
								public void onException(Exception e) {
									isCancleCount--;
									info.setDes(getString(R.string.cancled_failed));
									canclePayAdapter.notifyDataSetChanged();
									ToastUtils.sendtoastbyhandler(handler, "发生异常，异常信息是：" + e.toString());
								}
							});
				}
			}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
				if(isCancleCount > 0){
					ToastUtils.sendtoastbyhandler(handler, "撤销中，请稍后再试");
					return;
				}
				if(payments.get(position).getDes().equals(getString(R.string.cancled_failed))
						||payments.get(position).getDes().equals(getString(R.string.cancled))){
					this.position = position;
					JSONObject json = new JSONObject();
					String tradeNo = Utils.formatDate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmss") + AppConfigFile.getBillId();
					try {
						json.put("orgTraceNo",info.getTraceNo());
						json.put("extOrderNo", tradeNo);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					isCancleCount++;
					info.setDes(getString(R.string.cancleing_pay));
					canclePayAdapter.notifyDataSetChanged();
					AppHelper.callTrans(CanclePayDialog.this, ConstantData.YHK_SK, ConstantData.YHK_CX, json);
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(Activity.RESULT_OK == resultCode){
			if(AppHelper.TRANS_REQUEST_CODE == requestCode){
				PayMentsCancleInfo info = payments.get(position);
				isCancleCount--;
				if (null != data) {
					StringBuilder result = new StringBuilder();
					Map<String,String> map = AppHelper.filterTransResult(data);
					if("0".equals(map.get(AppHelper.RESULT_CODE))){
						Type type =new TypeToken<Map<String, String>>(){}.getType();
						try {
							Map<String, String> transData = GsonUtil.jsonToObect(map.get(AppHelper.TRANS_DATA), type);
							if("00".equals(transData.get("resCode"))){
								OrderBean orderBean= new OrderBean();
								orderBean.setTransAmount(CurrencyUnit.yuan2fenStr(info.getMoney()));
								orderBean.setTxnId(transData.get("extOrderNo"));
								orderBean.setAccountNo(transData.get("cardNo"));
								orderBean.setAcquId(transData.get("cardIssuerCode"));
								orderBean.setBatchId(transData.get("traceNo"));
								orderBean.setRefNo(transData.get("refNo"));
								info.setIsCancle(true);
								info.setDes(getString(R.string.cancled_pay));
								canclePayAdapter.notifyDataSetChanged();
								orderBean.setPaymentId(payments.get(position).getId());
								orderBean.setTransType(ConstantData.TRANS_REVOKE);
								orderBean.setTraceId(AppConfigFile.getBillId());
								Intent serviceintent = new Intent(mContext, RunTimeService.class);
								serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
								serviceintent.putExtra(ConstantData.THIRD_DATA, orderBean);
								startService(serviceintent);
							}else{
								info.setDes(getString(R.string.cancled_failed));
								canclePayAdapter.notifyDataSetChanged();
								ToastUtils.sendtoastbyhandler(handler, transData.get("resDesc"));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						String msg = "银行卡撤销返回信息异常";
						if(!StringUtil.isEmpty(map.get(AppHelper.RESULT_MSG))){
							msg = map.get(AppHelper.RESULT_MSG);
						}
						info.setDes(getString(R.string.cancled_failed));
						canclePayAdapter.notifyDataSetChanged();
					}
				}else{
					info.setDes(getString(R.string.cancled_failed));
					canclePayAdapter.notifyDataSetChanged();
					ToastUtils.sendtoastbyhandler(handler,"银行卡撤销异常！");
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
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
			PayMentsCancleInfo info = payments.get(position);
			switch (message.what) {
				case TransState.State_Waiting_Reverse: {
					break;
				}
				case TransState.State_Reverse_Success: {
					// 冲正成功
					// 冲正成功后该笔交易失效，需要重新手动发起交易（此处需要UI提示）
					info.setDes(getString(R.string.cancled_failed));
					canclePayAdapter.notifyDataSetChanged();
					break;
				}
				case TransState.State_Reverse_Failed: {
					// 冲正失败
					// 冲正失败后该笔交易失效，需要重新手动发起交易（此处需要UI提示）
					info.setDes(getString(R.string.cancled_failed));
					canclePayAdapter.notifyDataSetChanged();
					break;
				}

				case TransState.State_Waiting_Sign: {
					// 签到中
					break;
				}
				case TransState.State_Finish_Sign: {
					// 签到完成
					try {
						JSONObject object = new JSONObject(dataString);
						if (object.optString("responseCode").equals("00")) {
							return;
						} else {
							info.setDes(getString(R.string.cancled_failed));
							canclePayAdapter.notifyDataSetChanged();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				}
				case TransState.State_Waiting_Swipe: {
					isWaitSwip = true;
					ToastUtils.sendtoastbyhandler(handler, "请刷卡");
					break;
				}
				case TransState.State_Finish_Swipe: {
					isWaitSwip = false;
					break;
				}
				case TransState.State_Waiting_Pinpad: {
					break;
				}
				case TransState.State_Finish_Pinpad: {
					// 输密结束
					break;
				}
				case TransState.State_Waiting_Trans: {
					break;
				}
				case TransState.State_Neeed_Query: {
					// 需要查询
					isWaitSwip = false;
					info.setDes(getString(R.string.cancled_query));
					canclePayAdapter.notifyDataSetChanged();
					break;
				}
				case TransState.State_Pinpad_Input: {
					break;
				}
				case TransState.State_Catch_DOWN_TRANS: {
					// 是否降级交易
					// TODO 应用层需要处理选择
					try {
						mYunService.stopDownTrade();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					info.setDes(getString(R.string.cancled_failed));
					canclePayAdapter.notifyDataSetChanged();
					ToastUtils.sendtoastbyhandler(handler, "暂不支持降级交易！");
					break;
				}
				case TransState.State_Abord_Trans: {
					isWaitSwip = false;
					// 交易取消（超时、手动取消、联网交易密码错、余额不足、发卡行不允许~~~）
					info.setDes(getString(R.string.cancled_failed));
					canclePayAdapter.notifyDataSetChanged();
					break;
				}
				case TransState.State_Finish_Trans: {
					isWaitSwip = false;
					info.setIsCancle(true);
					info.setDes(getString(R.string.cancled_pay));
					canclePayAdapter.notifyDataSetChanged();
					handleTransResult(true, dataString);
					// 银行卡交易完成（成功）
					break;
				}
				case TransState.STATE_NO_CARD_TRANS_START: {
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
				case TransState.STATE_NO_CARD_VOID_FINISH: {
					if (dataString != null && !dataString.isEmpty()) {
						try {
							JSONObject dataJson = new JSONObject(dataString);
							String code  = dataJson.optString("resultCode");
							if(code == null || "".equals(code) || !"00".equals(code)){
								info.setDes(getString(R.string.cancled_failed));
								canclePayAdapter.notifyDataSetChanged();
								return;
							}
							info.setIsCancle(true);
							info.setDes(getString(R.string.cancled_pay));
							canclePayAdapter.notifyDataSetChanged();
							handleTransResult(true, dataString);
						} catch (JSONException e) {
							e.printStackTrace();
							info.setDes(getString(R.string.cancled_failed));
							canclePayAdapter.notifyDataSetChanged();
						}
					} else {
						info.setDes(getString(R.string.cancled_failed));
						canclePayAdapter.notifyDataSetChanged();
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
		if (isSuccess && payments.get(position).getType().equals(PaymentTypeEnum.BANK.getStyletype())) {
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
			Intent intent = new Intent();
			if (resultBean.getTransType().equals(ConstantData.SALE)) {

			} else if (resultBean.getTransType().equals(ConstantData.SALE_VOID)) {
				if (resultBean.getResultCode().equals("00")) {
					resultBean.setOrderState("0");
					resultBean.setPaymentId(payments.get(position).getId());
					resultBean.setTransType(ConstantData.TRANS_REVOKE);
					resultBean.setTraceId(AppConfigFile.getBillId());
					Intent serviceintent = new Intent(mContext, RunTimeService.class);
					serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
					serviceintent.putExtra(ConstantData.THIRD_DATA, resultBean);
					startService(serviceintent);
				}else{
					LogUtil.e("lgs", "退款失败" + data);
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
		// newBean.setAidlPaymentInfo(paymentInfo);
		if (newBean.getOrderState().isEmpty()) {
			newBean.setOrderState(isSuccess ? "0" : "");
		}
//		if (newBean.getPaymentName().isEmpty()) {
//			newBean.setPaymentName(DevDemoSPEdit.getInstance(this)
//					.getPaymentName(newBean.getPaymentId()));
//		}
		// newBean.setSuccess(isSuccess);
		return newBean;
	}

	private BizServiceInvoker mBizServiceInvoker;

	// 1.执行调用之前需要调用WeiposImpl.as().init()方法，保证sdk初始化成功。
	//
	// 2.调用收银支付成功后，收银支付结果页面完成后，BizServiceInvoker.OnResponseListener后收到响应的结果
	//
	// 3.如果需要页面调回到自己的App，需要在调用中增加参数package和classpath(如com.xxx.pay.ResultActivity)，并且这个跳转的Activity需要在AndroidManifest.xml中增加android:exported=”true”属性。
	private void innerRequestCashier(String tradeNo) {
		String seqNo = "1";//服务端请求序列,本地应用调用可固定写死为1
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
			isCancleCount--;
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			isCancleCount--;
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
			isCancleCount--;
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
					payments.get(position).setIsCancle(true);
					payments.get(position).setDes(getString(R.string.cancled_pay));
					canclePayAdapter.notifyDataSetChanged();
					OrderBean orderBean= new OrderBean();
					orderBean.setAccountNo(CurrencyUnit.yuan2fenStr(payments.get(position).getMoney()));
					orderBean.setTxnId(info.getCashier_trade_no());
					orderBean.setRefNo(info.getRefund_ref_no());
					orderBean.setBatchId(info.getRefund_vouch_no());
					orderBean.setPaymentId(payments.get(position).getId());
					orderBean.setTransType(ConstantData.TRANS_REVOKE);
					orderBean.setTraceId(AppConfigFile.getBillId());
					Intent serviceintent = new Intent(mContext, RunTimeService.class);
					serviceintent.putExtra(ConstantData.SAVE_THIRD_DATA, true);
					serviceintent.putExtra(ConstantData.THIRD_DATA, orderBean);
					startService(serviceintent);
				}else{
					payments.get(position).setDes(getString(R.string.cancled_failed));
					canclePayAdapter.notifyDataSetChanged();
					ToastUtils.sendtoastbyhandler(handler, info.getErrMsg());
				}
			}else{
				payments.get(position).setDes(getString(R.string.cancled_failed));
				canclePayAdapter.notifyDataSetChanged();
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
	private void requestCashier() {

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
	}
}
