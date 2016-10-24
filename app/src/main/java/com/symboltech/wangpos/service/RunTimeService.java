package com.symboltech.wangpos.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.db.dao.OrderInfoDao;
import com.symboltech.wangpos.http.HttpActionHandle;
import com.symboltech.wangpos.http.HttpRequestUtil;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.log.OperateLog;
import com.symboltech.wangpos.msg.entity.OfflineBankInfo;
import com.symboltech.wangpos.msg.entity.OfflineBillInfo;
import com.symboltech.wangpos.msg.entity.OptLogInfo;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.result.LogResult;
import com.symboltech.wangpos.result.OfflineDataResult;
import com.symboltech.wangpos.utils.OptLogEnum;
import com.symboltech.wangpos.utils.SpSaveUtils;

/**
 * 后台服务，提供日志上传等功能
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 2015年12月15日
 * 
 */
public class RunTimeService extends Service {

	/**
	 * 监听pos状态需要传递的收款员
	 */
	private String cashierId = "-1";

	private Timer mTimer;

	private TimerTask mTask;

	private DateFormat formatContent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void onCreate() {
		checkNettimertask();
		super.onCreate();
	}

	/**
	 * 检测网络状态定时器
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 */
	private void checkNettimertask() {
		// TODO Auto-generated method stub
		if (mTimer == null) {
			mTimer = new Timer();
		}
		if (mTask == null) {
			mTask = new TimerTask() {
				@Override
				public void run() {
					checkNetStatus(false);
				}
			};
			mTimer.schedule(mTask, 0, AppConfigFile.NETWORK_STATUS_INTERVAL);
		}
	}

