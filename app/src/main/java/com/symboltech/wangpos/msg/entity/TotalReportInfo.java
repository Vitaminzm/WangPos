package com.symboltech.wangpos.msg.entity;

import java.util.List;

public class TotalReportInfo extends BaseReportInfo {

	private List<ReportDetailInfo> totallist;

	public List<ReportDetailInfo> getTotallist() {
		return totallist;
	}

	public void setTotallist(List<ReportDetailInfo> totallist) {
		this.totallist = totallist;
	}
	
}
