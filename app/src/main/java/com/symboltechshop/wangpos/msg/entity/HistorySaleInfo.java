package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 历史消费
 * 
 * @author so
 * 
 */
public class HistorySaleInfo implements Serializable {

	private String salemoney;// 销售金额
	private String saletime;// 销售时间
	private String couponmoney;//用券金额

	public String getSalemoney() {
		return salemoney;
	}

	public void setSalemoney(String salemoney) {
		this.salemoney = salemoney;
	}

	public String getSaletime() {
		return saletime;
	}

	public void setSaletime(String saletime) {
		this.saletime = saletime;
	}

	public String getCouponmoney() {
		return couponmoney;
	}

	public void setCouponmoney(String couponmoney) {
		this.couponmoney = couponmoney;
	}
	
}
