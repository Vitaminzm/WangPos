package com.symboltech.wangpos.msg.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


/**
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年10月27日 下午5:44:34
 * @version 1.0
 */
public class CashierInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getCashierid() {
		return cashierid;
	}

	public void setCashierid(String cashierid) {
		this.cashierid = cashierid;
	}

	public String getCashiername() {
		return cashiername;
	}

	public void setCashiername(String cashiername) {
		this.cashiername = cashiername;
	}

	public String getCashiercode() {
		return cashiercode;
	}

	public void setCashiercode(String cashiercode) {
		this.cashiercode = cashiercode;
	}

	public String getWork_type() {
		return work_type;
	}

	public void setWork_type(String work_type) {
		this.work_type = work_type;
	}

	public String getPersonxtm() {
		return personxtm;
	}

	public void setPersonxtm(String personxtm) {
		this.personxtm = personxtm;
	}

	@SerializedName("person_id")
	private String cashierid;

	@SerializedName("person_name")
	private String cashiername;

	@SerializedName("personcode")
	private String cashiercode;

	/** 用户系统码 */
	public String personxtm;
	private String work_type;
}
