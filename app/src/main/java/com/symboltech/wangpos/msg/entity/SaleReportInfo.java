package com.symboltech.wangpos.msg.entity;

import java.util.List;

public class SaleReportInfo extends BaseReportInfo {

	private List<ReportDetailInfo> salelist;

	public List<ReportDetailInfo> getSalelist() {
		return salelist;
	}

	public void setSalelist(List<ReportDetailInfo> salelist) {
		this.salelist = salelist;
	}
	
	
}
