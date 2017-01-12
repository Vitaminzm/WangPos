package com.symboltech.wangpos.msg.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 报表详情
 * @author so
 *
 */
public class ReportDetailInfo implements Serializable, Cloneable{

	private String code;
	private String name;
	private String money;
	@SerializedName("billcount")
	private String count;
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
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	@Override
	public ReportDetailInfo clone() {
		ReportDetailInfo clone = null;
		try {
			clone = (ReportDetailInfo) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clone;
	}
}
