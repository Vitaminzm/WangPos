package com.symboltechshop.wangpos.utils;

/**
 * 支付方式相关操作
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 
 * @date 创建时间：2015年11月12日
 * @version 1.0
 */
public enum PaymentTypeEnum {

	CASH("1"), STORE("2"), BANK("3"), ALIPAY("4"), WECHAT("5"), COUPON("6"), ICBC_BANK("7"), ICBC_BUSY("8"), YXLM("9"), YIPAY("11"), BANK_CODE("10"), WEIPAY_BANK("12"), ALIPAY_BANK("13"), YIPAY_BANK("14"), YXLM_BANK("15"), LING("99999"), SCORE("106"), ALIPAYRECORDED("107"),HANDRECORDED("101"), STORERECORDED("102"), WECHATRECORDED("103") ,ERR("201");
	
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
