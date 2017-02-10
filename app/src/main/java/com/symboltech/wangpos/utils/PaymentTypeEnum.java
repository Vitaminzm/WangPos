package com.symboltech.wangpos.utils;

/**
 * 支付方式相关操作
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 
 * @date 创建时间：2015年11月12日
 * @version 1.0
 */
public enum PaymentTypeEnum {

	CASH("0"), STORE("2"),SCORE("3"), BANK("7"), WECHAT("12"), ALIPAY("12"), COUPON("4"), LING("99999"), HANDRECORDED("101"), ALIPAYRECORDED("102"), WECHATRECORDED("103") ,RECORDED_CAREDUCTION("104"), ALLWANCE_COMPENSATION("105"), ERR("201");
	
	private String styletype;
	
	
	public static PaymentTypeEnum getpaymentstyle(String type){
		if(!StringUtil.isEmpty(type)){
			for (PaymentTypeEnum couponstyle : PaymentTypeEnum.values()) {
				if (!StringUtil.isEmpty(type) && couponstyle.getStyletype().equals(type)) {
					return couponstyle;
				}
			}
		}
		return ERR;
	}

	public static boolean isPaymentstyle(String type){
		if(!StringUtil.isEmpty(type)){
			for (PaymentTypeEnum couponstyle : PaymentTypeEnum.values()) {
				if (!StringUtil.isEmpty(type) && couponstyle.getStyletype().equals(type)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private PaymentTypeEnum(String styletype) {
		this.styletype = styletype;
	}

	public String getStyletype() {
		return styletype;
	}

	public void setStyletype(String styletype) {
		this.styletype = styletype;
	}
}
