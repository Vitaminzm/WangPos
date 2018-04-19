package com.symboltech.wangpos.utils;

/**
 * 支付方式相关操作
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 
 * @date 创建时间：2015年11月12日
 * @version 1.0
 */
public enum TicketFormatEnum {

	//--小票头 尾

	/** 收款员系统码*/
	TICKET_CASHER_CODE("\\[收款员系统码\\]"),
	/** 销售员系统码*/
	TICKET_SALE_CODE("\\[销售员系统码\\]"),
	/** 店铺代码*/
	TICKET_AUTH_CODE("\\[验证码\\]"),
	/** 店铺代码*/
	TICKET_SHOP_CODE("\\[店铺代码\\]"),
	/** 店铺名称*/
	TICKET_SHOP_NAME("\\[店铺名称\\]"),
	/** 门店名称*/
	TICKET_MALL_NAME("\\[门店名称\\]"),
	/** 订单号*/
	TICKET_BILL_NO("\\[订单号\\]"),
	/** 收款台号*/
	TICKET_DESK_CODE("\\[收款台号\\]"),
	/** 收款员代码*/
	TICKET_CASHIER_CODE("\\[收款员代码\\]"),
	/** 收款员姓名*/
	TICKET_CASHIER_NAME("\\[收款员姓名\\]"),
	/** 销售员代码*/
	TICKET_SALEMAN_CODE("\\[销售员代码\\]"),
	/** 销售员名称*/
	TICKET_SALEMAN_NAME("\\[销售员姓名\\]"),
	/** 交易日期*/
	TICKET_SALE_DATE("\\[交易日期\\]"),
	/** 交易时间*/
	TICKET_SALE_TIME("\\[交易时间\\]"),
	/** 网址*/
	TICKET_WEB("\\[网址\\]"),
	/** 服务热线*/
	TICKET_HOT_LINE("\\[服务热线\\]"),
	/** 分割行*/
	TICKET_LINE("\\[分割行\\]"),
	/** 普通分割行*/
	TICKET_NORMAL_LINE("\\[普通分割行\\]"),
	/** 补打小票分割行*/
	TICKET_BUDALINE("\\[补打小票分割行\\]"),
	/** 补打人姓名*/
	TICKET_BUDASALEMAN("\\[补打人姓名\\]"),
	/** 补打人代码*/
	TICKET_BUDASALEMANCODE("\\[补打人代码\\]"),
	/** 补打时间*/
	TICKET_BUDATIME("\\[补打时间\\]"),
	/** 补打日期*/
	TICKET_BUDADATE("\\[补打日期\\]"),
	/** 换行*/
	TICKET_ENTER("\\[换行\\]"),
	/** 活动信息开始*/
	TICKET_ACTIVITY_BEGIN("\\[活动信息开始\\]"),
	/** 活动信息结束*/
	TICKET_ACTIVITY_END("\\[活动信息结束\\]"),
	/** 会员信息开始*/
	TICKET_MEMBER_BEGIN("\\[会员信息开始\\]"),
	/** 会员信息结束*/
	TICKET_MEMBER_END("\\[会员信息结束\\]"),

	//--------商品信息------
	/** 商品代码*/
	TICKET_GOOD_CODE("\\[商品代码\\]"),
	/** 商品名称*/
	TICKET_GOOD_NAME("\\[商品名称\\]"),
	/** 数量*/
	TICKET_COUNT("\\[数量\\]"),
	/** 金额*/
	TICKET_MONEY("\\[金额\\]"),
	/** 消费积分*/
	TICKET_USED_SCORE("\\[消耗积分\\]"),
	/** 新增积分*/
	TICKET_ADD_SCORE("\\[新增积分\\]"),
	/** 商品明细开始*/
	TICKET_GOOD_BEGIN("\\[商品明细开始\\]"),
	/** 商品明细结束*/
	TICKET_GOOD_END("\\[商品明细结束\\]"),
	
