package com.symboltechshop.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.ThirdPayQuery;

/**
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年11月4日 下午2:49:22 
* @version 1.0 
*/
public class ThirdPayQueryResult extends BaseResult{

	@SerializedName("data")
	private ThirdPayQuery thirdpayquery;

	public ThirdPayQuery getThirdpayquery() {
		return thirdpayquery;
	}

	public void setThirdpayquery(ThirdPayQuery thirdpayquery) {
		this.thirdpayquery = thirdpayquery;
	}
}
