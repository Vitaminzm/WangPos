package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.ZklInfo;

public class ZklResult extends BaseResult {

	@SerializedName("data")
	private ZklInfo data;

	public ZklInfo getData() {
		return data;
	}

	public void setData(ZklInfo data) {
		this.data = data;
	}
}
