package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单信息
 * 
 * @author so
 * 
 */
public class BillInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	private String posno; // 款台
	private String billid; // 小票
	private String cashier; // 收款员工号
	private String cashiername; // 款员名称
	private String saleman; // 营业员工号
	private String salemanname; // 营业员名称
	private String totalmoney; // 交易总金额
	private String realmoney;//实际交易金额
	private String saletime; // 交易时间
	private String awardpoint; // 交易获得积分
	private String usedpoint; // 交易使用的积分
	private String exchangedpoint; // 交易抵扣的积分
	private String saletype;// 交易类型:0:消费 1:选单退货 2:普通退货
	private String oldposno;//原款台号 退货使用
	private String oldbillid;//原小票号 退货使用
	private String backreason;//退货原因
	private String changemoney;//找零
	private String usedpointmoney;//积分抵扣金额
	private String totalpoint;//会员总积分
	
	public String getTotalpoint() {
		return totalpoint;
	}

	public void setTotalpoint(String totalpoint) {
		this.totalpoint = totalpoint;
	}

	public String getUsedpointmoney() {
		return usedpointmoney;
	}

	public void setUsedpointmoney(String usedpointmoney) {
		this.usedpointmoney = usedpointmoney;
	}

	public String getCashiername() {
		return cashiername;
	}

	public void setCashiername(String cashiername) {
		this.cashiername = cashiername;
	}

	public String getSalemanname() {
		return salemanname;
	}

	public void setSalemanname(String salemanname) {
		this.salemanname = salemanname;
	}

	public String getChangemoney() {
		return changemoney;
	}

	public void setChangemoney(String changemoney) {
		this.changemoney = changemoney;
	}

	private MemberInfo member;
	private ExchangeInfo exchange;//积分兑换信息
	private List<GoodsInfo> goodslist;
	private List<PayMentsInfo> paymentslist;
	private List<CouponInfo> usedcouponlist;// 交易用卷信息
	private List<CouponInfo> allcouponlist;// 用户持有用卷信息
	public List<CouponInfo> getAllcouponlist() {
		return allcouponlist;
	}

	public void setAllcouponlist(List<CouponInfo> allcouponlist) {
		this.allcouponlist = allcouponlist;
	}

	private List<CouponInfo> grantcouponlist;// 交易返卷信息
	private List<ValueCardInfo> usedcardlist;//储值卡列表
	private List<BankPayInfo> bankpaylist;//银汉卡信息列表
	private List<ThirdPay> thirdpartypaylist;//第三方支付信息列表
	private PayMentsInfo cash;//现金

	

	public PayMentsInfo getCash() {
		return cash;
	}

	public void setCash(PayMentsInfo cash) {
		this.cash = cash;
	}

	public List<BankPayInfo> getBankpaylist() {
		return bankpaylist;
	}

	public void setBankpaylist(List<BankPayInfo> bankpaylist) {
		this.bankpaylist = bankpaylist;
	}

	public List<ThirdPay> getThirdpartypaylist() {
		return thirdpartypaylist;
	}

	public void setThirdpartypaylist(List<ThirdPay> thirdpartypaylist) {
		this.thirdpartypaylist = thirdpartypaylist;
	}

	public void bindGoods(GoodsInfo goods) {
		if(goodslist == null) {
			goodslist = new ArrayList<GoodsInfo>();
		}
		goodslist.add(goods);
	} 
	
	public List<PayMentsInfo> getPaymentslist() {
		return paymentslist;
	}

	public void setPaymentslist(List<PayMentsInfo> paymentslist) {
		this.paymentslist = paymentslist;
	}

	public List<ValueCardInfo> getUsedcardlist() {
		return usedcardlist;
	}

	public void setUsedcardlist(List<ValueCardInfo> usedcardlist) {
		this.usedcardlist = usedcardlist;
	}

	public String getBackreason() {
		return backreason;
	}

	public void setBackreason(String backreason) {
		this.backreason = backreason;
	}

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
	public String getCashier() {
		return cashier;
	}
	public void setCashier(String cashier) {
		this.cashier = cashier;
	}
	
	public String getSaleman() {
		return saleman;
	}
	public void setSaleman(String saleman) {
		this.saleman = saleman;
	}
	
	public String getTotalmoney() {
		return totalmoney;
	}
	public void setTotalmoney(String totalmoney) {
		this.totalmoney = totalmoney;
	}
	public String getSaletime() {
		return saletime;
	}
	public void setSaletime(String saletime) {
		this.saletime = saletime;
	}
	public String getAwardpoint() {
		return awardpoint;
	}
	public void setAwardpoint(String awardpoint) {
		this.awardpoint = awardpoint;
	}
	
	public String getUsedpoint() {
		return usedpoint;
	}

	public void setUsedpoint(String usedpoint) {
		this.usedpoint = usedpoint;
	}

	public String getRealmoney() {
		return realmoney;
	}

	public void setRealmoney(String realmoney) {
		this.realmoney = realmoney;
	}

	public String getExchangedpoint() {
		return exchangedpoint;
	}

	public void setExchangedpoint(String exchangedpoint) {
		this.exchangedpoint = exchangedpoint;
	}

	public String getSaletype() {
		return saletype;
	}
	public void setSaletype(String saletype) {
		this.saletype = saletype;
	}
	public String getOldposno() {
		return oldposno;
	}
	public void setOldposno(String oldposno) {
		this.oldposno = oldposno;
	}
	public String getOldbillid() {
		return oldbillid;
	}
	public void setOldbillid(String oldbillid) {
		this.oldbillid = oldbillid;
	}
	public MemberInfo getMember() {
		return member;
	}
	public void setMember(MemberInfo member) {
		this.member = member;
	}
	public List<GoodsInfo> getGoodslist() {
		return goodslist;
	}

	public void setGoodslist(List<GoodsInfo> goodslist) {
		this.goodslist = goodslist;
	}
	public List<CouponInfo> getUsedcouponlist() {
		return usedcouponlist;
	}
	public void setUsedcouponlist(List<CouponInfo> usedcouponlist) {
		this.usedcouponlist = usedcouponlist;
	}
	public List<CouponInfo> getGrantcouponlist() {
		return grantcouponlist;
	}
	public void setGrantcouponlist(List<CouponInfo> grantcouponlist) {
		this.grantcouponlist = grantcouponlist;
	}

	public ExchangeInfo getExchange() {
		return exchange;
	}

	public void setExchange(ExchangeInfo exchange) {
		this.exchange = exchange;
	}

}
