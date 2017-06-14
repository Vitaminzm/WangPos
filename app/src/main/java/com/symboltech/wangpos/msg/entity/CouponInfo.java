package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 优惠卷信息
 * 
 * @author so
 * 
 */
public class CouponInfo implements Serializable , Cloneable {

	private static final long serialVersionUID = 1L;
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBegindate() {
		return begindate;
	}

	public void setBegindate(String begindate) {
		this.begindate = begindate;
	}

	public String getEnddate() {
		return enddate;
	}

	public void setEnddate(String enddate) {
		this.enddate = enddate;
	}

	public String getFacevalue() {
		return facevalue;
	}

	public void setFacevalue(String facevalue) {
		this.facevalue = facevalue;
	}

	private String id;
	private String couponno;
	private String type;
	private String name;
	private String value;
	private String title;
	private String begindate;
	private String enddate;
	private String money;
	private String availablemoney;
	private String cashflag;//是否是现金券
	private String content;//使用说明
	
	
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCashflag() {
		return cashflag;
	}

	public void setCashflag(String cashflag) {
		this.cashflag = cashflag;
	}

	public String getAvailablemoney() {
		return availablemoney;
	}

	public void setAvailablemoney(String availablemoney) {
		this.availablemoney = availablemoney;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	/**优惠券面值*/
	private String facevalue;
	private String isused; // 0-未使用  1-使用

	public String getIsused() {
		return isused;
	}

	public void setIsused(String isused) {
		this.isused = isused;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCouponno() {
		return couponno;
	}

	public void setCouponno(String couponno) {
		this.couponno = couponno;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		return couponno.equals(((CouponInfo) o).getCouponno());
	}

	@Override
	public CouponInfo clone() {
		CouponInfo clone = null;
		try {
			clone = (CouponInfo) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clone;
	}
}
