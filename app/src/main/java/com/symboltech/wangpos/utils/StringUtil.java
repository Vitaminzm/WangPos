package com.symboltech.wangpos.utils;

/**
 * String utils simple introduction
 *
 * <p>
 * detailed comment
 * 
 * @author CWI-APST Email:lei.zhang@symboltech.com 2015年10月26日
 * @see
 * @since 1.0
 */
public class StringUtil {
	
	/**
	 * 
	 * @author CWI-APST emial:26873204@qq.com
	 * @Description: TODO(getclassname no contains packagename)
	 * @param classname packagename.classname
	 * @return
	 */
	public static String getclassname(String classname){
		if(classname.contains(".") && classname.lastIndexOf(".") + 2 < classname.length()){
			return classname.substring(classname.lastIndexOf(".")+1, classname.length());
		}
		return classname;
	}
	
	
	/**
	 * 保留18字符
	 * 
	 * @param printText
	 * @return
	 */
	public static String CutPrintutils(String printText) {
		if (printText.length() > 17) {
			return printText.substring(0, 17) + "...";
		} else {
			return printText;
		}
	}

	/**
	 * 判断字符串去除两端空格后是否为空串
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		if (str == null || str.equals("") || str.trim().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * 替换字符串中的换行符
	 * 
	 * @param src
	 * @return
	 */
	public static String insteadChangeLine(String src) {
		if (isEmpty(src)) {
			return null;
		} else {
			return src.replace("\r\n", "\n");
		}
	}

	/**
	 * 格式化字符串
	 * @param len 待格式长度
	 * @param str 待格式字符串
	 * @return
	 */
	public static String formatLString(int len, String str) {
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
