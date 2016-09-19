package com.symboltech.wangpos.msg.entity;

import java.io.Serializable;

/**
 * 用于保存用户登录用户名
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月28日
 * @see
 * @since 1.0
 */
public class DBUserInfo implements Serializable{
	
	/**用户名*/
	public String name;
	
	/**最后登录时间*/
	public long creattime;

}
