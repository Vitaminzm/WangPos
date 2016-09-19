package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 用于保存用户登录用户名 simple introduction
 *
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月28日
 * @see
 * @since 1.0
 */
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 用户id */
	public String personid;

	/** 用户名 */
	public String rydm;

	/** 用户密码 */
	public String password;
	
	/** 用户姓名 */
	public String personname;

	public String getPersonname() {
		return personname;
	}

	public void setPersonname(String personname) {
		this.personname = personname;
	}

	public String getPersonid() {
		return personid;
	}

	public void setPersonid(String personid) {
		this.personid = personid;
	}

	public String getRydm() {
		return rydm;
	}

	public void setRydm(String rydm) {
		this.rydm = rydm;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	
}
