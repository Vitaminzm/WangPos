package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 车牌信息
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class CarInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	private String carnum;
	private String cartype;
	private String primaryflag;
	
	public String getCarnum() {
		return carnum;
	}
	public void setCarnum(String carnum) {
		this.carnum = carnum;
	}
	public String getCartype() {
		return cartype;
	}
	public void setCartype(String cartype) {
		this.cartype = cartype;
	}
	public String getPrimaryflag() {
		return primaryflag;
	}
	public void setPrimaryflag(String primaryflag) {
		this.primaryflag = primaryflag;
	}
	
}
