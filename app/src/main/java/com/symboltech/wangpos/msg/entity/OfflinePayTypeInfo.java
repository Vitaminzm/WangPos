package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 离线支付方式info
 * 
 * @author so
 * 
 */
public class OfflinePayTypeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id; // 收款方式ID
	private String name; // 收款方式名称
	private String money;
	private String overage;
	private String type;

	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getOverage() {
		return overage;
	}

	public void setOverage(String overage) {
		this.overage = overage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
