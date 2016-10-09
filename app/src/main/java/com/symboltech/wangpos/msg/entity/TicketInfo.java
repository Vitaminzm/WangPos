package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 原交易订单信息
 * @author so
 *
 */
public class TicketInfo implements Serializable{

	private BillInfo billinfo;

	public BillInfo getBillinfo() {
		return billinfo;
	}

	public void setBillinfo(BillInfo billinfo) {
		this.billinfo = billinfo;
	}

}
