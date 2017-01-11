package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 品牌info
 * 
 * @author so
 * 
 */
public class BrandInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String brandcode; // 品牌编码
	private String name; // 品牌名称
	private List<GoodsInfo> goodslist; // 商品列表

	public String getBrandcode() {
		return brandcode;
	}

	public void setBrandcode(String brandcode) {
		this.brandcode = brandcode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<GoodsInfo> getGoodslist() {
		return goodslist;
	}

	public void setGoodslist(List<GoodsInfo> goodslist) {
		this.goodslist = goodslist;
	}

}
