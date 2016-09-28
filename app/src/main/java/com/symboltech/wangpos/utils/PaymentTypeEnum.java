package com.symboltech.wangpos.utils;

/**
 * 支付方式相关操作
 * 
 * @author zmm Email:mingming.zhang@symboltech.com 
 * @date 创建时间：2015年11月12日
 * @version 1.0
 */
public enum PaymentTypeEnum {

	CASH("1"), STORE("2"), BANK("3"), WECHAT("5"), ALIPAY("4"), CARD("6"), ICBC_BANK("7"), ICBC_BUSY("8"), LING("99999"), SCORE("106"), HANDRECORDED("101"), ALIPAYRECORDED("102"), WECHATRECORDED("103") ,ERR("201");
	
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
