package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.SaveOrderResultInfo;

/**
 * 保存交易
 * @author so
 *
 */
public class SaveOrderResult extends BaseResult {

	@SerializedName("data")
	private SaveOrderResultInfo saveOrderInfo;

	public SaveOrderResultInfo getSaveOrderInfo() {
		return saveOrderInfo;
	}

	public void setSaveOrderInfo(SaveOrderResultInfo saveOrderInfo) {
		this.saveOrderInfo = saveOrderInfo;
	}
	
}
