package com.symboltech.wangpos.msg.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 登录信息
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class LoginInfo implements Serializable{
	/** TODO*/
	private static final long serialVersionUID = 1L;

	private String billid;

	@SerializedName("configinfolist")
	private List<ConfigList> configlists;
	private String person_id;
	private String person_name;
	private String personcode;
	private String posno;
	private String serviceurl = ""; //修改本地IP地址

	public String getServiceurl() {
		return serviceurl;
	}

	public void setServiceurl(String serviceurl) {
		this.serviceurl = serviceurl;
	}

	@SerializedName("loginpresonlist")
	private ArrayList<UserInfo> userlists;
	
	public ArrayList<UserInfo> getUserlists() {
		return userlists;
	}

	public void setUserlists(ArrayList<UserInfo> userlists) {
		this.userlists = userlists;
	}

	@SerializedName("shopmsg")
	private ShopInfo shopinfo;
	@SerializedName("mallmsg")
	private ShopInfo mallinfo;
	
	private String token;
	private String work_type;

	public String getBillid() {
		return this.billid;
	}

	
	public ShopInfo getMallinfo() {
		return mallinfo;
	}

	public void setMallinfo(ShopInfo mallinfo) {
		this.mallinfo = mallinfo;
	}

	public List<ConfigList> getConfiglists() {
		return this.configlists;
	}

	public String getPerson_id() {
		return this.person_id;
	}

	public String getPerson_name() {
		return this.person_name;
	}

	public String getPersoncode() {
		return this.personcode;
	}

	public String getPosno() {
		return this.posno;
	}

	public ShopInfo getShopinfo() {
		return this.shopinfo;
	}

	public String getToken() {
		return this.token;
	}

	public String getWork_type() {
		return this.work_type;
	}

	public void setBillid(String paramString) {
		this.billid = paramString;
	}

	public void setConfiglists(List<ConfigList> paramArrayList) {
		this.configlists = paramArrayList;
	}

	public void setPerson_id(String paramString) {
		this.person_id = paramString;
	}

	public void setPerson_name(String paramString) {
		this.person_name = paramString;
	}

	public void setPersoncode(String paramString) {
		this.personcode = paramString;
	}

	public void setPosno(String paramString) {
		this.posno = paramString;
	}

	public void setShopinfo(ShopInfo paramShopInfo) {
		this.shopinfo = paramShopInfo;
	}

	public void setToken(String paramString) {
		this.token = paramString;
	}

	public void setWork_type(String paramString) {
		this.work_type = paramString;
	}
}
