package com.symboltechshop.wangpos.msg.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 脱机上传返回信息
 * simple introduction
 *
 * <p>detailed comment
 * @author zmm
 * @see
 * @since 1.0
 */
public class OfflineDataInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;
	
	private List<String> sucbillidlist;

	private String sucnum;

	public List<String> getSucbillidlist() {
		return sucbillidlist;
	}

	public void setSucbillidlist(List<String> sucbillidlist) {
		this.sucbillidlist = sucbillidlist;
	}

	public String getSucnum() {
		return sucnum;
	}

	public void setSucnum(String sucnum) {
		this.sucnum = sucnum;
	}
}
