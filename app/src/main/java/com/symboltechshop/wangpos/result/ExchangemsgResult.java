package com.symboltechshop.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltechshop.wangpos.msg.entity.ExchangeScoreInfo;

/**
 * exchangeresult
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm emial:mingming.zhang@symboltech.com 2015年11月11日
 * @see
 * @since 1.0
 */
public class ExchangemsgResult extends BaseResult{
	
	@SerializedName("data")
	private ExchangeScoreInfo info;

	public ExchangeScoreInfo getInfo() {
		return info;
	}

	public void setInfo(ExchangeScoreInfo info) {
		this.info = info;
	}


}
