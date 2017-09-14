package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 车牌信息
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class ZklInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	private String hyzkmoney;
	private String money;
	private String oldmoney;

	public String getHyzkmoney() {
		return hyzkmoney;
	}

	public void setHyzkmoney(String hyzkmoney) {
		this.hyzkmoney = hyzkmoney;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getOldmoney() {
		return oldmoney;
	}

	public void setOldmoney(String oldmoney) {
		this.oldmoney = oldmoney;
	}
}
