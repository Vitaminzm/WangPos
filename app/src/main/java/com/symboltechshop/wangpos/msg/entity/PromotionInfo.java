package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 促销info
 * 
 * @author so
 * 
 */
public class PromotionInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id; // 促销ID
	private String promname; // 促销主题
	private String description; // 促销描述
	private String status; // 0 new 1 普通

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPromname() {
		return promname;
	}

	public void setPromname(String promname) {
		this.promname = promname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
