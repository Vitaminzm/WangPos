package com.symboltechshop.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.ThirdPaySalesReturn;

/**
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月4日 下午2:50:45
 * @version 1.0
 */
public class ThirdPaySalesReturnResult extends BaseResult {

	@SerializedName("data")
	private ThirdPaySalesReturn thirdpaysalesreturn;

	public ThirdPaySalesReturn getThirdpaysalesreturn() {
		return thirdpaysalesreturn;
	}

	public void setThirdpaysalesreturn(ThirdPaySalesReturn thirdpaysalesreturn) {
		this.thirdpaysalesreturn = thirdpaysalesreturn;
	}
}
