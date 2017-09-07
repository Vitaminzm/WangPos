package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;

public class ZklResult extends BaseResult {

	@SerializedName("data")
	private Double data;

	public Double getData() {
		return data;
	}

	public void setData(Double data) {
		this.data = data;
	}
}
