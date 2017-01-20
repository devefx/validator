package com.devefx.validation.kit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StrKit.
 */
public class StrKit {

    public static String firstCharToLowerCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] arr = str.toCharArray();
            arr[0] += ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    public static String firstCharToUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] arr = str.toCharArray();
            arr[0] -= ('a' - 'A');
            return new String(arr);
        }
        return str;
    }

    public static boolean isBlank(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }
    
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    public static String format(String pattern, Object ... arguments) {
        if (arguments.length > 0) {
            Matcher matcher = Pattern.compile("\\{(\\d+)\\}").matcher(pattern);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String str = matcher.group(1);
                try {
                    int i = Integer.parseInt(str);
                    if (i < arguments.length) {
                        matcher.appendReplacement(buffer, ""+arguments[i]);
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("can't parse argument number: " + str);
                }
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        }
        return pattern;
    }
    
}
