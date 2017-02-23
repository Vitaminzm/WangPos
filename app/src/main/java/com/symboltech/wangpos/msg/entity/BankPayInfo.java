package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 银行支付信息
 * @author so
 *
 */
public class BankPayInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	private String transtype;
	private String cardno;
	private String bankcode;
	private String amount;
	private String decmoney;
	private String skfsid;
	private String des;
	private String tradeno;
	private String refno;

	public String getRefno() {
		return refno;
	}

	public void setRefno(String refno) {
		this.refno = refno;
	}

	public String getTradeno() {
		return tradeno;
	}

	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getSkfsid() {
		return skfsid;
	}
	public void setSkfsid(String skfsid) {
		this.skfsid = skfsid;
	}
	public String getTranstype() {
		return transtype;
	}
	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getBankcode() {
		return bankcode;
	}
	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDecmoney() {
		return decmoney;
	}
	public void setDecmoney(String decmoney) {
		this.decmoney = decmoney;
	}
	
	@Override
	public BankPayInfo clone() {
		BankPayInfo clone = null;
		try {
			clone = (BankPayInfo) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clone;
	}
	
}
