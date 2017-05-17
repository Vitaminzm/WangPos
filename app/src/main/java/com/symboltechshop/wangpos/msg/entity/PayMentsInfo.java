package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 支付方式info
 * 
 * @author so
 * 
 */
public class PayMentsInfo implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id; // 收款方式ID
	private String name; // 收款方式名称
	private String couponid; // 对应的券ID（非券收款方式值为-1）
	private String changetype; // 找零收款方式 （0不处理 1不能多收 2多收不找零 ）
	private String rate; // 汇率（如美元）
	private String money;
	private String overage;
	private String type;
	private String visibled;//0 可见 1 不可见
	private String des;//描述 客户端使用
	
	
	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getVisibled() {
		return visibled;
	}

	public void setVisibled(String visibled) {
		this.visibled = visibled;
	}

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

	public String getCouponid() {
		return couponid;
	}

	public void setCouponid(String couponid) {
		this.couponid = couponid;
	}

	public String getChangetype() {
		return changetype;
	}

	public void setChangetype(String changetype) {
		this.changetype = changetype;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}
	
	@Override
	public PayMentsInfo clone() {
		PayMentsInfo info = null;
		try {
			info = (PayMentsInfo) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

}
