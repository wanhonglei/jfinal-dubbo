package com.kakarote.crm9.utils;

import java.math.BigDecimal;

public class ReportUtil {

	/**
	 * 省市区格式化
	 * @param address
	 * @return
	 */
	public static String formatAddress(String address) {
		
		return address==null || "".equals(address)? "" : ReportUtil.replaceOnlyLast(address, ",", "").replace(",区", "").replace("[]","");
		
	}

	/**
	 * 替换最后一个匹配到的字符串
	 * @param string
	 * @param toReplace
	 * @param replacement
	 * @return
	 */
	public static String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	                + replacement
	                + string.substring(pos + toReplace.length(), string.length());
	    } else {
	        return string;
	    }
	}
	/**
	 * 替换最后一个匹配到的字符串
	 * @param string
	 * @param toReplace
	 * @param replacement
	 * @return
	 */
	public static String replaceOnlyLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos == string.length()-1) {
	        return string.substring(0, pos)
	                + replacement
	                + string.substring(pos + toReplace.length(), string.length());
	    } else {
	        return string;
	    }
	}

	/**
	 * 金额格式化
	 * @param adress
	 * @return
	 */
	public static String formatMoney(String money) {
		
		try {
			return new BigDecimal(money).setScale(2,BigDecimal.ROUND_HALF_UP).toString();
		} catch (Exception e) {
			return BigDecimal.ZERO.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
		}
		
	}

	/**
	 * 金额格式化0.00
	 * @param adress
	 * @return
	 */
	public static String formatMoney(BigDecimal money) {
		
		try {
			return money.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
		} catch (Exception e) {
			return BigDecimal.ZERO.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
		}
		
	}
	
	public static void main(String[] args) {
		
		System.out.print(ReportUtil.replaceOnlyLast("上海市,上海市,", ",", ""));
	}
}
