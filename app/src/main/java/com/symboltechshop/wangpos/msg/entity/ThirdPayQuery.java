package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 第三方支付查询
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月4日 下午2:51:41
 * @version 1.0
 */
public class ThirdPayQuery implements Serializable{

	private static final long serialVersionUID = 1L;
	/** 支付中心的订单号 */
	private String trade_no;
	/** 交易时间 */
	private String time;
	/** 交易金额 */
	private String total_fee;
	/** 交易途径 */
	private String pay_type;

	public String getTrade_no() {
		return trade_no;
	}

	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(String total_fee) {
		this.total_fee = total_fee;
	}

	public String getPay_type() {
		return pay_type;
	}

	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}

}
