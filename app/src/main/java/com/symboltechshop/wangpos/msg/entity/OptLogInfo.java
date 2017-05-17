package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 促销info
 * 
 * @author so
 * 
 */
public class OptLogInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String operCode; // 操作员ID
	private String opType; // 操作类型（1登录系统、2退出系统、3锁屏、4切换款员）
	private String opMsg; // 操作描述
	private String opTime;// 操作时间
	
	public String getOperCode() {
		return operCode;
	}
	public void setOperCode(String operCode) {
		this.operCode = operCode;
	}
	public String getOpType() {
		return opType;
	}
	public void setOpType(String opType) {
		this.opType = opType;
	}
	public String getOpMsg() {
		return opMsg;
	}
	public void setOpMsg(String opMsg) {
		this.opMsg = opMsg;
	}
	public String getOpTime() {
		return opTime;
	}
	public void setOpTime(String opTime) {
		this.opTime = opTime;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj) {
			return true;
		}
		if (obj instanceof OptLogInfo) {
			OptLogInfo other = (OptLogInfo) obj;
			return (other.opTime).equals(this.opTime);
		}
		return false;
	}

}
