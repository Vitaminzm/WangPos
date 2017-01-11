package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

public class AllMemberInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private MemberInfo member;
	private List<CouponInfo> couponlist;
	private List<GoodsInfo> goodslist;

	private List<HistorySaleInfo> historysalelist;

	private HistoryAllSaleInfo historyallsale;
	public MemberInfo getMember() {
		return member;
	}
	public void setMember(MemberInfo member) {
		this.member = member;
	}
	public List<CouponInfo> getCouponlist() {
		return couponlist;
	}
	public void setCouponlist(List<CouponInfo> couponlist) {
		this.couponlist = couponlist;
	}
	public List<GoodsInfo> getGoodslist() {
		return goodslist;
	}
	public void setGoodslist(List<GoodsInfo> goodslist) {
		this.goodslist = goodslist;
	}
	public List<HistorySaleInfo> getHistorysalelist() {
		return historysalelist;
	}
	public void setHistorysalelist(List<HistorySaleInfo> historysalelist) {
		this.historysalelist = historysalelist;
	}
	public HistoryAllSaleInfo getHistoryallsale() {
		return historyallsale;
	}
	public void setHistoryallsale(HistoryAllSaleInfo historyallsale) {
		this.historyallsale = historyallsale;
	}
}
