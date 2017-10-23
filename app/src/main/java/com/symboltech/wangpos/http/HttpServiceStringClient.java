package com.symboltech.wangpos.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.symboltech.wangpos.R;
import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;
import com.symboltech.wangpos.log.LogUtil;
import com.symboltech.wangpos.result.BaseResult;
import com.symboltech.wangpos.utils.SpSaveUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * simple introduction Volley http 请求基类
 * <p>
 * detailed comment
 *
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月22日
 * @see
 * @since 1.0
 */
public class HttpServiceStringClient {
	private static HttpServiceStringClient httpServiceStringRequest;
	private static OkHttpClient httpServiceClient ;
	private static final int TIMEOUT_MS_DEFAULT = 40 *1000;
	private static final int MAX_RETRIES = 0;
	private static final int BACKOFF_MULT = 0;

	private HttpServiceStringClient() {
		initHttpConfig();
	}

    public void initHttpConfig() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(TIMEOUT_MS_DEFAULT, TimeUnit.MILLISECONDS)
				.writeTimeout(TIMEOUT_MS_DEFAULT, TimeUnit.MILLISECONDS)
				.readTimeout(TIMEOUT_MS_DEFAULT, TimeUnit.MILLISECONDS);
		httpServiceClient = builder.build();
    }

	public static HttpServiceStringClient getinstance() {
		if (httpServiceStringRequest == null) {
			synchronized (HttpServiceStringClient.class) {
				if (httpServiceStringRequest == null) {
					httpServiceStringRequest = new HttpServiceStringClient();
				}
			}
		}
		return httpServiceStringRequest;
	}

	/**
	 * 获取请求队列
	 *
	 * @return
	 */
	public OkHttpClient getRequestClient() {
		return httpServiceClient;
	}

	public void cancleRequest(){
		synchronized (httpServiceClient.dispatcher().getClass()) {
			for (Call call : httpServiceClient.dispatcher().queuedCalls()) {
				if (ConstantData.NET_TAG.equals(call.request().tag())) call.cancel();
			}
			for (Call call : httpServiceClient.dispatcher().runningCalls()) {
				if (ConstantData.NET_TAG.equals(call.request().tag())) call.cancel();
			}
		}
	}
	/**
	 *
	 * @param actionname
	 * @param url
	 * @param map
	 * @param clz
	 * @param httpactionhandler
	 * @param <T>
	 */
	public <T> void getForObject(final String actionname, String url, Map<String, String> map, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		final Gson gson = getGson();
		httpactionhandler.handleActionStart();

		Map<String, String> param = map;
		if (param == null) {
			param = new HashMap<String, String>();
		}
		param.put("token", SpSaveUtils.read(MyApplication.context, ConstantData.LOGIN_TOKEN, ""));
		FormBody.Builder builder = new FormBody.Builder();
		Set<Map.Entry<String, String>> set = param.entrySet();
		for (Iterator<Map.Entry<String, String>> it = set.iterator(); it.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			if(entry.getValue() == null){
				httpactionhandler.handleActionError(actionname, "参数异常");
				httpactionhandler.handleActionFinish();
				return;
			}
			builder.add(entry.getKey(), entry.getValue());
		}
		LogUtil.i("lgs","map--ss---"+param.toString());
		FormBody formBody = builder.build();
		Request request;
		try {
			request = new Request.Builder().tag(actionname)
					.url(url)
					.post(formBody)
					.build();
		}catch (Exception e){
			e.printStackTrace();
			httpactionhandler.handleActionError(actionname, "服务器地址错误");
			httpactionhandler.handleActionFinish();
			return;
		}
		requestPost(url, param, request, new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
				Boolean ret = false;
				if (e.getMessage() != null) {
					if (e.getMessage().contains("Failed to connect to") || e.getMessage().contains("failed to connect to")) {
						ret = true;
					}
				}
				if (ret) {
					if (AppConfigFile.isNetConnect()) {
						AppConfigFile.setNetConnect(false);
					}
				}
				httpactionhandler.handleActionError(actionname, e.getMessage());
				httpactionhandler.handleActionFinish();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				T result = null;
				LogUtil.i("lgs", response.code() + response.message());
				if (response != null && response.code() == 200) {
					if (!AppConfigFile.isNetConnect()) {
						AppConfigFile.setNetConnect(true);
					}
					try {
						String re = response.body().string();
						LogUtil.i("lgs", "response====service======" + re);
						result = gson.fromJson(re, clz);
						if (result != null) {
							if (((BaseResult) result).getCode() != null)
								httpactionhandler.handleActionSuccess(actionname, result);
							else {
								httpactionhandler.handleActionError(actionname, MyApplication.context.getString(R.string.exception_opt));
							}
						} else {
							httpactionhandler.handleActionError(actionname, MyApplication.context.getString(R.string.exception_opt));
						}

					} catch (JsonSyntaxException e) {
						httpactionhandler.handleActionError(actionname, MyApplication.context.getString(R.string.exception_opt));
						e.printStackTrace();
					} catch (JsonParseException e) {
						httpactionhandler.handleActionError(actionname, MyApplication.context.getString(R.string.exception_opt));
						e.printStackTrace();
					} catch (IOException e) {
						httpactionhandler.handleActionError(actionname, MyApplication.context.getString(R.string.exception_opt));
						e.printStackTrace();
					}
				} else {
					httpactionhandler.handleActionError(actionname, response.message());
				}
				httpactionhandler.handleActionFinish();
			}
		});
	}

	/**
	 * 设置有的属性不能映射时候不报错
	 *
	 * @return ObjectMapper
	 */
	public Gson getGson() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson;
	}

	/**
	 *
	 * @param url
	 * @param param
	 * @param request
	 * @param call
	 */
	public void requestPost(String url, Map<String, String> param, Request request, Callback call) {
		request("POST", url, param, null, request, call);
	}

	/**
	 *
	 * @param url
	 * @param param
	 * @param request
	 * @param call
	 */
	public void requestGet(String url, final Map<String, String> param, Request request, Callback call) {

		request("GET", url, param, null, request, call);
	}

	/**
	 *
	 * @param method
	 * @param url
	 * @param param
	 * @param headers
	 * @param request
	 * @param callBack
	 */
	private void request(String method, String url, final Map<String, String> param,
			final Map<String, String> headers, Request request, Callback callBack) {
		try {
			if ("GET".equals(method) && param != null) {
				StringBuilder sb = new StringBuilder();
				sb.append("?");
				Set<Map.Entry<String, String>> set = param.entrySet();
				for (Iterator<Map.Entry<String, String>> it = set.iterator(); it.hasNext();) {
					Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
					sb.append(entry.getKey() + "");
					sb.append("=");
					sb.append(URLEncoder.encode(entry.getValue() + "", "UTF-8"));
					sb.append("&");
				}
				if (sb.length() > 0) {
					url += sb.substring(0, sb.length() - 1);
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		httpServiceClient.newCall(request).enqueue(callBack);
	}

}
