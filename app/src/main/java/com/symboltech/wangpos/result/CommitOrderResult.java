package com.symboltech.wangpos.result;


import com.symboltech.wangpos.msg.entity.ReturnInfo;

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
