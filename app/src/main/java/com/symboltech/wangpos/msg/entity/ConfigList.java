package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 配置config simple introduction
 *
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class ConfigList implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String desc;
	private String id;
	private String value;

	public String getDesc() {
		return this.desc;
	}

	public String getId() {
		return this.id;
	}

	public String getValue() {
		return this.value;
	}

	public void setDesc(String paramString) {
		this.desc = paramString;
	}

	public void setId(String paramString) {
		this.id = paramString;
	}

	public void setValue(String paramString) {
		this.value = paramString;
	}
}
