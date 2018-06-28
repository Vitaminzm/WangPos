package com.symboltech.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.ThirdPay;

import java.util.List;

/** 
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年11月4日 下午2:26:21 
* @version 1.0 
*/
public class ThirdPayResult extends BaseResult{

	@SerializedName("data")
	private List<ThirdPay> thirdpay;

	public List<ThirdPay> getThirdpay() {
		return thirdpay;
	}

	public void setThirdpay(List<ThirdPay> thirdpay) {
		this.thirdpay = thirdpay;
	}
}
