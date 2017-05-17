package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 发放停车券信息
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm
 * @see
 * @since 1.0
 */
public class SendCarCouponInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	//停车券时长
	private String hour;
	//车牌号
	private String carno;
	//会员卡号
	private String cardno;
	//款台号
	private String posno;
	//小票号
	private String billid;
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	public String getCarno() {
		return carno;
	}
	public void setCarno(String carno) {
		this.carno = carno;
	}
	public String getCardno() {
		return cardno;
	}
	public void setCardno(String cardno) {
		this.cardno = cardno;
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
	
}
