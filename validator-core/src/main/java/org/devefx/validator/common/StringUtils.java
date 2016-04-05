package org.devefx.validator.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * 字符串帮助类
 * @author： youqian.yue
 * @date： 2015-12-18 上午10:30:09
 */
public class StringUtils {
	
	private static final String lineSeparator = System.getProperty("line.separator", "\n");
	/**
	 * 文本中${name}的形式将被替换成map中key=name的value
	 * @param text
	 * @param parameter
	 * @return String
	 */
	public static String format(String text, Map<String, Object> parameter) {
		final String OPEN = "${";
		final String CLOSE = "}";
		
		String newText = text;
		if (text != null && parameter != null) {
			int start = newText.indexOf(OPEN);
			int end = newText.indexOf(CLOSE, start);
			
			while (start > -1 && end > start) {
				String prepend = newText.substring(0, start);
				String append = newText.substring(end + CLOSE.length());
				String propName = newText.substring(start + OPEN.length(), end);
				Object propValue = parameter.get(propName);
				if (propValue == null) {
					newText = prepend + propName + append;
				} else {
					newText = prepend + propValue + append;
				}
				start = newText.indexOf(OPEN);
				end = newText.indexOf(CLOSE, start);
			}
		}
		return newText;
	}
	/**
	 * 读取InputStream中的文本，方法将自动关闭流
	 * @param is
	 * @param charsetName
	 * @return String
	 */
	public static String reader(InputStream is, String charsetName) throws IOException {
		if (is != null) {
			String line = null;
			Reader reader = null;
			BufferedReader bufferedReader = null;
			StringBuffer buf = new StringBuffer();
			try {
				reader = new InputStreamReader(is);
				bufferedReader = new BufferedReader(reader);
				while ((line = bufferedReader.readLine()) != null) {
					if (buf.length() != 0) {
						buf.append(lineSeparator);
					}
					buf.append(line);
				}
			} finally {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				if (reader != null) {
					reader.close();
				}
				is.close();
			}
			return buf.toString();
		}
		return null;
	}
}
