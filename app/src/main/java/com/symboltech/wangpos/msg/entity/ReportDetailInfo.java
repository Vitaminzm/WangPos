package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 报表详情
 * @author so
 *
 */
public class ReportDetailInfo implements Serializable{

	private String code;
	private String name;
	private String money;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	
	
}
