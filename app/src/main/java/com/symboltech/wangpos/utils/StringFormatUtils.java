package com.symboltech.wangpos.utils;

/**
 * 字符串格式化工具类
 * @author so
 *
 */
public class StringFormatUtils {

	/**
	 * 格式化字符串
	 * @param len 待格式长度
	 * @param str 待格式字符串
	 * @return
	 */
	public static String formatString(int len, String str) {
		if(str == null){
			str = "null";
		}
//		if(str.length() > len) {
//			str = str.substring(0, len);
//		}
		return String.format("%-"+ len +"s", str);
	}
	
	public static String formatRString(int len, String str) {
		if(str == null){
			str = "null";
		}
		return String.format("%"+ len +"s", str);
	}
}
