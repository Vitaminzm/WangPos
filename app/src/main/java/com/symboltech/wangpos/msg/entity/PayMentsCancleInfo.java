package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 支付方式撤销info
 * 
 * @author so
 * 
 */
public class PayMentsCancleInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id; // 收款方式ID
	private String name; // 收款方式名称
	private String money;
	private String overage;
	private String type;
	private String txnid;//收银通唯一ID
	private String des="撤销";//收银通唯一ID

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getTxnid() {
		return txnid;
	}

	public void setTxnid(String txnid) {
		this.txnid = txnid;
	}

	private Boolean isCancle;
	public Boolean getIsCancle() {
		return isCancle;
	}

	public void setIsCancle(Boolean isCancle) {
		this.isCancle = isCancle;
	}

	public ThirdPay getThridPay() {
		return thridPay;
	}

	public void setThridPay(ThirdPay thridPay) {
		this.thridPay = thridPay;
	}

	private ThirdPay thridPay;
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
