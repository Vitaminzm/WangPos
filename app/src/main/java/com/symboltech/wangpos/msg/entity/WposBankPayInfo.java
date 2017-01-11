package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 银行支付信息
 * @author so
 *
 */
public class WposBankPayInfo implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	/*
	voucher_no	凭证号	String	银行卡消费才返回	否
	bank_no	银行卡号	String	银行卡消费才返回	否
	ref_no	参考号	String	银行卡消费才返回	否
	buyer_user	支付账号	String	微信或支付宝支付返回，为支付帐号	否
	third_serial_no	支付订单号	String	微信或支付宝支付返回，为微信或支付宝订单号	否
	*/
	private String voucher_no;
	private String bank_no;
	private String ref_no;
	private String buyer_user;
	private String third_serial_no;

	public String getVoucher_no() {
		return voucher_no;
	}

	public void setVoucher_no(String voucher_no) {
		this.voucher_no = voucher_no;
	}

	public String getBank_no() {
		return bank_no;
	}

	public void setBank_no(String bank_no) {
		this.bank_no = bank_no;
	}

	public String getRef_no() {
		return ref_no;
	}

	public void setRef_no(String ref_no) {
		this.ref_no = ref_no;
	}

	public String getBuyer_user() {
		return buyer_user;
	}

	public void setBuyer_user(String buyer_user) {
		this.buyer_user = buyer_user;
	}

	public String getThird_serial_no() {
		return third_serial_no;
	}

	public void setThird_serial_no(String third_serial_no) {
		this.third_serial_no = third_serial_no;
	}
}
