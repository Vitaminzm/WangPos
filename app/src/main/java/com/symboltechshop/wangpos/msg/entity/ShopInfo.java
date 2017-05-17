package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 店铺信息
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class ShopInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	private String code;
	private String id;
	private String name;

	public String getCode() {
		return this.code;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setCode(String paramString) {
		this.code = paramString;
	}

	public void setId(String paramString) {
		this.id = paramString;
	}

	public void setName(String paramString) {
		this.name = paramString;
	}
}
