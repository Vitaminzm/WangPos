package com.symboltech.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.AllMemberInfo;

public class AllMemeberInfoResult extends BaseResult {

	@SerializedName("data")
	private AllMemberInfo allInfo;

	public AllMemberInfo getAllInfo() {
		return allInfo;
	}

	public void setAllInfo(AllMemberInfo allInfo) {
		this.allInfo = allInfo;
	}
	
	
}
