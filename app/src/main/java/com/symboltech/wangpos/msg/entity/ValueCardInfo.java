package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 储值卡信息
 * @author so
 *
 */
public class ValueCardInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private String cardno;
	private String money;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	
}
