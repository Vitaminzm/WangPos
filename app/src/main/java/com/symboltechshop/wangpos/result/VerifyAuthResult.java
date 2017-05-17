package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;

public class VerifyAuthResult extends BaseResult {

	@SerializedName("data")
	private String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
