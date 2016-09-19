package com.symboltech.wangpos.utils;

import com.symboltech.wangpos.app.AppConfigFile;
import com.symboltech.wangpos.db.dao.UserNameDao;
import com.symboltech.wangpos.msg.entity.DBUserInfo;

import java.util.ArrayList;

/**
 * 二次处理dao数据
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年10月28日 下午3:13:46
 * @version 1.0
 */
public class DButils {

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(getUserNames by db)
	 * @param und
	 * @return
	 */
	public static ArrayList<String> getUserNames(UserNameDao und) {
		ArrayList<DBUserInfo> alist = und.findNidAll();
		ArrayList<String> username = new ArrayList<String>();
		long temp = System.currentTimeMillis();
		for (DBUserInfo dbu : alist) {
			if (temp - dbu.creattime < AppConfigFile.USER_LOGIN_SAVE_TIME) {
				username.add(dbu.name);
			}
		}

		if (username.size() >= 3) {
			return username;
		} else if (username.size() == 0) {
			return username;
		} else if (username.size() == 1) {
			username.add("");
			username.add("");
			return username;
		} else if (username.size() == 2) {
			username.add("");
			return username;
		}
		return username;
	}
}
