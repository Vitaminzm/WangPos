package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/** 
 * 第三方支付撤销
* @author  cwi-apst E-mail: 26873204@qq.com
* @date 创建时间：2015年11月4日 下午2:51:41 
* @version 1.0 
*/
public class ThirdPayCancel implements Serializable{

	/**支付类型 1、支付宝；2、银联；3、微信；4、全民付（银联商务） */
	private String pay_type;
	/**支付模式 1、手机APP快捷支付 2、手机网页支付3、被扫支付（支付宝当面付/微信刷卡支付）*/
	private String pay_mode;
	/** 交易订单号  */
	private String trade_no;
	
	public String getPay_type() {
		return pay_type;
	}
	public void setPay_type(String pay_type) {
		this.pay_type = pay_type;
	}
	public String getPay_mode() {
		return pay_mode;
	}
	public void setPay_mode(String pay_mode) {
		this.pay_mode = pay_mode;
	}
	public String getTrade_no() {
		return trade_no;
	}
	public void setTrade_no(String trade_no) {
		this.trade_no = trade_no;
	}
}
