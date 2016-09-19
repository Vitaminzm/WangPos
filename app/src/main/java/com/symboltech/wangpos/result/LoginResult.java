package com.symboltech.wangpos.result;

import com.google.gson.annotations.SerializedName;
import com.symboltech.wangpos.msg.entity.LoginInfo;

/**
 * loginresult
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class LoginResult extends BaseResult {

	@SerializedName("data")
	private LoginInfo logininfo;

	public LoginInfo getLogininfo() {
		return this.logininfo;
	}

	public void setLogininfo(LoginInfo paramLoginInfo) {
		this.logininfo = paramLoginInfo;
	}
}
