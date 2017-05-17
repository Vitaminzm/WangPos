package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月4日 下午2:51:41
 * @version 1.0
 */
public class ThirdPayInfo implements Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;
	/** 授权码 */
	private String auth_code;
	/** 收款员 */
	private String sky;
	/** 总金额 */
	private String je;
	/** 收款方式id */
	private String skfsid;

	public String getAuth_code() {
		return auth_code;
	}

	public void setAuth_code(String auth_code) {
		this.auth_code = auth_code;
	}

	public String getSky() {
		return sky;
	}

	public void setSky(String sky) {
		this.sky = sky;
	}

	public String getJe() {
		return je;
	}

	public void setJe(String je) {
		this.je = je;
	}

	public String getSkfsid() {
		return skfsid;
	}

	public void setSkfsid(String skfsid) {
		this.skfsid = skfsid;
	}

	@Override
	public ThirdPayInfo clone() {
		ThirdPayInfo clone = null;
		try {
			clone = (ThirdPayInfo) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clone;
	}

}
