package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 报表基类
 * @author so
 *
 */
public class BaseReportInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String billcount; // 笔数统计
	private String totalmoney; // 金额统计
	public String getBillcount() {
		return billcount;
	}
	public void setBillcount(String billcount) {
		this.billcount = billcount;
	}
	public String getTotalmoney() {
		return totalmoney;
	}
	public void setTotalmoney(String totalmoney) {
		this.totalmoney = totalmoney;
	}

	
}
