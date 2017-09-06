package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;


/**
 * 离线保存交易info
 * 
 * @author so
 * 
 */
public class OfflineSavearticleinfos implements Serializable {

	private static final long serialVersionUID = 1L;
	 private String billid;
     private String personid;
     private String cashier;
     private String cashiername;
     private String oldbillid;
     private String oldposno;
     private String saletime;
     private String saletype;
     private String totalmoney;
	 private MemberInfo member;
     private List<OfflineGoodsInfo> goodslist;

	public MemberInfo getMember() {
		return member;
	}

	public void setMember(MemberInfo member) {
		this.member = member;
	}

	public String getCashiername() {
		return cashiername;
	}
	public void setCashiername(String cashiername) {
		this.cashiername = cashiername;
	}
	public String getBillid() {
		return billid;
	}
	public void setBillid(String billid) {
		this.billid = billid;
	}
	public String getPersonid() {
		return personid;
	}
	public void setPersonid(String personid) {
		this.personid = personid;
	}
	public String getCashier() {
		return cashier;
	}
	public void setCashier(String cashier) {
		this.cashier = cashier;
	}
	public String getOldbillid() {
		return oldbillid;
	}
	public void setOldbillid(String oldbillid) {
		this.oldbillid = oldbillid;
	}
	public String getOldposno() {
		return oldposno;
	}
	public void setOldposno(String oldposno) {
		this.oldposno = oldposno;
	}
	public String getSaletime() {
		return saletime;
	}
	public void setSaletime(String saletime) {
		this.saletime = saletime;
	}
	public String getSaletype() {
		return saletype;
	}
	public void setSaletype(String saletype) {
		this.saletype = saletype;
	}
	public String getTotalmoney() {
		return totalmoney;
	}
	public void setTotalmoney(String totalmoney) {
		this.totalmoney = totalmoney;
	}
	public List<OfflineGoodsInfo> getGoodslist() {
		return goodslist;
	}
	public void setGoodslist(List<OfflineGoodsInfo> goodslist) {
		this.goodslist = goodslist;
	}
     

}
