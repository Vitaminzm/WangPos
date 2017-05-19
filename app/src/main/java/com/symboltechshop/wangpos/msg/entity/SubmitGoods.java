package com.symboltechshop.wangpos.msg.entity;

import com.google.gson.annotations.SerializedName;


import java.io.Serializable;
import java.util.List;

/**
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月12日 下午5:40:07
 * @version 1.0
 */
public class SubmitGoods implements Serializable{

	/** TODO*/
	private static final long serialVersionUID = 1L;


	@SerializedName("maxcanexchangepoint")
	private String limitpoint;
	@SerializedName("usedcouponlist")
	private List<CouponInfo> CouponInfos;


	public String getLimitpoint() {
		return limitpoint;
	}

	public void setLimitpoint(String limitpoint) {
		this.limitpoint = limitpoint;
	}

	public List<CouponInfo> getCouponInfos() {
		return CouponInfos;
	}

	public void setCouponInfos(List<CouponInfo> couponInfos) {
		CouponInfos = couponInfos;
	}
}
