package com.symboltech.wangpos.utils;

import java.math.BigDecimal;

import android.R.integer;

public final class CurrencyUnit {

	/**
	 * 货币单位转换，分转为元
	 * 
	 * @param fen
	 *            以分为单位的金额字符串
	 * @return 以元为单位的金额字符串
	 */
	public static String fen2yuanStr(String fen) {
		if (null == fen || fen.isEmpty()) {
			fen = "0";
		}
		double double_fen = Double.parseDouble(String.valueOf((long) Double
				.parseDouble(fen)));
		BigDecimal bd = new BigDecimal(double_fen / 100.0);
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
		BigDecimal valWan = new BigDecimal(10000);
		if (bd.compareTo(valWan) > 0) {
			bd = new BigDecimal(double_fen / 1000000.0);
			bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
			return bd.toString() + "万";
		}
		return bd.toString();
	}

	/**
	 * format amount yuan to fen
	 * 
	 * @param yuan
	 * @return
	 */
	public static String yuan2fenStr(String yuan) {
		if (!yuan.contains(".")) {
			yuan += ".00";
		} else {
			int yuanLength = yuan.length();
			int yuanDotPosition = yuan.indexOf(".");
			if (yuanDotPosition == 0) {
				yuan = "0" + yuan;
				yuanLength = yuan.length();
				yuanDotPosition = yuan.indexOf(".");
			}
			if (yuanDotPosition == yuanLength - 1) {
				yuan += "00";
			} else if (yuanDotPosition == yuanLength - 2) {
				yuan += "0";
			} else if (yuanLength - yuanDotPosition > 3) {
				yuan = yuan.substring(0, yuanDotPosition + 3);
			}
		}
		
		String fenAmount = yuan.replace(".", "");
		fenAmount = ""+Integer.valueOf(fenAmount);

		return fenAmount;
	}

	/**
	 * format the str which unit is yuan
	 * 
	 * @param yuan
	 * @return
	 */
	public static String formatYuanStr(String yuan) {
		if (!yuan.contains(".")) {
			yuan += ".00";
		} else {
			int yuanLength = yuan.length();
			int yuanDotPosition = yuan.indexOf(".");
			if (yuanDotPosition == 0) {
				yuan = "0" + yuan;
				yuanLength = yuan.length();
				yuanDotPosition = yuan.indexOf(".");
			}
			if (yuanDotPosition == yuanLength - 1) {
				yuan += "00";
			} else if (yuanDotPosition == yuanLength - 2) {
				yuan += "0";
			} else if (yuanLength - yuanDotPosition > 3) {
				yuan = yuan.substring(0, yuanDotPosition + 3);
			}
		}
		return yuan;
	}
}
