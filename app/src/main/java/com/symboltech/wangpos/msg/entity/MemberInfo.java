package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 会员信息
 * 
 * @author so
 * 
 */
public class MemberInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//private String id;// 会员唯一ID
	private String memberno;// 会员卡号
	private String membertype;// 会员类型
	private String membername;// 会员名称
	private String membertypename;// 会员卡名称
	private String phoneno;// 手机号
	private String point;// 积分
	private String cent_available;// 可用积分
	private String cent_total;// 总积分
	private String can_cashcard;// 是否拥有储值卡账户:0否1是
	private String can_coupon;// 是否拥有券账户:0否1是
	private String can_cent;// 是否拥有积分账户 :0否1是
	private String Ischecked;// 是否验证 //0 未验证 1 已经验证
	private String pointrule;// 积分规则描述
	private List<String> membertag;//会员标签
	private List<String> behaviortag;//行为标签
	private List<String> saletag;//消费标签
	private String status;// 积分规则描述
	private List<CarInfo> listcar;

	public String getMembertypename() {
		return membertypename;
	}

	public void setMembertypename(String membertypename) {
		this.membertypename = membertypename;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<CarInfo> getListcar() {
		return listcar;
	}

	public void setListcar(List<CarInfo> listcar) {
		this.listcar = listcar;
	}

	public String getPointrule() {
		return pointrule;
	}

	public void setPointrule(String pointrule) {
		this.pointrule = pointrule;
	}

	public String getMembername() {
		return membername;
	}

	public String getIschecked() {
		return Ischecked;
	}

	public void setIschecked(String ischecked) {
		Ischecked = ischecked;
	}

	public void setMembername(String membername) {
		this.membername = membername;
	}

//	public String getId() {
//		return id;
//	}
//
//	public void setId(String id) {
//		this.id = id;
//	}

	public String getMemberno() {
		return memberno;
	}

	public void setMemberno(String memberno) {
		this.memberno = memberno;
	}

	public String getMembertype() {
		return membertype;
	}

	public void setMembertype(String membertype) {
		this.membertype = membertype;
	}

	public String getPhoneno() {
		return phoneno;
	}

	public void setPhoneno(String phoneno) {
		this.phoneno = phoneno;
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

	public String getCent_available() {
		return cent_available;
	}

	public void setCent_available(String cent_available) {
		this.cent_available = cent_available;
	}

	public String getCent_total() {
		return cent_total;
	}

	public void setCent_total(String cent_total) {
		this.cent_total = cent_total;
	}

	public String getCan_cashcard() {
		return can_cashcard;
	}

	public void setCan_cashcard(String can_cashcard) {
		this.can_cashcard = can_cashcard;
	}

	public String getCan_coupon() {
		return can_coupon;
	}

	public void setCan_coupon(String can_coupon) {
		this.can_coupon = can_coupon;
	}

	public String getCan_cent() {
		return can_cent;
	}

	public void setCan_cent(String can_cent) {
		this.can_cent = can_cent;
	}

	public List<String> getMembertag() {
		return membertag;
	}

	public void setMembertag(List<String> membertag) {
		this.membertag = membertag;
	}

	public List<String> getBehaviortag() {
		return behaviortag;
	}

	public void setBehaviortag(List<String> behaviortag) {
		this.behaviortag = behaviortag;
	}

	public List<String> getSaletag() {
		return saletag;
	}

	public void setSaletag(List<String> saletag) {
		this.saletag = saletag;
	}

}
