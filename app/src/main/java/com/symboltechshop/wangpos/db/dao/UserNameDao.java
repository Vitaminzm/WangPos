package com.symboltechshop.wangpos.db.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.symboltechshop.wangpos.db.SymboltechMallDBOpenHelper;
import com.symboltechshop.wangpos.msg.entity.DBUserInfo;
import com.symboltechshop.wangpos.utils.StringUtil;

import java.util.ArrayList;

/**
 * 保存登录信息
 * simple introduction
 *
 * <p>detailed comment
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年11月2日
 * @see
 * @since 1.0
 */
public class UserNameDao {

	private SymboltechMallDBOpenHelper.DatabaseHelper helper;

	private Context context;

	public UserNameDao(Context context) {
		SymboltechMallDBOpenHelper dbutil = new SymboltechMallDBOpenHelper(context);
		helper = dbutil.getDatabaseHelper();
		this.context = context;
	}

	/**
	 * 查找是否有重复的名字
	 * 
	 * @param name
	 *            名字
	 * @return
	 */
	public boolean find(String name) {
		boolean result = false;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from login_user_name where username = ?", new String[] { name });
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
	 * @param username
	 */
	public void add(String username) {
		if (!StringUtil.isEmpty(username)) {
			if (find(username)) {
				update(username);
			} else {
				SQLiteDatabase db = helper.getWritableDatabase();

				db.execSQL("insert into login_user_name (username, creattime) values (?, ?)",
						new Object[] { username, System.currentTimeMillis() });
				db.close();
			}
		}
	}

	/**
	 * 根据用户名删除一个用户
	 * 
	 * @param username
	 *            用户名
	 */
	public void delete(String username) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from login_user_name where username = ?", new Object[] { username });
		db.close();
	}

	/**
	 * 根据用户名，更新当前用户登录时间
	 * 
	 * @param name
	 *            用户名
	 */
	public void update(String name) {

		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("update login_user_name set creattime = ? where username=?",
				new Object[] { System.currentTimeMillis(), name });
		db.close();
	}

	/**
	 * 查询用户登录信息
	 * 
	 * @return ArrayLIst<DBUserINfo>
	 */
	public ArrayList<DBUserInfo> findNidAll() {
		DBUserInfo dbui;
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select username, creattime from login_user_name ", null);
		ArrayList<DBUserInfo> numbers = new ArrayList<DBUserInfo>();
		while (cursor.moveToNext()) {
			dbui = new DBUserInfo();
			dbui.name = cursor.getString(0);
			dbui.creattime = cursor.getLong(1);
			numbers.add(dbui);
		}
		cursor.close();
		db.close();
		return numbers;
	}

}
