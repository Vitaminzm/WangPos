package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * pos初始化info
 * 
 * @author so
 * 
 */
public class InitializeInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<GoodsInfo> brandgoodslist;// 品牌列表
	private List<PayMentsInfo> paymentslist;// 收款方式列表
	private List<PromotionInfo> promlist;// 促销列表
	private List<RefundReasonInfo> refundreasonlist;// 退货原因列表
	private List<CashierInfo> salemanlist;// 营业员列表
	public List<GoodsInfo> getBrandgoodslist() {
		return brandgoodslist;
	}
	public void setBrandgoodslist(List<GoodsInfo> brandgoodslist) {
		this.brandgoodslist = brandgoodslist;
	}
	public List<PayMentsInfo> getPaymentslist() {
		return paymentslist;
	}
	public void setPaymentslist(List<PayMentsInfo> paymentslist) {
		this.paymentslist = paymentslist;
	}
	public List<PromotionInfo> getPromlist() {
		return promlist;
	}
	public void setPromlist(List<PromotionInfo> promlist) {
		this.promlist = promlist;
	}
	public List<RefundReasonInfo> getRefundreasonlist() {
		return refundreasonlist;
	}
	public void setRefundreasonlist(List<RefundReasonInfo> refundreasonlist) {
		this.refundreasonlist = refundreasonlist;
	}
	public List<CashierInfo> getSalemanlist() {
		return salemanlist;
	}
	public void setSalemanlist(List<CashierInfo> salemanlist) {
		this.salemanlist = salemanlist;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