	//------------金额信息--------
	/** 总数量*/
	TICKET_TOTAL_COUNT("\\[总数量\\]"),
	/** 总金额*/
	TICKET_TOTAL_MONEY("\\[总金额\\]"),
	/** 总消费积分*/
	TICKET_TOTAL_USED_SCORE("\\[总消耗积分\\]"),
	/** 总新增积分*/
	TICKET_TOTAL_ADD_SCORE("\\[总新增积分\\]"),
	/** 满减金额*/
	TICKET_MANJIAN_MONEY("\\[满减金额\\]"),
	/** 代金券名称*/
	TICKET_CASH_COUPON_NAME("\\[代金券名称\\]"),
	/** 代金券金额*/
	TICKET_CASH_COUPON_MONEY("\\[代金券金额\\]"),
	/** 积分抵扣名称*/
	TICKET_DUDUC_SOCRE_NAME("\\[积分抵扣名称\\]"),
	/** 积分抵扣金额*/
	TICKET_DUDUC_SOCRE_MONEY("\\[积分抵扣金额\\]"),
	/** 应付金额*/
	TICKET_DEAL_MONEY("\\[应付金额\\]"),
	/** 实付金额*/
	TICKET_REAL_MONEY("\\[实付金额\\]"),
	/** 收款方式名称*/
	TICKET_PAYTYPE_NAME("\\[收款方式名称\\]"),
	/** 收款方式金额*/
	TICKET_PAYTYPE_MONEY("\\[收款方式金额\\]"),
	/** 找零金额*/
	TICKET_EXCHANGE("\\[找零金额\\]"),
	/** 代金券明细开始*/
	TICKET_CASH_COUPON_BEGIN("\\[代金券明细开始\\]"),
	/** 代金券明细结束*/
	TICKET_CASH_COUPON_END("\\[代金券明细结束\\]"),
	/** 收款方式明细开始*/
	TICKET_PAYTYPE_BEGIN("\\[收款方式明细开始\\]"),
	/** 收款方式明细结束*/
	TICKET_PAYTYPE_END("\\[收款方式明细结束\\]"),

	/** 待返券明细开始*/
	TICKET_DFQ_COUPON_BEGIN("\\[待返券开始\\]"),
	/** 待返券明细结束*/
	TICKET_DFQ_COUPON_END("\\[待返券结束\\]"),
	/** 待返券明细结束*/
	TICKET_DFQ_COUPON_NAME("\\[返券规则\\]"),
	/** 待返券明细结束*/
	TICKET_DFQ_COUPON_MONEY("\\[待返券金额\\]"),
	//---------------会员信息--------------------------------------

	/** 会员卡号*/
	TICKET_MEMBER_NO("\\[会员卡号\\]"),
	/** 会员姓名*/
	TICKET_MEMBER_NAME("\\[会员姓名\\]"),
	/** 会员手机号码*/
	TICKET_MEMBER_TEL("\\[会员手机号码\\]"),
	/** 会员卡类型*/
	TICKET_MEMBER_TYPE("\\[会员卡类型\\]"),
	/** 累计积分*/
	TICKET_TOTAL_SCORE("\\[累计积分\\]"),

	//----------------银行卡信息-------------------------------------------------

	/** 银行卡号*/
	TICKET_BANK_NO("\\[银行卡号\\]"),
	/** 银行名称*/
	TICKET_BANK_NAME("\\[银行名称\\]"),
	/** 消费金额*/
	TICKET_USED_MONEY("\\[消费金额\\]"),
	/** 签购单号*/
	TICKET_QIANGOUDAN("\\[签购单号\\]"),
	
	//----------------停车券发放信息-------------------------------------------------
	/** 停车券时长*/
	TICKET_PARK_COUPON_HOUR("\\[停车时长\\]"),
	/** 停车券发放车牌*/
	TICKET_PARK_COUPON_NO("\\[车牌号\\]"),
		
