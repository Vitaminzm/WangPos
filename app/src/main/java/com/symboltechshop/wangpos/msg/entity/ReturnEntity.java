package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退款信息实体
 * @author so
 *
 */
public class ReturnEntity implements Serializable{

	/**退款方式id*/
	private String id;
	/**退款金额*/
	private BigDecimal money;
	public ReturnEntity(String id, BigDecimal money) {
		super();
		this.id = id;
		this.money = money;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public BigDecimal getMoney() {
		return money;
	}
	public void setMoney(BigDecimal money) {
		this.money = money;
	}
	
}
