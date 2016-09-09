package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 退货原因info
 * 
 * @author so
 * 
 */
public class RefundReasonInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;// 退货原因ID
	private String name;// 退货原因内容

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
