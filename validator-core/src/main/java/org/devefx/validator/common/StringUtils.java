package org.devefx.validator.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串帮助类
 * @author： youqian.yue
 * @date： 2015-12-18 上午10:30:09
 */
public class StringUtils {
	/**
	 * 文本中${name}的形式将被替换成map中key=name的value
	 * @param text
	 * @param parameter
	 * @return String
	 */
	public static String format(String text, Map<String, Object> parameter) {
		StringBuffer sb = new StringBuffer(text.length());
		Pattern pattern = Pattern.compile("\\$\\{([a-zA-Z_]+)\\}");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			Object value = parameter.get(matcher.group(1));
			matcher.appendReplacement(sb, value == null ? "" : value.toString());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
	/**
	 * 读取InputStream中的文本，方法将自动关闭流
	 * @param is
	 * @param charsetName
	 * @return String
	 */
	public static String reader(InputStream is, String charsetName) {
		if (is != null) {
			StringBuffer sb = new StringBuffer();
			try {
				int off = 0, len;
				byte[] bytes = new byte[1024];
				while ((len = is.read(bytes, off, 1024)) != -1) {
					sb.append(new String(bytes, 0, len, charsetName));
				}
			} catch (IOException e) {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e2) {
					}
				}
			}
			return sb.toString();
		}
		return null;
	}
}
