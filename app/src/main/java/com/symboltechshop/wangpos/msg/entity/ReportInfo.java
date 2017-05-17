package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

public class ReportInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private SaleReportInfo sale;
	private RefundReportInfo refund;
	private TotalReportInfo total;

	public SaleReportInfo getSale() {
		return sale;
	}

	public void setSale(SaleReportInfo sale) {
		this.sale = sale;
	}

	public RefundReportInfo getRefund() {
		return refund;
	}

	public void setRefund(RefundReportInfo refund) {
		this.refund = refund;
	}

	public TotalReportInfo getTotal() {
		return total;
	}

	public void setTotal(TotalReportInfo total) {
		this.total = total;
	}
}
