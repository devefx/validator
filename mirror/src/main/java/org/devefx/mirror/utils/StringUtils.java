package org.devefx.mirror.utils;

/**
 * StringUtils
 * @author： youqian.yue
 * @date： 2015-11-10 下午6:10:57
 */
public class StringUtils {
	public static boolean isEmpty(String input) {
		return input == null || input.isEmpty();
	}
	public static boolean isNotEmpty(String input) {
		return !isEmpty(input);
	}
	public static boolean isBlank(String input) {
		return input == null || input.trim().length() == 0;
	}
	public static boolean isNotBlank(String input) {
		return !isBlank(input);
	}
	
	/**
	 * 首字母大写
	 * @param input
	 * @return String
	 */
	public static String firstToUpperCase(String input) {
		if (!isBlank(input)) {
			String firstStr = input.substring(0, 1).toUpperCase();
			if (input.length() > 1)
				return firstStr + input.substring(1);
			return firstStr;
		}
		return input;
	}
	/**
	 * 首字母小写
	 * @param input
	 * @return String
	 */
	public static String firstToLowerCase(String input) {
		if (!isBlank(input)) {
			String firstStr = input.substring(0, 1).toLowerCase();
			if (input.length() > 1)
				return firstStr + input.substring(1);
			return firstStr;
		}
		return input;
	}
}
