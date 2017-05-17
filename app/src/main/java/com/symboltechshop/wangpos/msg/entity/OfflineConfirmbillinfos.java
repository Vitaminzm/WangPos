package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;


/**
 * 离线支付info
 * 
 * @author so
 * 
 */
public class OfflineConfirmbillinfos implements Serializable {

	private static final long serialVersionUID = 1L;
	private String backreason;
    private String billid;
    private String changemoney;
    private String saletime;
    private List<OfflinePayTypeInfo> paymentslist;
	public String getBackreason() {
		return backreason;
	}
	public void setBackreason(String backreason) {
		this.backreason = backreason;
	}
	public String getBillid() {
		return billid;
	}
	public void setBillid(String billid) {
		this.billid = billid;
	}
	public String getChangemoney() {
		return changemoney;
	}
	public void setChangemoney(String changemoney) {
		this.changemoney = changemoney;
	}
	public String getSaletime() {
		return saletime;
	}
	public void setSaletime(String saletime) {
		this.saletime = saletime;
	}
	public List<OfflinePayTypeInfo> getPaymentslist() {
		return paymentslist;
	}
	public void setPaymentslist(List<OfflinePayTypeInfo> paymentslist) {
		this.paymentslist = paymentslist;
	}

}
