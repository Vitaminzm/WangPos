package com.symboltechshop.wangpos.result;


import com.symboltechshop.wangpos.msg.entity.ReturnInfo;

/**
 * 
 * @author so
 *
 */
public class CommitOrderResult extends BaseResult {

	private ReturnInfo data;

	public ReturnInfo getData() {
		return data;
	}

	public void setData(ReturnInfo data) {
		this.data = data;
	}
}
