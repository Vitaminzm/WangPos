package com.symboltechshop.wangpos.dialog;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.symboltechshop.wangpos.R;
import com.symboltechshop.wangpos.activity.BaseActivity;
import com.symboltechshop.wangpos.app.ConstantData;
import com.symboltechshop.wangpos.app.MyApplication;
import com.symboltechshop.wangpos.http.HttpActionHandle;
import com.symboltechshop.wangpos.http.HttpRequestUtil;
import com.symboltechshop.wangpos.result.VerifyAuthResult;
import com.symboltechshop.wangpos.utils.AndroidUtils;
import com.symboltechshop.wangpos.utils.ToastUtils;
import com.symboltechshop.wangpos.utils.Utils;
import com.symboltechshop.wangpos.view.HorizontalKeyBoard;
import com.symboltechshop.wangpos.log.LogUtil;
import com.symboltechshop.wangpos.utils.StringUtil;
import com.ums.upos.sdk.cardslot.CardInfoEntity;
import com.ums.upos.sdk.cardslot.CardSlotManager;
import com.ums.upos.sdk.cardslot.CardSlotTypeEnum;
import com.ums.upos.sdk.cardslot.CardTypeEnum;
import com.ums.upos.sdk.cardslot.OnCardInfoListener;
import com.ums.upos.sdk.cardslot.SwipeSlotOptions;
import com.ums.upos.sdk.exception.CallServiceException;
import com.ums.upos.sdk.exception.SdkException;
import com.ums.upos.sdk.system.BaseSystemManager;
import com.ums.upos.sdk.system.OnServiceStatusListener;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.koolcloud.engine.service.aidl.IMemberCardService;
import cn.weipass.pos.sdk.MagneticReader;
import cn.weipass.pos.sdk.impl.WeiposImpl;

/**
 * Description 通用提示dialog
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月6日 上午11:25:11
 * @version 1.0
 */
public class VerifyAuthDialog extends BaseActivity {
	@Bind(R.id.edit_input_verify_auth)
	EditText edit_input_verify_auth;
	@Bind(R.id.text_cancle)
	TextView text_cancle;
	@Bind(R.id.text_confirm)
	TextView text_confirm;
	@Bind(R.id.imageview_close)
	ImageView imageview_close;

	public static final int Vipcard = 2;
	public static final int SEARCHCARDERROR = -1;
	class MyHandler extends Handler {
		WeakReference<BaseActivity> mActivity;

