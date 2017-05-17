package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 优惠卷监测信息
 * 
 * @author so
 * 
 */
public class CheckCouponInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String rulemoney;
	private String couponfacevalue;
	private String overagemoney;
	public String getRulemoney() {
		return rulemoney;
	}
	public void setRulemoney(String rulemoney) {
		this.rulemoney = rulemoney;
	}
	public String getCouponfacevalue() {
		return couponfacevalue;
	}
	public void setCouponfacevalue(String couponfacevalue) {
		this.couponfacevalue = couponfacevalue;
	}
	public String getOveragemoney() {
		return overagemoney;
	}
	public void setOveragemoney(String overagemoney) {
		this.overagemoney = overagemoney;
	}
	
}
