package com.symboltech.wangpos.db.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.symboltech.wangpos.db.SymboltechMallDBOpenHelper;
import com.symboltech.wangpos.msg.entity.LoginInfo;
import com.symboltech.wangpos.msg.entity.UserInfo;
import com.symboltech.wangpos.utils.StringUtil;

import java.util.List;

/**
 * 保存登录信息
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月2日
 * @see
 * @since 1.0
 */
public class LoginDao {

	private SymboltechMallDBOpenHelper.DatabaseHelper helper;

	private Context context;

	public LoginDao(Context context) {
		SymboltechMallDBOpenHelper dbutil = new SymboltechMallDBOpenHelper(context);
		helper = dbutil.getDatabaseHelper();
		this.context = context;
	}

	/**
	 * 查找是否有重复的名字
	 * 
	 * @param id
	 *            名字
	 * @return
	 */
	public boolean find(String id) {
		boolean result = false;
		if(id == null){
			return result;
		}
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("login", null, "rydm = ?", new String[] { id }, null, null, null);
		if (cursor.moveToFirst()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;

	}

	/**
	 * 添加一个新用户
	 * 
	 */
	public void add(String userid ,String username, String password, String personname) {
		if (!StringUtil.isEmpty(username) && !StringUtil.isEmpty(userid)&& !StringUtil.isEmpty(password)&& !StringUtil.isEmpty(personname)) {
			if (find(username)) {
				update(userid,username, password);
			} else {
				SQLiteDatabase db = helper.getWritableDatabase();
				ContentValues values=new ContentValues();
				values.put("personid", userid);
				values.put("rydm", username);
				values.put("password", password);
				values.put("personname", personname);
				db.insert("login", null, values);
				db.close();
			}
		}
	}

	/**
	 * 添加一个新用户
	 * 
	 * @param users
	 */
	public boolean add(List<UserInfo> users) {
		boolean ret = true;
		if(users == null && users.size() <=0 ){
			return false;
		}
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		db.delete("login", null, null);
		for (UserInfo user: users) {
			ContentValues values=new ContentValues();
			values.put("personid", user.getPersonid());
			values.put("rydm", user.getRydm());
			values.put("personname", user.getPersonname());
			values.put("password", user.getPassword().toLowerCase());
			long success = -1;
			success = db.insert("login", null, values);
			if(success == -1){
				ret = false;
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return ret;
	}
	/**
	 * 根据用户名删除一个用户
	 * 
	 * @param userid
	 *            用户名
	 */
	public void delete(String userid) {
		if(!StringUtil.isEmpty(userid)){
			SQLiteDatabase db = helper.getWritableDatabase();
			db.delete("login", "rydm = ?", new String[]{ userid });
			db.close();
		}
	}

	/**
	 * 根据用户id，更新当前用户登录密码
	 * 
	 * @param name
	 *            用户id
	 */
	public boolean update(String id, String name, String password) {
		boolean ret = false;
		if(!StringUtil.isEmpty(id) && !StringUtil.isEmpty(password) && !StringUtil.isEmpty(name)){
			SQLiteDatabase db = helper.getWritableDatabase();
			ContentValues values=new ContentValues();
			values.put("password", password);
			values.put("rydm", name);
			int success = 0;
			success = db.update("login", values, "rydm = ?", new String[]{ id });
			db.close();
			if(success == 1){
				ret = true;
			}
		}
		return ret;
	}

	public boolean unlock(String username, String password){
		boolean result = false;
		if(StringUtil.isEmpty(username) || StringUtil.isEmpty(password)){
			return result;
		}
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("login", null, "rydm = ? and password = ?", new String[] { username, password.toLowerCase() }, null, null, null);
		if (cursor.moveToFirst()) {
			result = true;
		}
		cursor.close();
		db.close();
		return result;
	}
	
	public LoginInfo login(String username, String password){
		LoginInfo info = null;
		if(StringUtil.isEmpty(username) || StringUtil.isEmpty(password)){
			return info;
		}
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query("login", null, "rydm = ? and password = ?", new String[] { username, password.toLowerCase() }, null, null, null);
		if (cursor.moveToFirst()) {
			if(info == null) {
				info = new LoginInfo();
			}
			info.setPerson_name(cursor.getString(cursor.getColumnIndex("personname")));
			info.setPerson_id(cursor.getString(cursor.getColumnIndex("personid")));
			info.setPersoncode(cursor.getString(cursor.getColumnIndex("rydm")));
		}
		cursor.close();
		db.close();
		return info;
	}

}
