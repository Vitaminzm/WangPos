package com.symboltech.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.GoodsAndSalerListInfo;

public class GoodsAndSalerInfoResult extends BaseResult {

	@SerializedName("data")
	private GoodsAndSalerListInfo allInfo;

	public GoodsAndSalerListInfo getAllInfo() {
		return allInfo;
	}

	public void setAllInfo(GoodsAndSalerListInfo allInfo) {
		this.allInfo = allInfo;
	}
}