	/**
	 * 检测网络状态，发送心跳包
	 * 
	 * @param isCheck
	 *            true 检测网络状态 false 心跳包功能
	 */
	public void checkNetStatus(final boolean isCheck) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("cashier", cashierId);
		LogUtil.v("lgs", "cashierID = " + cashierId);
		HttpRequestUtil.getinstance().monitorSKT(map, BaseResult.class, new HttpActionHandle<BaseResult>() {

			@Override
			public void handleActionStart() {

			}

			@Override
			public void handleActionFinish() {

			}

			@Override
			public void handleActionError(String actionName, String errmsg) {
				LogUtil.v("lgs", "service error = " + errmsg);
			}

			@Override
			public void handleActionSuccess(String actionName, BaseResult result) {
				LogUtil.v("lgs", "service result = " + result.getCode());
				if (isCheck && ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
					if (!AppConfigFile.isNetConnect()) {
						AppConfigFile.setNetConnect(true);
					}
					if (AppConfigFile.isOffLineMode()) {
						Intent intent = new Intent(ConstantData.OFFLINE_MODE);
						intent.putExtra(ConstantData.OFFLINE_MODE_INFO, ConstantData.CHANGE_MODE_STATE);
						sendBroadcast(intent);
					}
				}
			}
		});
	}

	/**
	 * 根据状态更新收款员id
	 * 
	 * @param cashierId
	 */
	public void setCashierId(String cashierId) {
		this.cashierId = cashierId;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			if (intent.getBooleanExtra(ConstantData.UPLOAD_LOG, false)) {
				// 上传操作日志
				OperateLog.getInstance().DeleteLog(AppConfigFile.OPT_LOG_INTERVAL);
				setCashierId(intent.getStringExtra(ConstantData.CASHIER_ID));
			}
			if (intent.getBooleanExtra(ConstantData.CHECK_NET, false)) {
				// 手动检测网络
				checkNetStatus(true);
			}
			if (intent.getBooleanExtra(ConstantData.UPLOAD_OFFLINE_DATA, false)) {
				// 手动上传离线数据
				uploadOfflineData(null, AppConfigFile.OFFLINE_DATA_COUNT);
			}
			if(intent.getBooleanExtra(ConstantData.UPDATE_STATUS, false)){
				setCashierId(intent.getStringExtra(ConstantData.CASHIER_ID));
				checkNetStatus(false);
			}
			if(intent.getBooleanExtra(ConstantData.UPLOAD_OFFLINE_DATA_BYLOG, false)){
				sendOffLineByLog(null, AppConfigFile.OFFLINE_DATA_COUNT);
				sendBankOffLineByLog(null, AppConfigFile.OFFLINE_DATA_COUNT);
			}
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		LogUtil.i("lgs","onDestroy-------------");
		cancel();
		super.onDestroy();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void cancel(){
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
		OperateLog.getInstance().stopUpload();
		System.exit(0);
	}

	/**
	 * 处理每一条离线数据
	 *
	 * @param banknfos
	 * @param i
	 * @return
	 */
	private OptLogInfo dealBankData(List<OfflineBankInfo> banknfos, int i) {
		Date date = new Date();
		OptLogInfo optLogInfo = new OptLogInfo();
		optLogInfo.setOperCode(SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, "0"));
		optLogInfo.setOpMsg(new Gson().toJson(banknfos.get(i)));
		optLogInfo.setOpType(OptLogEnum.OFFLINE_BANK_DATA.getOptLogCode());
		optLogInfo.setOpTime(formatContent.format(date));
		return optLogInfo;
	}
	/**
	 * 处理每一条离线数据
	 *
	 * @param billinfos
	 * @param i
	 * @return
	 */
	private OptLogInfo dealData(List<OfflineBillInfo> billinfos, int i) {
		Date date = new Date();
		OptLogInfo optLogInfo = new OptLogInfo();
		optLogInfo.setOperCode(SpSaveUtils.read(MyApplication.context, ConstantData.CASHIER_ID, "0"));
		optLogInfo.setOpMsg(new Gson().toJson(billinfos.get(i)));
		optLogInfo.setOpType(OptLogEnum.OFFLINE_DATA.getOptLogCode());
		optLogInfo.setOpTime(formatContent.format(date));
		return optLogInfo;
	}

	/**
	 * 上传离线数据
	 *
	 * @param filename
	 *            标记名称
	 * @param data
	 *            离线数据当日志传
	 * @param billID
	 *            新的订单号
	 * @param count
	 *            上传条数
	 * @param bills
	 *            当前上传的订单号
	 * @param billinfos
	 */
	private void send2server(String filename, String data, final String billID, final int count,
							 final List<String> bills, final List<OfflineBillInfo> billinfos) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("signid", filename);
		map.put("operations", data);
		HttpRequestUtil.getinstance().saveOperationLog(map, LogResult.class, new HttpActionHandle<LogResult>() {
			@Override
			public void handleActionError(String actionName, String errmsg) {
				OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
				if (OperateLog.getInstance().saveBillLog2File(OptLogEnum.SEND_OFFLINE_DATA.getOptLogCode(), billinfos)) {
					dao.setOfflineStatus(bills);
				}
				sendOffLineByLog(billID, count);
			}

			@Override
			public void handleActionSuccess(String actionName, LogResult result) {
				OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
				if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())) {
					dao.setOfflineStatus(bills);
				} else {
					if (OperateLog.getInstance().saveBillLog2File(OptLogEnum.SEND_OFFLINE_DATA.getOptLogCode(), billinfos)) {
						dao.setOfflineStatus(bills);
					}
				}
				sendOffLineByLog(billID, count);
			}
		});
	}

	/**
	 * 通过日志方式发送离线数据
	 */
	public void sendOffLineByLog(String beginBillID, final int count) {
		OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
		List<OfflineBillInfo> billinfos = dao.getOfflineOrderInfo(beginBillID, count);
		if (billinfos != null && billinfos.size() > 0) {
			// 存放billID
			List<String> bills = new ArrayList<>();
			List<OptLogInfo> list = new ArrayList<>();
			for (int i = 0; i < billinfos.size(); i++) {
				bills.add(billinfos.get(i).getConfirmbillinfos().getBillid());
				OptLogInfo opt = dealData(billinfos, i);
				list.add(opt);
			}
			// 重新复制开始的订单号
			final String newBillID = billinfos.get(billinfos.size() - 1).getConfirmbillinfos().getBillid();
			send2server("offdata", new Gson().toJson(list), newBillID, count, bills, billinfos);
		}
	}


	/**
	 * 上传银行离线数据
	 *
	 * @param filename
	 *            标记名称
	 * @param data
	 *            离线数据当日志传
	 * @param billID
	 *            新的订单号
	 * @param count
	 *            上传条数
	 * @param bills
	 *            当前上传的订单号
	 * @param billinfos
	 */
	private void sendBank2server(String filename, String data, final String billID, final int count,
								 final List<String> bills, final List<OfflineBankInfo> billinfos) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("signid", filename);
		map.put("operations", data);
		LogUtil.i("lgs", data);
		HttpRequestUtil.getinstance().saveOperationLog(map, LogResult.class, new HttpActionHandle<LogResult>() {
			@Override
			public void handleActionError(String actionName, String errmsg) {
				OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
				if(OperateLog.getInstance().saveBankLog2File(OptLogEnum.SEND_OFFLINE_DATA.getOptLogCode(), billinfos)) {
					dao.setBankOfflineStatus(bills);
				}
				sendBankOffLineByLog(billID, count);
			}

			@Override
			public void handleActionSuccess(String actionName, LogResult result) {
				OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
				if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())){
					dao.setBankOfflineStatus(bills);
				}else{
					if(OperateLog.getInstance().saveBankLog2File(OptLogEnum.SEND_OFFLINE_DATA.getOptLogCode(), billinfos)) {
						dao.setBankOfflineStatus(bills);
					}
				}
				sendBankOffLineByLog(billID, count);
			}
		});
	}


	/**
	 * 通过日志方式发送银行离线数据
	 */
	public void sendBankOffLineByLog(String beginBillID, final int count) {
		OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
		List<OfflineBankInfo> bankinfos = dao.getOfflineBankInfo(beginBillID, count);
		if (bankinfos != null && bankinfos.size() > 0) {
			// 存放billID
			List<String> bills = new ArrayList<String>();
			List<OptLogInfo> list = new ArrayList<OptLogInfo>();
			for (int i = 0; i < bankinfos.size(); i++) {
				bills.add(bankinfos.get(i).getTradeno());
				OptLogInfo opt = dealBankData(bankinfos, i);
				list.add(opt);
			}
			// 重新复制开始的订单号
			final String newBillID = bankinfos.get(bankinfos.size() - 1).getTradeno();
			sendBank2server("offbankdata", new Gson().toJson(list), newBillID, count, bills, bankinfos);
		}
	}

	/**
	 * 上传离线数据
	 *
	 * @param beginBillID
	 *            上传开始订单号，传空即是从头开始
	 * @param count
	 *            每次上传条数
	 */
	public void uploadOfflineData(String beginBillID, final int count) {
		OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
		List<OfflineBillInfo> billinfos = null;
		if (AppConfigFile.isNetConnect()) {
			billinfos = dao.getOfflineOrderInfo(beginBillID, count);
		}
		if (billinfos != null && billinfos.size() > 0) {
			Map<String, String> map = new HashMap<String, String>();
			// 重新复制开始的订单号
			final String newBillID = billinfos.get(billinfos.size() - 1).getConfirmbillinfos().getBillid();
			String json = new GsonBuilder().serializeNulls().create().toJson(billinfos);
			LogUtil.v("lgs", "error = " + json);
			map.put("billinfo", json);
			HttpRequestUtil.getinstance().uploadOfflineData(map, OfflineDataResult.class,
					new HttpActionHandle<OfflineDataResult>() {
						@Override
						public void handleActionError(String actionName, String errmsg) {
							LogUtil.v("lgs", "error = " + errmsg);
							uploadOfflineData(newBillID, count);
						}

						@Override
						public void handleActionSuccess(String actionName, OfflineDataResult result) {
							if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())
									|| ConstantData.HTTP_RESPONSE_PART_OK.equals(result.getCode())) {
								if (result != null || result.getOfflineDatainfo() != null) {
									OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
									dao.setOfflineStatus(result.getOfflineDatainfo().getSucbillidlist());
								}
							} else {
								LogUtil.v("lgs", result.getMsg());
							}
							// 不管成功还是不成功，都进行下次上传
							uploadOfflineData(newBillID, count);
						}
					});
		} else {
			uploadOfflineBankData(null, AppConfigFile.OFFLINE_DATA_COUNT);
		}
	}

	/**
	 * 上传离线数据
	 *
	 * @param beginBillID
	 *            上传开始订单号，传空即是从头开始
	 * @param count
	 *            每次上传条数
	 */
	public void uploadOfflineBankData(String beginBillID, final int count) {
		OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
		List<OfflineBankInfo> bankinfos = null;
		if (AppConfigFile.isNetConnect()) {
			bankinfos = dao.getOfflineBankInfo(beginBillID, count);
		}
		if (bankinfos != null && bankinfos.size() > 0) {
			Map<String, String> map = new HashMap<String, String>();
			// 重新复制开始的订单号
			final String newBillID = bankinfos.get(bankinfos.size() - 1).getTradeno();
			String json = new GsonBuilder().serializeNulls().create().toJson(bankinfos);
			LogUtil.v("lgs", "error = " + json);
			map.put("data", json);
			HttpRequestUtil.getinstance().uploadOfflineBankData(map, OfflineDataResult.class,
					new HttpActionHandle<OfflineDataResult>() {

						@Override
						public void handleActionError(String actionName, String errmsg) {
							LogUtil.v("lgs", "error = " + errmsg);
							uploadOfflineBankData(newBillID, count);
						}

						@Override
						public void handleActionSuccess(String actionName, OfflineDataResult result) {
							if (ConstantData.HTTP_RESPONSE_OK.equals(result.getCode())
									|| ConstantData.HTTP_RESPONSE_PART_OK.equals(result.getCode())) {
								if (result != null || result.getOfflineDatainfo() != null) {
									OrderInfoDao dao = new OrderInfoDao(MyApplication.context);
									dao.setBankOfflineStatus(result.getOfflineDatainfo().getSucbillidlist());
								}
							} else {
								LogUtil.v("lgs", result.getMsg());
							}
							// 不管成功还是不成功，都进行下次上传
							uploadOfflineBankData(newBillID, count);
						}
					});
		} else {
			AppConfigFile.setUploadStatus(ConstantData.UPLOAD_SUCCESS);
		}
	}
}
