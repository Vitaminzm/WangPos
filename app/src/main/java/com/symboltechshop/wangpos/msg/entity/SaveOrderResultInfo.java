package com.symboltechshop.wangpos.msg.entity;

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
	private String parkcouponhour; // 赠送的停车券时长
	private String parkcouponaddhour; //赠送的停车券有效日期
	private String randomcode; //小票随机校验码
	private List<CouponInfo> grantcouponlist; // 返券列表
	private List<CouponInfo> allcouponlist;//会员总积分

	public String getRandomcode() {
		return randomcode;
	}

	public void setRandomcode(String randomcode) {
		this.randomcode = randomcode;
	}

	public String getParkcouponhour() {
		return parkcouponhour;
	}
	public void setParkcouponhour(String parkcouponhour) {
		this.parkcouponhour = parkcouponhour;
	}
	
	
	public String getParkcouponaddhour() {
		return parkcouponaddhour;
	}
	public void setParkcouponaddhour(String parkcouponaddhour) {
		this.parkcouponaddhour = parkcouponaddhour;
	}
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
