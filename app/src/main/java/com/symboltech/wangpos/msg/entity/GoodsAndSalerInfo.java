package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 离线商品和营业员
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class GoodsAndSalerInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	private List<GoodsInfo> brandgoodlist;
	private List<CashierInfo> salemanlist;
	public List<GoodsInfo> getBrandgoodlist() {
		return brandgoodlist;
	}
	public void setBrandgoodlist(List<GoodsInfo> brandgoodlist) {
		this.brandgoodlist = brandgoodlist;
	}
	public List<CashierInfo> getSalemanlist() {
		return salemanlist;
	}
	public void setSalemanlist(List<CashierInfo> salemanlist) {
		this.salemanlist = salemanlist;
	}
}
