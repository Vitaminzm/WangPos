package com.symboltech.wangpos.result;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * gson 映射基础类
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月9日
 * @see
 * @since 1.0
 */
public class BaseResult implements Serializable{

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Expose
	public static final String SUCCESS = "00";

	@SerializedName("retcode")
	private String code;

	@SerializedName("retmsg")
	private String msg;

}