package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.CheckCouponInfo;

/**
 * CheckCouponInfo
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm emial:mingming.zhang@symboltech.com 2015年11月11日
 * @see
 * @since 1.0
 */
public class CheckCouponResult extends BaseResult{
	
	@SerializedName("data")
	private CheckCouponInfo checkcouponinfo;

	public CheckCouponInfo getCheckcouponinfo() {
		return checkcouponinfo;
	}

	public void setCheckcouponinfo(CheckCouponInfo checkcouponinfo) {
		this.checkcouponinfo = checkcouponinfo;
	}

}
