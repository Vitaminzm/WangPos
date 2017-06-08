package com.symboltech.wangpos.utils;

/**
 * 支付方式相关操作
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 
 * @date 创建时间：2015年11月12日
 * @version 1.0
 */
public enum OptLogEnum {

	/** 登录---------------------*/
	/** 登录*/
	LOGIN_OPT("100"),
	/** 登录成功*/
	LOGIN_SUCCESS_OPT("101"),
	/** 登录失败*/
	LOGIN_FAILED_OPT("102"),
	/** 查看版本操作码*/
	LOGIN_VERSION_OPT("103"),
	/** 退出系统*/
	LOGOUT_OPT("104"),
	
	/** 解锁---------------------*/
	/** 解锁*/
	UNLOCK_OPT("200"),
	/** 解锁成功*/
	UNLOCK_SUCCESS_OPT("201"),
	/** 解锁失败*/
	UNLOCK_FAILED_OPT("202"),
	
	/** 班报日报---------------------*/
	/** 班报日报返回*/
	REPORT_DAY_JOB_RETURN_OPT("300"),
	/** 日报打印*/
	REPORT_DAY_PRINT_OPT("301"),
	/** 班报打印*/
	REPORT_JOB_PRINT_OPT("302"),
	
	/** 脱机模式切换---------------------*/
	/** 切换脱机模式*/
	OFFLINE_IN_OPT("401"),
	/** 切换脱机模式*/
	OFFLINE_IN_CANCLEOPT("402"),
	/** 切换联网模式*/
	OFFLINE_OUT_OPT("403"),
	/** 切换联网模式*/
	OFFLINE_OUT_CANCLEOPT("404"),
	
	/** 会员---------------------*/
	MEMBER_VERIFY_TEL("500"),
	MEMBER_VERIFY_NO("501"),
	MEMBER_VERIFY_BANK("502"),
	MEMBER_VERIFY_QRCODE("503"),
	MEMBER_VERIFY_SUCCCESS("504"),
	MEMBER_VERIFY_FAILED("505"),
	MEMBER_ADD_SUCCESS("506"),
	
	REPORT_JOB_OPT("507"),
	REPORT_DAY_OPT("508"),
	/** 切换操作员操作码*/
	CHANGE_OPT("509"),
	LOCK_OPT("510"),
	
	PRINT_SUCCESS("511"),
	PRINT_FAILED("512"),
	PRINT_CONFIRM("513"),
	PRINT_CANCLE("514"),
	
	BANK_TRADE("520"),
	BANK_TRADE_SUCCESS("521"),
	BANK_TRADE_FAILED("522"),
	BANK_TRADE_CANCLE("523"),
	BANK_RETURN("524"),
	BANK_RETURN_SUCCESS("525"),
	BANK_RETURN_FAILED("526"),
	BANK_RETURN_CANCLE("527"),
	BANK_REPEAL("528"),
	BANK_REPEAL_SUCCESS("529"),
	BANK_REPEAL_FAILED("530"),
	BANK_REPEAL_CANCLE("531"),
	BANK_SIGN("532"),
	BANK_SIGN_SUCCESS("533"),
	BANK_SIGN_FAILED("534"),
	BANK_SIGN_CANCLE("535"),
	BANK_PRINT("536"),
	BANK_PRINT_SUCCESS("537"),
	BANK_PRINT_FAILED("538"),
	BANK_PRINT_CANCLE("539"),
	BANK_CLEAR("540"),
	BANK_CLEAR_SUCCESS("541"),
	BANK_CLEAR_FAILED("542"),
	BANK_CLEAR_CANCLE("543"),
	
	ALIPAY_TRADE("550"),
	ALIPAY_TRADE_SUCCESS("551"),
	ALIPAY_TRADE_FAILED("552"),
	ALIPAY_TRADE_CANCLE("553"),
	ALIPAY_QUERY("554"),
	ALIPAY_QUERY_SUCCESS("555"),
	ALIPAY_QUERY_FAILED("556"),
	ALIPAY_QUERY_CANCLE("557"),
	ALIPAY_REPEAL("558"),
	ALIPAY_REPEAL_SUCCESS("559"),
	ALIPAY_REPEAL_FAILED("560"),
	ALIPAY_REPEAL_CANCLE("561"),
	ALIPAY_RETURN("562"),
	ALIPAY_RETURN_SUCCESS("563"),
	ALIPAY_RETURN_FAILED("564"),
	ALIPAY_RETURN_CANCLE("565"),
	
	WECHAT_TRADE("570"),
	WECHAT_TRADE_SUCCESS("571"),
	WECHAT_TRADE_FAILED("572"),
	WECHAT_TRADE_CANCLE("573"),
	WECHAT_QUERY("574"),
	WECHAT_QUERY_SUCCESS("575"),
	WECHAT_QUERY_FAILED("576"),
	WECHAT_QUERY_CANCLE("577"),
	WECHAT_REPEAL("578"),
	WECHAT_REPEAL_SUCCESS("579"),
	WECHAT_REPEAL_FAILED("580"),
	WECHAT_REPEAL_CANCLE("581"),
	WECHAT_RETURN("582"),
	WECHAT_RETURN_SUCCESS("583"),
	WECHAT_RETURN_FAILED("584"),
	WECHAT_RETURN_CANCLE("585"),
	
