package com.symboltech.wangpos.http;

import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.app.ConstantData;
import com.symboltech.wangpos.app.MyApplication;

import java.util.Map;


/**
 * 
 * simple introduction http 请求封装
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月22日
 * @see
 * @since 1.0
 */
public class HttpRequestUtil {
	
	/** 开发环境 */
//	private static final String SYM_HOST = "http://210.14.148.181:81/";

	/** 测试环境 */
	private static final String SYM_HOST = "http://210.14.148.183:81/";


	/**
	 * 获取实际接口地址
	 * 
	 * @param url
	 * @return
	 */
	private static String getUrl(String url) {
		return ConstantData.IP_HOST_CONFIG_PREFIX + AppConfigFile.getHost_config() +"/"+ url;
	}

	private HttpRequestUtil() {
	}

	public static HttpRequestUtil getinstance() {
		return new HttpRequestUtil();
	}
	


	/**
	 * 补打交易订单
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void printerOrderagain(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetReprintInfo"), param, clz,
				httpactionhandler);
	}

	/**
	 * 第三方支付
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void thirdpay(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/paycenter/pay"), param, clz,
				httpactionhandler);
	}

	/**
	 * 第三方支付撤销
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void thirdpaycancel(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/paycenter/cancel"), param, clz,
				httpactionhandler);
	}

	/**
	 * 第三方支付 退货
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void thirdpaysalesreturn(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/paycenter/refund"), param, clz,
				httpactionhandler);
	}

	/**
	 * 第三方支付 查询
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void thirdpayquery(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/paycenter/query"), param, clz,
				httpactionhandler);
	}

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @param <T>
	 * @Description: TODO(登陆)
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void login(Map<String, String> param, final Class<T> clz, final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject("login", getUrl("xbapi/login"), param, clz,
				httpactionhandler);
	}

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(unlock)
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void unlock(Map<String, String> param, final Class<T> clz, final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject("unlock", getUrl("xbapi/unlock"), param, clz,
				httpactionhandler);
	}

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(获取会员信息)
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getmemberinfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetMemberInfo"), param, clz,
				httpactionhandler);
	}

	/**
	 * init pos info
	 * 
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void initialize(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetPosInitializeInfo"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 获取订单信息
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getOrderInfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetOldTicket"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 提交退货单
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void commitReturnOrder(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/saveArticles"), param, clz,
				httpactionhandler);
	}

	/**
	 * 提交商品
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void submitgoods(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/saveArticles"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 获取报表信息
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getReportInfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetReportData"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 获取会员所有信息
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getAllMemberInfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetMemberallInfo"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 保存退货单
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void saveReturnOrder(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/confirmBill"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 验证短息发送
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void sendSMSverify(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/requestverifycode"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 短信验证
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getSMSverify(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/checkoutverifycode"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 验证纸券是否可用
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getPaperCoupon(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/getpapercoupon"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 获取可用积分
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void calcutePointExchange(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/calcutePointExchange"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 获取支付方式
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getPaymentType(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/GetPay"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 获取优惠券打印信息
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getCouponPrintInfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/saveprintcoupon"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 发送优惠券信息
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void sendCouponInfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/sendecoupon"), param, clz,
				httpactionhandler);
	}

	/**
	 * @Description 获取小票格式
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void getTicketFormat(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpServiceStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/gettick"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 提交订单前的券验证
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void checkPaperCoupon(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/checkpapercoupon"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 发放停车券
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void manualParkCoupon(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/manualparkcoupon"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 会员激活
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void actiateMember(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/registermemeber"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 保存银行交易信息
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void saveBankInfo(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpServiceStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/savebankinfo"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 提交操作日志，错误日志
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void saveOperationLog(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpServiceStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/saveOperationLog"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 提交离线数据
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void uploadOfflineData(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpServiceStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/uploadofflinelist"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * @Description 提交离线数据
	 * @author zmm
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void uploadOfflineBankData(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpServiceStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/savebankinfolist"), param, clz,
				httpactionhandler);
	}
	
	/**
	 * 收款台监控
	 * @author so
	 * @param param
	 * @param clz
	 * @param httpactionhandler
	 */
	public <T> void monitorSKT(Map<String, String> param, final Class<T> clz,
			final HttpActionHandle<T> httpactionhandler) {
		HttpServiceStringClient.getinstance().getForObject(clz.getName(), getUrl("xbapi/monitorskt"), param, clz,
				httpactionhandler);
	}
	
}
