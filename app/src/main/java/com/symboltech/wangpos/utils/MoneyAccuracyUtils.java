package com.symboltech.wangpos.utils;

import java.text.DecimalFormat;

/**
 * 进度处理工具
 * 
 * @author cwi-apst E-mail: 26873204@qq.com
 * @date 创建时间：2015年11月4日 下午8:06:33
 * @version 1.0
 */
public class MoneyAccuracyUtils {

	/**
	 * 三方支付输入金额处理
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param money
	 * @return
	 */
	public static String thirdpaymoneydealbyinput(String money) {
		try {
			if (!StringUtil.isEmpty(money)) {
				DecimalFormat fnum = new DecimalFormat("0.00");
				return fnum.format(Double.parseDouble(money)).replace(".", "");
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 三方支付输出金额处理
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param money
	 * @return
	 */
	public static String thirdpaymoneydealbyoutput(String money) {
		try {
			if (!StringUtil.isEmpty(money)) {
				DecimalFormat fnum = new DecimalFormat("0.00");
				return decimalsutils(fnum.format(Double.parseDouble(money) / 100));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 去除小数点后面无用的0
	 * 
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param decimals
	 * @return
	 */
	public static String decimalsutils(String decimals) {
		if (decimals != null && !"".equals(decimals) && decimals.indexOf(".") > 0) {
			// 正则表达
			decimals = decimals.replaceAll("0+?$", "");// 去掉后面无用的零
			decimals = decimals.replaceAll("[.]$", "");// 如小数点后面全是零则去掉小数点
			return decimals;
		} else if (decimals != null && !"".equals(decimals)) {
			return decimals;
		}
		return "";

	}
	
	public static String getmoneybyone(double money) {
		try {
			DecimalFormat fnum = new DecimalFormat("0.0");
			fnum.setGroupingUsed(false);
			return fnum.format(money);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getmoneybytwo(double money) {
		try {
			DecimalFormat fnum = new DecimalFormat("0.00");
			fnum.setGroupingUsed(false);
			return fnum.format(money);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 格式化字符串成两位小数
	 * @param money
	 * @return
	 */
	public static String formatMoneyByTwo(String money) {
		try {
			DecimalFormat fnum = new DecimalFormat("0.00");
			fnum.setGroupingUsed(false);
			return fnum.format(Double.parseDouble(money));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 判断金额是否正确
	 * @author CWI-APST email:26873204@qq.com
	 * @Description: TODO
	 * @param money
	 * @return
	 */
	public static boolean IsMoneyCorrect(String money){
		if(!StringUtil.isEmpty(money)){
			try {
				if(!StringUtil.isEmpty(formatMoneyByTwo(money)) && Double.parseDouble(formatMoneyByTwo(money)) > 0){
					return true;
				}else {
					return false;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}else {
			return false;
		}
	}

	/**
	 * 转换成真实金额
	 * @return
	 */
	public static String makeRealAmount(String amount) {
		if(amount == null) {
			return "0.00";
		}
		StringBuilder sb = new StringBuilder();
		boolean flag = false;
		char[] array = amount.toCharArray();
		//去掉前面的0
		for (int i = 0; i < array.length; i++) {
			if(!flag) {
				if(array[i] > '0') {
					sb.append(array[i]);
					flag = true;
				}else {
					continue;
				}
			}else {
				sb.append(array[i]);
			}
		}
		try {
			DecimalFormat fnum = new DecimalFormat("0.00");
			return fnum.format(Double.parseDouble(sb.toString()) / 100);
		} catch (Exception e) {
		}
		return "0.00";
	}
}