	//----------------优惠券发放信息-------------------------------------------------
	/** 优惠券金额*/
	TICKET_COUPON_MONEY("\\[优惠券金额\\]"),
	/** 优惠券名称*/
	TICKET_COUPON_NAME("\\[优惠券名称\\]"),
	/** 优惠券余额*/
	TICKET_COUPON_YUE("\\[优惠券余额\\]"),
	/** 结束日期*/
	TICKET_COUPON_ENDDATE("\\[结束日期\\]"),
	/** 优惠券明细开始*/
	TICKET_COUPON_BEGIN("\\[优惠券明细开始\\]"),
	/** 优惠券明细结束*/
	TICKET_COUPON_END("\\[优惠券明细结束\\]"),
	/** 退货原因*/
	TICKET_RETURN_REASON("\\[退货原因\\]"),
	/** 退货积分*/
	TICKET_RETURN_GOOD_SCORE("\\[退货积分\\]"),
	/** 总退货积分*/
	TICKET_TOTAL_RETURN_GOOD_SCORE("\\[总退货积分\\]"),
	/** 应退金额*/
	TICKET_RETURN_DEAL_MONEY("\\[应退金额\\]"),
	/** 实退金额*/
	TICKET_RETURN_REAL_MONEY("\\[实退金额\\]"),
	/** 退款方式明细开始*/
	TICKET_RETURN_PAYTYPE_BEGIN("\\[退款方式明细开始\\]"),
	/** 退款方式明细结束*/
	TICKET_RETURN_PAYTYPE_END("\\[退款方式明细结束\\]"),
	/** 退款方式名称*/
	TICKET_RETURN_PAYTYPE_NAME("\\[退款方式名称\\]"),
	/** 退款方式金额*/
	TICKET_RETURN_PAYTYPE_MONEY("\\[退款方式金额\\]"),
	/** 退回积分*/
	TICKET_RETURN_SCORE("\\[退回积分\\]"),
	/** 退款金额*/
	TICKET_RETURN_MONEY("\\[退款金额\\]"),
	/** 打印日期*/
	TICKET_PRINT_DATE("\\[打印日期\\]"),
	/** 打印时间*/
	TICKET_PRINT_TIME("\\[打印时间\\]"),
	
	
	/** 班报日报---------------------*/
	/** 收款总金额*/
	TICKET_GET_TOTAL_MONEY("\\[收款总金额\\]"),
	/** 收款总笔数*/
	TICKET_GET_TOTAL_COUNT("\\[收款总笔数\\]"),
	/** 退款总金额*/
	TICKET_RETURN_TOTAL_MONEY("\\[退款总金额\\]"),
	/** 退货原因*/
	TICKET_RETURN_TOTAL_COUNT("\\[退款总笔数\\]"),
	/** 合计金额*/
	TICKET_TOTAL_ADD_MONEY("\\[合计金额\\]"),
	/** 合计笔数*/
	TICKET_TOTAL_ADD_COUNT("\\[合计笔数\\]"),
	/** 收款明细开始*/
	TICKET_PAY_BEGIN("\\[收款明细开始\\]"),
	/** 收款明细结束*/
	TICKET_PAY_END("\\[收款明细结束\\]"),
	/** 退款明细开始*/
	TICKET_RETURN_BEGIN("\\[退款明细开始\\]"),
	/** 退款明细开始*/
	TICKET_RETURN_END("\\[退款明细结束\\]"),
	/** 合计明细开始*/
	TICKET_TOTAL_BEGIN("\\[合计明细开始\\]"),
	/** 合计明细结束*/
	TICKET_TOTAL_END("\\[合计明细结束\\]"),
	/** 错误*/
	ERROR("404");
	
	private String lable;
	
	public static TicketFormatEnum getLable(String type){
		if(!StringUtil.isEmpty(type)){
			for (TicketFormatEnum couponstyle : TicketFormatEnum.values()) {
				if (!StringUtil.isEmpty(type) && couponstyle.getLable().equals(type)) {
					return couponstyle;
				}
			}
		}
		return ERROR;
	}
	
	private TicketFormatEnum(String lable) {
		this.lable = lable;
	}

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}
}
