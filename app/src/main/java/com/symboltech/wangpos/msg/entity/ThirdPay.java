package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月4日 下午2:51:41
 * @version 1.0
 */
public class ThirdPay implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;
	/** 交易单号 */
	private String trade_no;
	/** 顾客名称 */
	private String pay_buyer;
	/** 顾客ID */
	private String pay_buyer_id;
	/** 第三方流水号 */
	private String pay_trade_no;
	/** 总金额 */
	private String pay_total_fee;
	/** 支付方式 */
	private String pay_type;
	/** 支付减免 */
	private String pay_coupon_fee;
	/** 实际支付 */
	private String pay_cash_fee;
	/** 收款方式id */
	private String skfsid;

	public String getTrade_no() {
		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public String getPay_buyer() {
		return pay_buyer;
	}

	public void setPay_buyer(String pay_buyer) {
		this.pay_buyer = pay_buyer;
	}

	public String getPay_buyer_id() {
		return pay_buyer_id;
	}

	public void setPay_buyer_id(String pay_buyer_id) {
		this.pay_buyer_id = pay_buyer_id;
	}

	public String getPay_trade_no() {
		return pay_trade_no;
	}

	public void setPay_trade_no(String pay_trade_no) {
		this.pay_trade_no = pay_trade_no;
	}

	public String getPay_total_fee() {
		return pay_total_fee;
	}

	public void setPay_total_fee(String pay_total_fee) {
		this.pay_total_fee = pay_total_fee;
	}

	public String getPay_type() {
		return pay_type;
	}

	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}

	public String getPay_coupon_fee() {
		return pay_coupon_fee;
	}

	public void setPay_coupon_fee(String pay_coupon_fee) {
		this.pay_coupon_fee = pay_coupon_fee;
	}

	public String getPay_cash_fee() {
		return pay_cash_fee;
	}

	public void setPay_cash_fee(String pay_cash_fee) {
		this.pay_cash_fee = pay_cash_fee;
	}
	
	public String getSkfsid() {
		return skfsid;
	}

	public void setSkfsid(String skfsid) {
		this.skfsid = skfsid;
	}

	@Override
	public ThirdPay clone() {
		ThirdPay clone = null;
		try {
			clone = (ThirdPay) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clone;
	}

}
