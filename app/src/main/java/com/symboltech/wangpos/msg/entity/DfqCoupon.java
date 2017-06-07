package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 优惠卷信息
 * 
 * @author so
 * 
 */
public class DfqCoupon implements Serializable {

	private static final long serialVersionUID = 1L;

	private String dfqgzname;
	private String dfqmoney;

	public String getDfqgzname() {
		return dfqgzname;
	}

	public void setDfqgzname(String dfqgzname) {
		this.dfqgzname = dfqgzname;
	}

	public String getDfqmoney() {
		return dfqmoney;
	}

	public void setDfqmoney(String dfqmoney) {
		this.dfqmoney = dfqmoney;
	}
}