	SALE_GOOD_OPT("590"),
	RETURN_GOOD_NORMAL_OPT("591"),
	RETURN_GOOD_ORDER_OPT("592"),
	
	/** 上传脱机数据操作码*/
	UPLOAD_OPT("593"),
	OFFLINE_SALE_OPT("594"),
	OFFLINE_RETURN_OPT("595"),
	
	/** 会员详情---------------------*/
	MEMBER_BACK_OPT("600"),
	MEMBER_RETURN_GOOD_OPT("601"),
	MEMBER_SEND_CODE_OPT("602"),
	MEMBER_SALE_GOOD_OPT("603"),
	MEMBER_LOOK_SCORE_GOOD_OPT("604"),
	MEMBER_ADD_SCORE_GOOD_OPT("605"),
	
	/** 收银---------------------*/
	SALE_REPEAL_ORDER("700"),
	SALE_ADD_SALERMAN("701"),
	SALE_LOOK_MEMBER("702"),
	SALE_ADD_MEMBER("703"),
	SALE_ADD_SCORE_GOOD("704"),
	SALE_INPUT_MONEY("705"),
	SALE_SELECT_GOOD("706"),
	SALE_CONFIRM_BILL("707"),
	SALE_VERIFY_BILL("708"),
	SALE_NO_VERIFY_BILL("709"),
	SALE_VERIFY_CODE("710"),
	SALE_VERIFY_CODE_AGAIN("711"),
	SALE_VERIFY_SUCCCESS("712"),
	SALE_VERIFY_FAILED("713"),
	
	/** 支付*/
	PAY_USED_COUPON("800"),
	PAY_USED_COUPON_SUCCESS("801"),
	PAY_USED_COUPON_FAILED("802"),
	PAY_USED_SCORE("803"),
	PAY_USED_SCORE_SUCCESS("804"),
	PAY_USED_SCORE_FAILED("805"),
	PAY_SCANE_COUPON("806"),
	PAY_SCANE_COUPON_SUCCESS("807"),
	PAY_SCANE_COUPON_FAILED("808"),
	PAY_BACK_GOOD_ORDER("809"),
	PAY_GO_PAY("810"),
	PAY_BACK_USECOUPON("811"),
	PAY_CANCLE_PAY("812"),
	PAY_CANCLE_PAY_ONE("813"),
	PAY_CANCLE_PAY_SUCCESS("814"),
	PAY_CANCLE_PAY_FAILED("815"),
	PAY_CANCLE_PAY_CONFIRM("816"),
	PAY_CANCLE_PAY_CANCLE("817"),
	
	PAY_BU_PRINT_BILL("820"),
	PAY_BU_PRINT_SLIP("821"),
	PAY_PRINT_COUPON("822"),
	PAY_FINISH("823"),
	
	/** 退货*/
	RETURN_CANCLE_ORDER("900"),
	RETURN_LOOK_MEMBER("901"),
	RETURN_ADD_SALEMAN("902"),
	RETURN_ADD_GOODS("903"),
	RETURN_ADD_MEMBER("904"),
	RETURN_REASON("905"),
	RETURN_BACKTO_ORDER("906"),
	RETURN_ADD_PAYTYPE("907"),
	RETURN_DEL_PAYTYPE("908"),
	RETURN_CONFIRM_ORDER("909"),
	RETURN_SUBMIT_ORDER("910"),
	
	RETURN_SELECT_ORDER("920"),
	RETURN_SELECT_CANCLE_ORDER("921"),
	RETURN_SELECT_LOOK_MEMBER("922"),
	RETURN_SELECT_CONFIRM_ORDER("923"),
	RETURN_SELECT_BACK_ORDER("924"),
	RETURN_SELECT_REASON("925"),
	RETURN_SELECT_ADD_INDEMNITY("926"),
	RETURN_SELECT_SUBMIT_ORDER("927"),
	RETURN_SELECT_BU_PRINT_BILL("928"),
	RETURN_SELECT_BU_PRINT_SLIP("929"),
	RETURN_SELECT_BU_PRINT_COUPON("930"),
	RETURN_SELECT_FINISH("931"),
	
	SEND_PACK_COUPON("940"),
	
	
	SEND_OFFLINE_DATA("1000"),
	
	OFFLINE_DATA("1001"),
	
	OFFLINE_BANK_DATA("1002"),
	/** 出错日志操作码*/
	ERROR_OPT("997"),
	
	/** 异常日志操作码*/
	EXCEPTION_OPT("998");
	
	private String optLogCode;
	
	private OptLogEnum(String optLogCode) {
		this.optLogCode = optLogCode;
	}

	public String getOptLogCode() {
		return optLogCode;
	}

	public void setOptLogCode(String optLogCode) {
		this.optLogCode = optLogCode;
	}
}