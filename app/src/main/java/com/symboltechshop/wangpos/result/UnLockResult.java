package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.CashierInfo;

/** 
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年10月27日 下午5:48:05 
* @version 1.0 
*/
public class UnLockResult extends BaseResult{

	@SerializedName("data")
	private CashierInfo cashierinfo;

	public CashierInfo getCashierinfo() {
		return cashierinfo;
	}

	public void setCashierinfo(CashierInfo cashierinfo) {
		this.cashierinfo = cashierinfo;
	}
}
