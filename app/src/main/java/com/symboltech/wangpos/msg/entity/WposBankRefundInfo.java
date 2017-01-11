package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 银行支付信息
 * @author so
 *
 */
public class WposBankRefundInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;


	/**
	 * errCode : 0
	 * errMsg : 退款成功
	 * cashier_trade_no : 10001574042016081190000011
	 * operator : 匿名用户
	 * origin_ref_no : 560913150456
	 * refund_ref_no : 560913150456
	 * refund_vouch_no : 164611
	 */

	private String errCode;
	private String errMsg;
	private String cashier_trade_no;
	private String operator;
	private String origin_ref_no;
	private String refund_ref_no;
	private String refund_vouch_no;

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public String getCashier_trade_no() {
		return cashier_trade_no;
	}

	public void setCashier_trade_no(String cashier_trade_no) {
		this.cashier_trade_no = cashier_trade_no;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getOrigin_ref_no() {
		return origin_ref_no;
	}

	public void setOrigin_ref_no(String origin_ref_no) {
		this.origin_ref_no = origin_ref_no;
	}

	public String getRefund_ref_no() {
		return refund_ref_no;
	}

	public void setRefund_ref_no(String refund_ref_no) {
		this.refund_ref_no = refund_ref_no;
	}

	public String getRefund_vouch_no() {
		return refund_vouch_no;
	}

	public void setRefund_vouch_no(String refund_vouch_no) {
		this.refund_vouch_no = refund_vouch_no;
	}
}
