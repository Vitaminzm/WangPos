package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 历史总消费
 * 
 * @author so
 * 
 */
public class HistoryAllSaleInfo implements Serializable {

	private String salemoney;// 消费总计
	private String saleamount;// 消费次数
	private String avge;//客单价
	public String getSalemoney() {
		return salemoney;
	}
	public void setSalemoney(String salemoney) {
		this.salemoney = salemoney;
	}
	public String getSaleamount() {
		return saleamount;
	}
	public void setSaleamount(String saleamount) {
		this.saleamount = saleamount;
	}
	public String getAvge() {
		return avge;
	}
	public void setAvge(String avge) {
		this.avge = avge;
	}

}
