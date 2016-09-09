package com.symboltech.wangpos.result;


import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.InitializeInfo;

/**
 * InitializeInfResult
 * @author so
 *
 */
public class InitializeInfResult extends BaseResult {
	
	@SerializedName("data")
	private InitializeInfo initializeInfo;

	public InitializeInfo getInitializeInfo() {
		return initializeInfo;
	}

	public void setInitializeInfo(InitializeInfo initializeInfo) {
		this.initializeInfo = initializeInfo;
	}
	
}
