package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 积分兑换信息
 * 
 * @author so
 * 
 */
public class ExchangemsgInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String usepoint;// 参与兑换积分
	private String exchangemoney;// 兑换金额
	
	public String getUsepoint() {
		return usepoint;
	}
	public void setUsepoint(String usepoint) {
		this.usepoint = usepoint;
	}
	public String getExchangemoney() {
		return exchangemoney;
	}
	public void setExchangemoney(String exchangemoney) {
		this.exchangemoney = exchangemoney;
	}
	
}
