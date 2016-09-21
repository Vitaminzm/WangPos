package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 操作日志返回信息
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm
 * @see
 * @since 1.0
 */
public class LogInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String signid;

	private String succeednum;

	public String getSignid() {
		return signid;
	}

	public void setSignid(String signid) {
		this.signid = signid;
	}

	public String getSucceednum() {
		return succeednum;
	}

	public void setSucceednum(String succeednum) {
		this.succeednum = succeednum;
	}
	
}
