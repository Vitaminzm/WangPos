package com.symboltech.wangpos.app;

/**
 * 常量池 用于保存常量 simple introduction
 * 
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class ConstantData {

	/** 基数,用于配置常量参数 */
	public static final int BASE_CODE = 0x200;

	/** 更换pos状态*/
	public static final String UPDATE_STATUS = "update_status";
	/** 第一次进入 */
	public static final String LOGIN_FIRST = "login_first";

	/** 收银员登录 */
	public static final int LOGIN_WITH_CASHIER = BASE_CODE + 1;

	/** 收银员收银员锁屏 */
	public static final int LOGIN_WITH_LOCKSCREEN = BASE_CODE + 2;

	/** 二维码验证resultcode */
	public static final int QRCODE_RESULT_MEMBER_VERIFY = BASE_CODE + 3;

	/** 二维码验证requestcode by 会员验证 */
	public static final int QRCODE_REQURST_MEMBER_VERIFY = BASE_CODE + 4;

	/** 二维码验证requestcode by 扫码支付 */
	public static final int QRCODE_REQURST_QR_PAY = BASE_CODE + 5;

	/** 第三方支付 */
	public static final int THIRD_OPERATION_PAY = BASE_CODE + 6;

	/** 第三方支付撤销 */
	public static final int THIRD_OPERATION_CANCEL = BASE_CODE + 7;

	/** 第三方支付查询 */
	public static final int THIRD_OPERATION_QUERY = BASE_CODE + 8;

	/** 第三方支付退货 */
	public static final int THIRD_OPERATION_SALES_RETURN = BASE_CODE + 9;

	/** 会员 */
	public static final int MEMBER_IS_VERITY = BASE_CODE + 10;

	/** 非会员 */
	public static final int MEMBER_IS_NOT_VERITY = BASE_CODE + 11;

	/** 直接结账进入 */
	public static final int ENTER_CASHIER_BY_ACCOUNTS = BASE_CODE + 12;

	/** 会员详情进入 */
	public static final int ENTER_CASHIER_BY_MEMBER = BASE_CODE + 13;

	/** 积分详情进入 */
	public static final int ENTER_CASHIER_BY_INTEGRAL = BASE_CODE + 14;

	/** 购物车进入积分页面request */
	public static final int REQUEST_BY_ENTER_INTEGRAL = BASE_CODE + 15;

	/** 购物车进入积分页面RESULT */
	public static final int RESULT_BY_ENTER_INTEGRAL = BASE_CODE + 16;

	/** 扫纸券 */
	public static final int SCAN_CASH_COUPON = BASE_CODE + 17;

	/** 会员验证不验证 */
	public static final int VERIFY_MEMBER_NO = BASE_CODE + 18;

	/** 会员验证SMS */
	public static final int VERIFY_MEMBER_BY_SMS = BASE_CODE + 19;

	/** 会员验证qr */
	public static final int VERIFY_MEMBER_BY_QR = BASE_CODE + 20;

	/** 二维码验证requestcode by 会员验证 */
	public static final int QRCODE_REQURST_MEMBER_VERIFY_SECOND = BASE_CODE + 21;

	/**启动会员请求码*/
	public static final int BOOT_MEMBER_REQUEST_CODE = BASE_CODE + 22;

	/**会员详情响应码*/
	public static final int MEMBER_RESULT_CODE = BASE_CODE + 23;

	/**离线数据上传成功*/
	public static final int UPLOAD_SUCCESS = BASE_CODE + 24;

	/**离线数据上传中*/
	public static final int UPLOAD_ING = BASE_CODE + 25;

	/**非定价商品*/
	public static final int GOOD_PRICE_CAN_CHANGE = BASE_CODE + 26;

	/**定价商品*/
	public static final int GOOD_PRICE_NO_CHANGE = BASE_CODE + 27;

	/** 预备会员 */
	public static final int MEMBER_IS_PREPARE_VERITY = BASE_CODE + 28;

	/**在线状态*/
	public static final int ONLINE_STATE = 0;

	/**离线状态*/
	public static final int OFFLINE_STATE = 1;

	/**更换状态码*/
	public static final int CHANGE_MODE_STATE = 2;

	/** login进入key */
	public static final String LOGIN_WITH_CHOOSE_KEY = "login_with_choose_key";

	/** http 请求成功 */
	public static final String HTTP_RESPONSE_OK = "00";

	/** http 上传日志部分成功 */
	public static final String HTTP_RESPONSE_PART_OK = "02";

	/** http 请求成功并添加会员 */
	public static final String HTTP_RESPONSE_OK_ADD_MEMBER = "99";

	/** http 请求支付等待 */
	public static final String HTTP_RESPONSE_THIRDPAY_WAIT = "98";

	/** 款台号 */
	public static final String CASHIER_DESK_CODE = "cashier_desk_code";

	/** 当前小票号 */
	public static final String RECEIPT_NUMBER = "receipt_number";

	/** 当前小票号 */
	public static final String RECEIPT_NUMBER_LAST = "receipt_number_last";

	/** 门店ID */
	public static final String MALL_ID = "mall_id";

	/** 门店代码 */
	public static final String MALL_CODE = "mall_code";

	/** 门店名称 */
	public static final String MALL_NAME = "mall_name";

	/** 店铺ID */
	public static final String SHOP_ID = "shop_id";

	/** 店铺代码 */
	public static final String SHOP_CODE = "shop_code";

	/** 店铺名称 */
	public static final String SHOP_NAME = "shop_name";

	/** 舍零方式 */
	public static final String MALL_MONEY_OMIT = "mall_money_omit";

	/** 支付宝是否输入单号 */
	public static final String MALL_ALIPAY_IS_INPUT = "mall_alipay_is_input";

	/** 微信是否输入单号 */
	public static final String MALL_WEIXIN_IS_INPUT = "mall_weixin_is_input";

	/** 收银员姓名 */
	public static final String CASHIER_NAME = "cashier_name";

	/** 收银员ID */
	public static final String CASHIER_ID = "cashier_id";

	/** 收银员代码 */
	public static final String CASHIER_CODE = "cashier_code";

	/** save token */
	public static final String LOGIN_TOKEN = "token";

	/** pos更新时间 */
	public static final String POS_UPDATE_TIME = "pos_update_time";

	/** 商品信息 */
	public static final String BRANDGOODSLIST = "brand_goods_list";

	/** 支付方式信息 */
	public static final String PAYMENTSLIST = "payments_list";

	/** 促销 信息 */
	public static final String PROMOTIONINFOLIST = "promotion_info_list";

	/** 退货原因信息 */
	public static final String REFUNDREASONLIST = "refund_reason_list";

	/** 营业员信息 */
	public static final String SALEMANLIST = "saleman_list";

	/** 交易类型 0-消费 */
	public static final String SALETYPE_SALE = "0";

	/** 交易类型 1-选单退货 */
	public static final String SALETYPE_SALE_RETURN_ORDER = "1";

	/** 交易类型 2-普通退货 */
	public static final String SALETYPE_SALE_RETURN_NORMAL = "2";

	/** 订单 */
	public static final String BILL = "bill";

	/** 订单号 */
	public static final String BILLID = "billId";

	/** 退款总金额 */
	public static final String TOTAL_RETURN_MONEY = "TotalReturnMoney";

	/** 会员 */
	public static final String MEMBER = "member";

	/** 会员id */
	public static final String MEMBER_ID = "member_id";

	/** 是否download配置信息 登录之后允许成功download一次基本配置信息，如果登录之后更新配置，需要重新登录 */
	public static final String IS_CONFIG_DOWNLOAD = "is_config_download";

	/** 阿里巴巴支付渠道 */
	public static final int PAYMODE_BY_ALIPAY = 1;

	/** 微信支付渠道 */
	public static final int PAYMODE_BY_WEIXIN = 3;

	/** 第三方需要输入单号 */
	public static final String THIRD_NEED_INPUT = "0";

	/** 第三方不需要输入单号 */
	public static final String THIRD_NOT_INPUT = "1";

	/** 0品牌商品 */
	public static final int GOODS_SOURCE_BY_BRAND = 0;

	/** 1积分商品 */
	public static final int GOODS_SOURCE_BY_INTEGRAL = 1;

	/** 是否是会员 */
	public static final String VERIFY_IS_MEMBER = "ismember";

	/** 查看会员信息 */
	public static final String GET_MEMBER_INFO = "get_member_info";

	/** 订单总额 */
	public static final String GET_ORDER_VALUE_INFO = "get_order_value_info";

	/** 可用纸券 */
	public static final String GET_ORDER_CARD_INFO = "get_order_cash_info";

	/** 可用纸券溢余 */
	public static final String GET_ORDER_CARD_OVERAGE = "get_order_cash_overage";

	/** 可用积分 */
	public static final String GET_ORDER_SCORE_INFO = "get_order_score_info";

	/** 可用积分溢余 */
	public static final String GET_ORDER_SCORE_OVERAGE = "get_order_score_overage";

	/** 查看优惠券信息 */
	public static final String GET_COUPON_INFO = "get_coupon_info";

	/** 品牌商品数据信息 */
	public static final String GOODS_BRAND_LISTS = "good_brand_lists";

	/** 积分商品数据信息 */
	public static final String GOOD_INTEGRAL_LISTS = "good_integral_lists";

	/** 会员详情所有信息 */
	public static final String ALLMEMBERINFO = "all_member_info";

	/** 进入收银页面途径 */
	public static final String ENTER_CASHIER_WAY_FLAG = "enter_cashier_way_flag";

	/** 保存交易结果数据 */
	public static final String SAVE_ORDER_RESULT_INFO = "save_order_result_info";

	/** 进入收银页面途径 积分 */
	public static final String ENTER_INTEGRAL_WAY_FLAG = "enter_integral_way_flag";

	/** 积分使用量 */
	public static final String USE_INTERRAL = "use_integral";

	/** 购物车里商品 */
	public static final String CART_HAVE_GOODS = "cart_have_goods";

	/** 可用券信息 */
	public static final String CAN_USED_COUPON = "can_used_coupon";

	/** 购物车所有金额 */
	public static final String CART_ALL_MONEY = "cart_all_money";

	/** 购物单详情 */
	public static final String ORDER_INFO = "order_info";

	/** 银行卡详情 */
	public static final String BANK_INFO = "bank_info";

	/** 脱机模式标记 */
	public static final String OFFLINE_MODE = "offline_mode";

	/** 脱机模式信息*/
	public static final String OFFLINE_MODE_INFO = "offline_mode_info";
	/** 会员是否是二次验证会员 */
	public static final String MEMBER_IS_SECOND_VERIFY = "member_is_second_verify";

	/** 会员验证 方式  */
	public static final String MEMBER_VERIFY= "member_type";

	/** 会员验证 方式 磁卡RFID */
	public static final String MEMBER_VERIFY_BY_MAGCARD = "0";

	/** 会员验证 方式 会员卡 */
	public static final String MEMBER_VERIFY_BY_MEMBERCARD = "1";

	/** 会员验证 方式手机号*/
	public static final String MEMBER_VERIFY_BY_PHONE = "2";

	/** 会员验证 方式 二维码 */
	public static final String MEMBER_VERIFY_BY_QR = "3";

	/** IP_HOST_CONFIG 配置host*/
	public static final String IP_HOST_CONFIG = "ip_host_config";

	/** IP_HOST_CONFIG 配置host前缀*/
	public static final String IP_HOST_CONFIG_PREFIX = "http://";

	/** salesman name */
	public static final String SALESMAN_NAME = "salesman";

	/** salesman name */
	public static final String SALESMAN_ID = "salesman_id";

	/**启动会员的源头*/
	public static final String BOOT_MEMBER_SOURCE = "boot_member_source";

	/** 开始上传日志*/
	public static final String UPLOAD_LOG = "upload_log";

	/** 手动监测网络*/
	public static final String CHECK_NET = "check_net";

	/** 上传脱机数据*/
	public static final String UPLOAD_OFFLINE_DATA = "upload_offline_data";

	/**pos 状态   登出*/
	public static final String POS_STATUS_LOGOUT = "-1";

	/**pos 状态   锁屏*/
	public static final String POS_STATUS_LOCK = "-2";

	/**会员验证信息发送状态*/
	public static final String MEMBER_VAILIDATE_STATUS = "member_vailidate_result";

	/**会员验证信息返回值*/
	public static final String MEMBER_VAILIDATE_RESULT = "member_vailidate_result";

	/**离线回调对象*/
	public static final String HTTPACTIONHANDLE = "httpactionhandle";

	/**是否切换到在线状态*/
	public static final String CHANGE_ONLINE_MODE = "change_online_mode";

	/** 小票格式信息 */
	public static final String TICKET_FORMAT_LIST = "ticket_format_list";

	/**查询报表类型 - 销售*/
	public static final int LOG_SALE = 1;

	/**查询报表类型 - 退货*/
	public static final int LOG_RETURN = 0;

	/**查询报表类型 - 全部*/
	public static final int LOG_ALL = -1;
}
