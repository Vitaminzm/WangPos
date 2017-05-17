package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 离线账单info
 * 
 * @author so
 * 
 */
public class OfflineBankInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String posno; // 款台号
	private String billid; // 小票号
	private String transtype; // 交易类型(1消费,2当日撤销,3隔日退货) 
	private String cardno; // 卡号(支付宝账号、微信账号)
	private String bankcode; // 银行代码
	private String batchno; // 批次号
	private String refno; // 参考号
	private String tradeno; // 流水号  
	private String amount; // 金额
	private String decmoney; // 扣减金额(预留，如果银行有扣减活动，则记录)
	private String skfsid; // 收款方式ID
	public String getPosno() {
		return posno;
	}
	public void setPosno(String posno) {
		this.posno = posno;
	}
	public String getBillid() {
		return billid;
	}
	public void setBillid(String billid) {
		this.billid = billid;
	}
	public String getTranstype() {
		return transtype;
	}
	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
	public String getBankcode() {
		return bankcode;
	}
	public void setBankcode(String bankcode) {
		this.bankcode = bankcode;
	}
	public String getBatchno() {
		return batchno;
	}
	public void setBatchno(String batchno) {
		this.batchno = batchno;
	}
	public String getRefno() {
		return refno;
	}
	public void setRefno(String refno) {
		this.refno = refno;
	}
	public String getTradeno() {
		return tradeno;
	}
	public void setTradeno(String tradeno) {
		this.tradeno = tradeno;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDecmoney() {
		return decmoney;
	}
	public void setDecmoney(String decmoney) {
		this.decmoney = decmoney;
	}
	public String getSkfsid() {
		return skfsid;
	}
	public void setSkfsid(String skfsid) {
		this.skfsid = skfsid;
	}
	
	
}