		MyHandler(BaseActivity activity) {
			mActivity = new WeakReference<BaseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			BaseActivity theActivity = mActivity.get();
			switch (msg.what) {
				case ToastUtils.TOAST_WHAT:
					ToastUtils.showtaostbyhandler(theActivity, msg);
					break;
				case SEARCHCARDERROR:
					if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
						if(cardSlotManager == null){
							return;
						}
						try {
							cardSlotManager.stopRead();
						} catch (SdkException e) {
							e.printStackTrace();
						} catch (CallServiceException e) {
							e.printStackTrace();
						}
						searchCardInfo();
					}
					break;
				case Vipcard:
					verifyauthbyhttp((String) msg.obj);
					if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){

					}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
						try {
							if (mCardService != null) {
								isSwipVipCard = true;
								mCardService.startReadCard();
							} else {
								LogUtil.e("lgs", "mCardService==null");
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
						searchCardInfo();
					}
					break;
			}
		}
	}
	private HorizontalKeyBoard keyboard;
	MyHandler handler = new MyHandler(this);

	private boolean isSwipVipCard =false;

	private IMemberCardService mCardService = null;
	private ServiceConnection mMemberCardConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mCardService = null;

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mCardService = IMemberCardService.Stub.asInterface(service);
		}
	};
	private MsrBroadcastReceiver msrReceiver = new MsrBroadcastReceiver();
	MagneticReader mMagneticReader;

	private CardSlotManager cardSlotManager = null;
	private ReadMagTask mReadMagTask = null;

	@Override
	protected void initData() {
		keyboard = new HorizontalKeyBoard(this, this, edit_input_verify_auth, null);
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_W)){
			mMagneticReader = WeiposImpl.as().openMagneticReader();
			if (mMagneticReader == null) {
				ToastUtils.sendtoastbyhandler(handler, "磁条卡读取服务不可用！");
			}
			if (mReadMagTask == null) {
				mReadMagTask = new ReadMagTask();
				mReadMagTask.start();
			}
		}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_K)){
			Intent intent = new Intent(IMemberCardService.class.getName());
			intent = AndroidUtils.getExplicitIntent(this, intent);
			if (intent != null) {
				bindService(intent, mMemberCardConnection, Context.BIND_AUTO_CREATE);
			} else {
				ToastUtils.sendtoastbyhandler(handler, "Check engine application version");
			}
			registerReceiver(msrReceiver, new IntentFilter("cn.koolcloud.engine.memberCard"));
			try {
				if (mCardService != null) {
					isSwipVipCard = true;
					mCardService.startReadCard();
				} else {
					LogUtil.e("lgs", "mCardService==null");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			try {
				BaseSystemManager.getInstance().deviceServiceLogin(
						this, null, "99999998",//设备ID，生产找后台配置
						new OnServiceStatusListener() {
							@Override
							public void onStatus(int arg0) {//arg0可见ServiceResult.java
								if (0 == arg0 || 2 == arg0 || 100 == arg0) {//0：登录成功，有相关参数；2：登录成功，无相关参数；100：重复登录。
									cardSlotManager = new CardSlotManager();
									searchCardInfo();
								}
							}
						});
				searchCardInfo();
			} catch (SdkException e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	protected void initView() {
		setContentView(R.layout.dialog_verify_auth);
		setFinishOnTouchOutside(false);
		ButterKnife.bind(this);
	}

	@Override
	protected void recycleMemery() {
		try {
			if (mCardService != null) {
				if(isSwipVipCard){
					isSwipVipCard = false;
					mCardService.stopReadCard();
				}
			} else {
				LogUtil.e("lgs", "mCardService==null");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (mCardService != null) {
			unbindService(mMemberCardConnection);
			unregisterReceiver(msrReceiver);
		}
		isRun = false;
		if(cardSlotManager != null){
			try {
				cardSlotManager.stopRead();
			} catch (SdkException e) {
				e.printStackTrace();
			} catch (CallServiceException e) {
				e.printStackTrace();
			}
		}
		if(MyApplication.posType.equals(ConstantData.POS_TYPE_Y)){
			try {
				BaseSystemManager.getInstance().deviceServiceLogout();
			} catch (SdkException e) {
				e.printStackTrace();
			}
		}
		handler.removeCallbacksAndMessages(null);
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
				if(keyboard.isShowing()){
					keyboard.dismiss();
				}
				finish();
				break;
			case R.id.text_confirm:
				if (!StringUtil.isEmpty(edit_input_verify_auth.getText().toString().trim())) {
					verifyauthbyhttp(edit_input_verify_auth.getText().toString().trim());
				} else {
					ToastUtils.sendtoastbyhandler(handler, getString(R.string.waring_null_msg));
				}
				break;
		}
	}

	private boolean isVerify = false;
	private  void verifyauthbyhttp(String rightcardno) {
		if(isVerify){
			return;
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("rightcardno", rightcardno);
		HttpRequestUtil.getinstance().RefundRight(HTTP_TASK_KEY, map, VerifyAuthResult.class,
				new HttpActionHandle<VerifyAuthResult>() {

					@Override
					public void handleActionStart() {
						isVerify = true;
						startwaitdialog();
					}

					@Override
					public void handleActionFinish() {
						isVerify = false;
						closewaitdialog();
					}

					@Override
					public void handleActionError(String actionName, String errmsg) {
						ToastUtils.sendtoastbyhandler(handler, errmsg);
					}

					@Override
					public void handleActionSuccess(String actionName, VerifyAuthResult result) {
						if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
							if(!StringUtil.isEmpty(result.getData()) && "1".equals(result.getData())){
								setResult(ConstantData.VERIFY_AUTH_RESULT_CODE, null);
								finish();
							}else{
								ToastUtils.sendtoastbyhandler(handler, "该授权码无权限");
							}
						} else {
							ToastUtils.sendtoastbyhandler(handler, result.getMsg());
						}
					}
				});
		}
	class MsrBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Message msgContext = (Message) intent.getParcelableExtra(Message.class
						.getName());
				Bundle data = msgContext.getData();

				if (data != null) {
					JSONObject jsonData = new JSONObject(data.getString("data"));
					if(!StringUtil.isEmpty(jsonData.optString("cardNo"))){
						Message msg = Message.obtain();
						msg.obj = jsonData.optString("cardNo");
						msg.what = Vipcard;
						handler.sendMessage(msg);
					}
					LogUtil.i("lgs", "dataStr is :" + jsonData.toString());
					// 处理返回结果
					LogUtil.i("lgs","\n\ntrack1 : "
							+ jsonData.optString("track1") + "\n\n"
							+ "track2 : " + jsonData.optString("track2")
							+ "\n\n" + "track3 : "
							+ jsonData.optString("track3") + "\n\n"
							+ "cardNo : " + jsonData.optString("cardNo")
							+ "\n\n" + "validTime : "
							+ jsonData.optString("validTime") + "\n\n"
							+ "serviceCode : "
							+ jsonData.optString("serviceCode") + "\n");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private boolean isRun = false;
	class ReadMagTask extends Thread {
		@Override
		public void run() {
			isRun = true;
			// 磁卡刷卡后，主动获取解码后的字符串数据信息
			try {
				while (isRun) {
					String decodeData = getMagneticReaderInfo();
					if (decodeData != null && decodeData.length() != 0) {
						System.out.println("final============>>>" + decodeData);
						Message m = Message.obtain();
						m.obj = decodeData;
						m.what = Vipcard;
						handler.sendMessage(m);
					}
					Thread.sleep(500);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				isRun = false;
			}
		}
	}

	public String getMagneticReaderInfo() {
		if (mMagneticReader == null) {
			ToastUtils.sendtoastbyhandler(handler, "初始化磁条卡sdk失败");
			return "";
		}
		// 刷卡后，主动获取磁卡的byte[]数据
		// byte[] cardByte = mMagneticReader.readCard();
		// String decodeData = mMagneticReader.getCardDecodeData();
		// 磁卡刷卡后，主动获取解码后的字符串数据信息
		String[] decodeData = mMagneticReader.getCardDecodeThreeTrackData();//
		if (decodeData != null && decodeData.length > 0) {
			/**
			 * 1：刷会员卡返回会员卡号后面变动的卡号，前面为固定卡号（没有写入到磁卡中）
			 * 如会员卡号：9999100100030318，读卡返回数据为00030318，前面99991001在磁卡中没有写入
			 * 2：刷银行卡返回数据格式为：卡号=有效期。
			 */
			for (int i = 0; i < decodeData.length; i++) {
				if (decodeData[i] == null)
					continue;
				return decodeData[i];
			}
			return "";
		} else {
			return "";
		}
	}

	private void searchCardInfo() {
		if(cardSlotManager == null){
			return;
		}
		Set<CardSlotTypeEnum> slotTypes = new HashSet<CardSlotTypeEnum>();
		slotTypes.add(CardSlotTypeEnum.SWIPE);
		Set<CardTypeEnum> cardTypes = new HashSet<CardTypeEnum>();
		cardTypes.add(CardTypeEnum.MAG_CARD);
		int timeout = 0;
		try {
			Map<CardSlotTypeEnum, Bundle> options = new HashMap<CardSlotTypeEnum, Bundle>();
			Bundle bundle = new Bundle();
			bundle.putBoolean(SwipeSlotOptions.LRC_CHECK, false);
			options.put(CardSlotTypeEnum.SWIPE, bundle);
			cardSlotManager.setConfig(options);
			cardSlotManager.readCard(slotTypes, cardTypes, timeout,
					new OnCardInfoListener() {

						@Override
						public void onCardInfo(int arg0, CardInfoEntity arg1) {
							if (0 != arg0) {
								handler.sendEmptyMessage(SEARCHCARDERROR);
							} else {
								switch (arg1.getActuralEnterType()) {
									case MAG_CARD:
										LogUtil.i("lgs", "磁道1：" + arg1.getTk1()
												+ "\n" + "磁道2：" + arg1.getTk2()
												+ "\n" + "磁道3：" + arg1.getTk3());
										Message msg = Message.obtain();
										msg.obj = arg1.getTk2();
										msg.what = Vipcard;
										handler.sendMessage(msg);
										break;
									default:
										break;
								}
								try {
									cardSlotManager.stopRead();
								} catch (SdkException e) {
									e.printStackTrace();
								} catch (CallServiceException e) {
									e.printStackTrace();
								}
							}
						}
					}, null);
		} catch (SdkException e) {
			e.printStackTrace();
		} catch (CallServiceException e) {
			e.printStackTrace();
		}

	}
}
