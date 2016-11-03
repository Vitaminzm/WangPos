package com.symboltech.wangpos.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * sharedPreferences utils simple introduction
 * 
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月27日
 * @see
 * @since 1.0
 */
public class SpSaveUtils {

	public static final String SAVE_FOR_SP_KEY = "SaveForSharedPreferences";

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(saveObject by SharedPreferences )
	 * @param context
	 * @param key
	 * @param obj
	 */
	public static void saveObject(Context context, String key, Object obj) {
		SharedPreferences sp = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		Editor edit = sp.edit();
		edit.putString(key, serialize(obj));
		edit.commit();
	}

	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(getObject by SharedPreferences)
	 * @param context
	 * @param key
	 * @return
	 */
	public static Object getObject(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		return deSerialization(sp.getString(key, null));
	}

	/**
	 * 读取默认的sp中键对应的值
	 * 
	 * @param context
	 * @param key
	 * @param Default
	 * @return
	 */
	public static String read(Context context, String key, String Default) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		String value = sharedPreferences.getString(key, Default);
		return value;
	}
	
	/**
	 * 读取默认的sp中键对应的值  return boolean
	 * 
	 * @param context
	 * @param key
	 * @param Default
	 * @return
	 */
	public static boolean readboolean(Context context, String key, boolean Default) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		boolean value = sharedPreferences.getBoolean(key, Default);
		return value;
	}

	/**
	 * 写入默认的sp中的键和值
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void write(Context context, String key, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * 写入默认的sp中的键和值  writeboolean
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void writeboolean(Context context, String key, boolean value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 删除指定的键的值
	 * 
	 * @param context
	 * @param key
	 *            指定的键
	 */
	public static void delete(Context context, String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.remove(key);
		editor.commit();
	}
	

	/**
	 * 读取相应的存储的xml文件 ，获取对应的值
	 * 
	 * @param context
	 * @param xmlname
	 * @param key
	 * @param Default
	 * @return
	 */

	public static String read(Context context, String xmlname, String key, String Default) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(xmlname, Context.MODE_PRIVATE);
		String value = sharedPreferences.getString(key, Default);
		return value;
	}

	/**
	 * 指定xml名字 ，并写入相应的键值
	 * 
	 * @param context
	 * @param xmlname
	 * @param key
	 * @param value
	 */
	public static void write(Context context, String xmlname, String key, String value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(xmlname, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 读取相应的存储的xml文件 ，获取对应的值
	 *
	 * @param context
	 * @param key
	 * @param Default
	 * @return
	 */

	public static int readInt(Context context, String key, int Default) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		int value = sharedPreferences.getInt(key, Default);
		return value;
	}

	/**
	 * 指定xml名字 ，并写入相应的键值
	 *
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void writeInt(Context context, String key, int value) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SAVE_FOR_SP_KEY, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	/**
	 * 删除指定的xml文件中对应的键的值
	 * 
	 * @param context
	 * @param xmlname
	 * @param key
	 */
	public static void delete(Context context, String xmlname, String key) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(xmlname, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.remove(key);
		editor.commit();
	}

	/**
	 * 删除指定的xml文件中所有保存的数据
	 * 
	 * @param context
	 * @param xmlname
	 */
	public static void deleteAll(Context context, String xmlname) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(xmlname, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * 序列化对象
	 * 
	 * @param
	 * @return
	 * @throws IOException
	 */
	public static String serialize(Object obj) {
		String serStr = null;
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(obj);
			serStr = byteArrayOutputStream.toString("ISO-8859-1");
			serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
			objectOutputStream.close();
			byteArrayOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serStr;
	}

	/**
	 * 反序列化对象
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deSerialization(String str) {
		Object obj = null;
		if(str == null){
			return obj;
		}
		try {
			String redStr = java.net.URLDecoder.decode(str, "UTF-8");
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			obj = objectInputStream.readObject();
			objectInputStream.close();
			byteArrayInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
//			Log.v("lgs", e.getMessage());
		}
		return obj;
	}

}
