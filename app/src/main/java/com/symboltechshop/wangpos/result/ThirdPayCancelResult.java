package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.ThirdPayCancel;

/** 
 * 
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年11月4日 下午2:42:50 
* @version 1.0 
*/
public class ThirdPayCancelResult extends BaseResult{

	@SerializedName("data")
	private ThirdPayCancel thirdpaycancel;

	public ThirdPayCancel getThirdpaycancel() {
		return thirdpaycancel;
	}

	public void setThirdpaycancel(ThirdPayCancel thirdpaycancel) {
		this.thirdpaycancel = thirdpaycancel;
	}
}
