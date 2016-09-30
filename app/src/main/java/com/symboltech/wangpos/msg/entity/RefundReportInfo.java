package com.symboltech.wangpos.msg.entity;

import java.util.List;

public class RefundReportInfo extends BaseReportInfo {

	private List<ReportDetailInfo> refundlist;

	public List<ReportDetailInfo> getRefundlist() {
		return refundlist;
	}

	public void setRefundlist(List<ReportDetailInfo> refundlist) {
		this.refundlist = refundlist;
	}
	
	
}
