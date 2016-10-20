package cn.koolcloud.aidl;


import org.json.JSONException;
import org.json.JSONObject;


import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.log.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import cn.koolcloud.engine.thirdparty.aidl.IKuYunThirdPartyService;
import cn.koolcloud.engine.thirdparty.aidlbean.LoginRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.SaleRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.SaleVoidRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransListQueryRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransPrintRequest;
import cn.koolcloud.engine.thirdparty.aidlbean.TransQueryRequest;

public class AidlRequestManager {

	public static final int AIDL_REQUEST_IS_LOGIN = 0;
	public static final int AIDL_REQUEST_LOGIN = 1;
	public static final int AIDL_REQUEST_LOGOUT = 2;
	public static final int AIDL_REQUEST_GET_PAYMENT_LIST = 3;
	public static final int AIDL_REQUEST_SALE = 4;
	public static final int AIDL_REQUEST_SALE_VOID = 5;
	public static final int AIDL_REQUEST_LAST_TRANS_QUERY = 6;
	public static final int AIDL_REQUEST_TRANS_QUERY = 7;
	public static final int AIDL_REQUEST_LAST_TRANS_PRINT = 8;
	public static final int AIDL_REQUEST_TRANS_PRINT = 9;
	public static final int AIDL_REQUEST_GET_PAYMENT_ICON = 10;
	public static final int AIDL_REQUEST_TRANS_RECORD_LIST = 11;

	private static AidlRequestManager mRequestManager = null;

	private AidlRequestManager() {

	}

	public static AidlRequestManager getInstance() {
		if (mRequestManager == null) {
			mRequestManager = new AidlRequestManager();
		}
		return mRequestManager;
	}

