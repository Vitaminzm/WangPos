package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 第三方支付退货
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月4日 下午2:51:41
 * @version 1.0
 */
public class ThirdPaySalesReturn implements Serializable{

	/** 支付中心退款编号 */
	private String refund_no;
	/** 内部退款编号 */
	private String out_refund_no;
	/** 外部退款编号 */
	private String source_refund_no;
	/** 总金额 */
	private String total_fee;
	/** 退款金额 真实退款金额 */
	private String refund_fee;
	/** 交易类型(1支付宝,3微信) */
	private String pay_type;

	public String getRefund_no() {
		return refund_no;
	}

	public void setRefund_no(String refund_no) {
		this.refund_no = refund_no;
	}

	public String getOut_refund_no() {
		return out_refund_no;
	}

	public void setOut_refund_no(String out_refund_no) {
		this.out_refund_no = out_refund_no;
	}

	public String getSource_refund_no() {
		return source_refund_no;
	}

	public void setSource_refund_no(String source_refund_no) {
		this.source_refund_no = source_refund_no;
	}

	public String getTotal_fee() {
		return total_fee;
	}

	public void setTotal_fee(String total_fee) {
		this.total_fee = total_fee;
	}

	public String getRefund_fee() {
		return refund_fee;
	}

	public void setRefund_fee(String refund_fee) {
		this.refund_fee = refund_fee;
	}

	public String getPay_type() {
		return pay_type;
	}

	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}

}
