package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.CouponInfo;

/**
 * papercouponresult
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm emial:mingming.zhang@symboltech.com 2015年11月11日
 * @see
 * @since 1.0
 */
public class CouponResult extends BaseResult{
	
	@SerializedName("data")
	private CouponInfo couponInfo;

	public CouponInfo getCouponinfo() {
		return couponInfo;
	}

	public void setCouponinfo(CouponInfo couponInfo) {
		this.couponInfo = couponInfo;
	}
	
	

}
