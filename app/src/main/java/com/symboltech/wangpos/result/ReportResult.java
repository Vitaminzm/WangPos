package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.ReportInfo;

public class ReportResult extends BaseResult {
	@SerializedName("data")
	private ReportInfo reportInfo;

	public ReportInfo getReportInfo() {
		return reportInfo;
	}

	public void setReportInfo(ReportInfo reportInfo) {
		this.reportInfo = reportInfo;
	}

}
