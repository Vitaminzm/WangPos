package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 退货信息
 * @author so
 *
 */
public class ReturnInfo implements Serializable{

	private String maxcanexchangepoint;//最多可参与兑换积分
	private List<CouponInfo> usedcouponlist;//可用券列表
	public String getMaxcanexchangepoint() {
		return maxcanexchangepoint;
	}
	public void setMaxcanexchangepoint(String maxcanexchangepoint) {
		this.maxcanexchangepoint = maxcanexchangepoint;
	}
	public List<CouponInfo> getUsedcouponlist() {
		return usedcouponlist;
	}
	public void setUsedcouponlist(List<CouponInfo> usedcouponlist) {
		this.usedcouponlist = usedcouponlist;
	}
	
}