	/**
	 * 查看登录状态
	 * 
	 * @param callBack
	 */
	public void aidlIsLoginRequest(IKuYunThirdPartyService service, AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_IS_LOGIN);
	}

	/**
	 * 发起登录请求
	 * 
	 * @param request
	 *            登录请求体
	 * @param callBack
	 *            请求回调
	 */
	public void aidlLoginRequest(IKuYunThirdPartyService service, LoginRequest request,
			AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_LOGIN, request);
	}

	/**
	 * 发起注销登录请求
	 * 
	 * @param callBack
	 *            请求回调
	 */
	public void aidlLogoutRequest(IKuYunThirdPartyService service, AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_LOGOUT);
	}

	/**
	 * 发起获取支付列表请求
	 * 
	 * @param callBack
	 *            请求回调
	 */
	public void aidlGetPaymentListRequest(IKuYunThirdPartyService service, AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_GET_PAYMENT_LIST);
	}

	/**
	 * 发起交易请求
	 * 
	 * @param request
	 *            交易请求体
	 * @param callBack
	 *            请求回调
	 */
	public void aidlSaleRequest(IKuYunThirdPartyService service, SaleRequest request,
			AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_SALE, request);
	}

	/**
	 * 发起交易撤销请求
	 * 
	 * @param request
	 *            交易撤销请求体
	 * @param callBack
	 *            请求回调
	 */
	public void aidlSaleVoidRequest(IKuYunThirdPartyService service, SaleVoidRequest request,
			AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_SALE_VOID, request);
	}

	/**
	 * 发起末笔交易查询请求
	 * 
	 * @param callBack
	 *            请求回调
	 */
	public void aidlLastTransQueryRequest(IKuYunThirdPartyService service, AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_LAST_TRANS_QUERY);
	}

	/**
	 * 发起交易查询请求
	 * 
	 * @param request
	 *            交易查询请求体
	 * @param callBack
	 *            请求回调
	 */
	public void aidlTransQueryRequest(IKuYunThirdPartyService service, TransQueryRequest request,
			AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_TRANS_QUERY, request);
	}

	/**
	 * 发起末笔交易签购单打印请求
	 * 
	 * @param callBack
	 *            请求回调
	 */
	public void aidlLastTransPrintRequest(IKuYunThirdPartyService service, AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_LAST_TRANS_PRINT);
	}

	/**
	 * 发起交易签购单打印请求
	 * 
	 * @param request
	 *            交易查询请求体
	 * @param callBack
	 *            请求回调
	 */
	public void aidlTransPrintRequest(IKuYunThirdPartyService service, TransPrintRequest request,
			AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_TRANS_PRINT, request);
	}

	/**
	 * 发起获取交易记录列表请求
	 * 
	 * @param request
	 *            交易记录列表查询请求体
	 * @param callBack
	 *            请求回调
	 */
	public void aidlGetTransRecordListRequest(IKuYunThirdPartyService service, TransListQueryRequest request,
			AidlRequestCallBack callBack) {
		AidlRequestTask task = new AidlRequestTask(service, callBack);
		task.execute(AIDL_REQUEST_TRANS_RECORD_LIST, request);
	}

	/**
	 * AIDL请求任务类
	 * 
	 * @author chice.xu
	 * @date 2016-7-11
	 */
	private class AidlRequestTask extends
			AsyncTask<Object, Boolean, JSONObject> {
		private IKuYunThirdPartyService mYunService = null;
		private AidlRequestCallBack mCallBack = null;

		public AidlRequestTask(IKuYunThirdPartyService service,
				AidlRequestCallBack callBack) {
			this.mYunService = service;
			this.mCallBack = callBack;
		}

		@Override
		protected void onPreExecute() {
			if (mCallBack != null) {
				mCallBack.onTaskStart();
			}
			super.onPreExecute();
		}

		@Override
		protected JSONObject doInBackground(Object... params) {

			JSONObject rspJson = new JSONObject();
			if (isCancelled()) {
				return rspJson;
			}
			switch ((int) params[0]) {
			case AIDL_REQUEST_IS_LOGIN:
				try {
					rspJson = new JSONObject(mYunService.isLogin());
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}

				break;
			case AIDL_REQUEST_LOGIN:
				try {
					rspJson = new JSONObject(
							mYunService.login((LoginRequest) params[1]));
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_LOGOUT:
				try {
					rspJson = new JSONObject(mYunService.logout());
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_GET_PAYMENT_LIST:
				try {
					rspJson = new JSONObject(mYunService.getPaymentList());
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_SALE:
				try {
					rspJson = new JSONObject(
							mYunService.sale((SaleRequest) params[1]));
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_SALE_VOID:
				try {
					rspJson = new JSONObject(
							mYunService.saleVoid((SaleVoidRequest) params[1]));
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_LAST_TRANS_QUERY:
				try {
					rspJson = new JSONObject(mYunService.lastTransQuery());
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_TRANS_QUERY:
				try {
					rspJson = new JSONObject(
							mYunService
									.transQuery((TransQueryRequest) params[1]));
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_LAST_TRANS_PRINT:
				try {
					rspJson = new JSONObject(mYunService.lastTransPrint());
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_TRANS_PRINT:
				try {
					rspJson = new JSONObject(
							mYunService
									.transPrint((TransPrintRequest) params[1]));
				} catch (RemoteException | JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			case AIDL_REQUEST_TRANS_RECORD_LIST:
				try {
					String rspStr = "";
					try {
						rspStr = mYunService
								.getTransRecordList((TransListQueryRequest) params[1]);
					} catch (RemoteException e) {
						if (mCallBack != null) {
							mCallBack.onException(e);
						}
						// 当前版本不支持交易记录查询
						return rspJson;
					}
					if (rspStr == null || rspStr.isEmpty()) {
						return rspJson;
					}

					JSONObject paramsObject = new JSONObject(rspStr);
					if (!paramsObject.optString("responseCode").equals("00")) {
						return paramsObject;
					}

					Cursor c = MyApplication.context
							.getContentResolver()
							.query(Uri.parse(paramsObject.optString("URI")),
									new String[] { paramsObject
											.optString("PROJECTION") },
									paramsObject.optString("SELECTION"),
									new String[] { paramsObject
											.optString("SELECTIONARGS") }, null);
					String proxyResponse = "";
					if (c != null && c.moveToFirst()) {
						try {
							byte[] data = c.getBlob(c
									.getColumnIndexOrThrow(paramsObject
											.optString("DATA")));
							proxyResponse = ByteToObject(data)
									.toString();
						} catch (Exception e) {
							if (mCallBack != null) {
								mCallBack.onException(e);
							}
						}
						c.close();
					} else {
						LogUtil.i("lgs","query Cursor failure!");
					}
					if (proxyResponse != null) {
						JSONObject dataResponse = new JSONObject(proxyResponse);
						rspJson = dataResponse;
					}

				} catch (JSONException e) {
					if (mCallBack != null) {
						mCallBack.onException(e);
					}
				}
				break;
			}
			return rspJson;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
			if (mCallBack != null) {
				mCallBack.onTaskFinish(result);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			if (mCallBack != null) {
				mCallBack.onTaskCancelled();
			}
		}
	}

	public interface AidlRequestCallBack {
		public void onTaskStart();

		public void onTaskCancelled();

		public void onTaskFinish(JSONObject rspJSON);

		public void onException(Exception e);
	}

	/**
	 * 数组转对象
	 * @param bytes
	 * @return
	 */
	public Object ByteToObject (byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;
	}   }
