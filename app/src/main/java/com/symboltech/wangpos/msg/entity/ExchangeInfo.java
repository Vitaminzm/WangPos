package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 积分兑换信息
 * 
 * @author so
 * 
 */
public class ExchangeInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String exchangepoint;// 参与兑换积分
	private String exchangemoney;// 兑换金额
	public String getExchangepoint() {
		return exchangepoint;
	}
	public void setExchangepoint(String exchangepoint) {
		this.exchangepoint = exchangepoint;
	}
	public String getExchangemoney() {
		return exchangemoney;
	}
	public void setExchangemoney(String exchangemoney) {
		this.exchangemoney = exchangemoney;
	}
	
}
