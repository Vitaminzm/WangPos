package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;


/**
 * 离线商品info
 * 
 * @author so
 * 
 */
public class OfflineGoodsInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String id; // 商品id
	private String inx;// 商品顺序
	private String goodsname;// 商品名称
	private String usedpoint;// 商品使用积分
	private String price;// 商品价格
	private String saleamt;// 销售金额
	private String salecount = "1";// 商品数量
	private String discmoney = "0";// 折扣金额
	private String preferentialmoney = "0";// 优惠金额
	private String barcode;// 条码
	private String code;// 条码
	private String unit;

	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getGoodsname() {
		return goodsname;
	}

	public void setGoodsname(String goodsname) {
		this.goodsname = goodsname;
	}

	public String getUsedpoint() {
		return usedpoint;
	}

	public void setUsedpoint(String usedpoint) {
		this.usedpoint = usedpoint;
	}
	
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getInx() {
		return inx;
	}

	public void setInx(String inx) {
		this.inx = inx;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getSaleamt() {
		return saleamt;
	}

	public void setSaleamt(String saleamt) {
		this.saleamt = saleamt;
	}

	public String getSalecount() {
		return salecount;
	}

	public void setSalecount(String salecount) {
		this.salecount = salecount;
	}

	public String getDiscmoney() {
		return discmoney;
	}

	public void setDiscmoney(String discmoney) {
		this.discmoney = discmoney;
	}

	public String getPreferentialmoney() {
		return preferentialmoney;
	}

	public void setPreferentialmoney(String preferentialmoney) {
		this.preferentialmoney = preferentialmoney;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
