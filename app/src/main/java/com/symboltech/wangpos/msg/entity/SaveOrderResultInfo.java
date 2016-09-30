package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 交易保存结果实体
 * 
 * @author so
 * 
 */
public class SaveOrderResultInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String billid; // 新的小票号
	private String gainpoint; // 本交易获得积分值
	private String totalpoint; // 本卡总积分
	private List<CouponInfo> grantcouponlist; // 返券列表
	private List<CouponInfo> allcouponlist;//会员总积分
	
	public List<CouponInfo> getAllcouponlist() {
		return allcouponlist;
	}
	public void setAllcouponlist(List<CouponInfo> allcouponlist) {
		this.allcouponlist = allcouponlist;
	}
	public String getBillid() {
		return billid;
	}
	public void setBillid(String billid) {
		this.billid = billid;
	}
	public String getGainpoint() {
		return gainpoint;
	}
	public void setGainpoint(String gainpoint) {
		this.gainpoint = gainpoint;
	}
	public String getTotalpoint() {
		return totalpoint;
	}
	public void setTotalpoint(String totalpoint) {
		this.totalpoint = totalpoint;
	}
	public List<CouponInfo> getGrantcouponlist() {
		return grantcouponlist;
	}
	public void setGrantcouponlist(List<CouponInfo> grantcouponlist) {
		this.grantcouponlist = grantcouponlist;
	}

}
