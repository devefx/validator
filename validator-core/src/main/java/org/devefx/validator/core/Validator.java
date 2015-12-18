package org.devefx.validator.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devefx.validator.common.JsonResult;
import org.devefx.validator.common.StringUtils;

public abstract class Validator {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	private boolean invalid = false;
	private boolean shortCircuit = false;
	private String datePattern = null;
	private boolean scriptMode = false;
	private List<String> javascript = new ArrayList<String>();
	private String scriptText;
	private Map<String, String> error = new HashMap<String, String>();
	
	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	private static final String emailAddressPattern = "\\b(^['_A-Za-z0-9-]+(\\.['_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";
	private static final String mobileNumberPattern = "^(0|86|17951)?(13[0-9]|15[0-9]|17[678]|18[0-9]|14[57])[0-9]{8}$";
	
	protected void setShortCircuit(boolean shortCircuit) {
		if (scriptMode) {
			addScript(String.format("setShortCircuit(%b);", shortCircuit));
			return;
		}
		this.shortCircuit = shortCircuit;
	}
	
	protected void setDatePattern(String datePattern) {
		if (scriptMode) {
			addScript(String.format("setDatePattern('%s');", datePattern));
			return;
		}
		this.datePattern = datePattern;
	}
	
	protected String getDatePattern() {
		return (datePattern != null ? datePattern : DEFAULT_DATE_PATTERN);
	}
	
	protected Map<String, String> getError() {
		return error;
	}
	
	private void addScript(String script) {
		javascript.add(script);
	}
	
	protected void render(String viewName) {
		try {
			response.sendRedirect(viewName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void renderJSONError() {
		JsonResult jsonResult = new JsonResult();
		jsonResult.put("success", !this.invalid);
		jsonResult.put("error", getError());
		try {
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.print(jsonResult.toString());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void output(Writer out) {
		try {
			if (this.scriptText == null) {
				Validator validator = getClass().newInstance();
				validator.scriptMode = true;
				validator.validate(null, null);

				String token = "";
				StringBuffer script = new StringBuffer();
				for (String s : validator.javascript) {
					script.append(token);
					script.append("\t\t\tthis.");
					script.append(s);
					token = "\n";
				}
				Map<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("script", script.toString());
				parameter.put("name", getClass().getSimpleName());
				
				InputStream is = getClass().getClassLoader().getResourceAsStream("validator.vm");
				this.scriptText = StringUtils.format(StringUtils.reader(is, "utf-8"), parameter);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		PrintWriter printWriter = (PrintWriter) out;
		printWriter.write(scriptText);
		printWriter.flush();
	}
	
	final public boolean process(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		try {
			this.validate(request, response);
		} catch (ValidateException e) {}
		if (this.invalid) {
			this.handleError(request, response);
			return false;
		}
		return true;
	}
	
	protected abstract void validate(HttpServletRequest request, HttpServletResponse response);
	
	protected abstract void handleError(HttpServletRequest request, HttpServletResponse response);
	
	private void addError(String errorKey, String errorMessage) {
		invalid = true;
		if (!error.containsKey(errorKey)) {
			request.setAttribute(errorKey, errorMessage);
			error.put(errorKey, errorMessage);
		}
		if (shortCircuit) {
			throw new ValidateException();
		}
	}
	
	protected void validateRequired(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateRequired('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value))
			addError(errorKey, errorMessage);
	}
	
	protected void validateRequiredString(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateRequiredString('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim()))
			addError(errorKey, errorMessage);
	}
	
	protected void validateInteger(String field, int min, int max, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateInteger('%s', %d, %d, '%s', '%s');", field, min, max, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			int temp = Integer.parseInt(value.trim());
			if (temp < min || temp > max)
				addError(errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateInteger(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateInteger('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Integer.parseInt(value.trim());
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateLong(String field, long min, long max, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateLong('%s', %d, %d, '%s', '%s');", field, min, max, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			long temp = Long.parseLong(value.trim());
			if (temp < min || temp > max)
				addError(errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateLong(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateLong('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Long.parseLong(value.trim());
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateDouble(String field, double min, double max, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateDouble('%s', %d, %d, '%s', '%s');", field, min, max, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			double temp = Double.parseDouble(value.trim());
			if (temp < min || temp > max)
				addError(errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateDouble(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateDouble('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Double.parseDouble(value.trim());
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateDate(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateDate('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			new SimpleDateFormat(getDatePattern()).parse(value.trim());
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateDate(String field, Date min, Date max, String errorKey, String errorMessage) {
		if (scriptMode) {
			SimpleDateFormat sdf = new SimpleDateFormat(getDatePattern());
			addScript(String.format("validateDate('%s', '%s', '%s', '%s', '%s');", field, sdf.format(min), sdf.format(max), errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
		if (value == null || "".equals(value.trim())) {
			addError(errorKey, errorMessage);
			return ;
		}
		try {
			Date temp = new SimpleDateFormat(getDatePattern()).parse(value.trim());
			if (temp.before(min) || temp.after(max))
				addError(errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateDate(String field, String min, String max, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateDate('%s', '%s', '%s', '%s', '%s');", field, min, max, errorKey, errorMessage));
			return;
		}
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(getDatePattern());
			validateDate(field, sdf.parse(min.trim()), sdf.parse(max.trim()), errorKey, errorMessage);
		} catch (Exception e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateEqualField(String field_1, String field_2, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateEqualField('%s', '%s', '%s', '%s');", field_1, field_2, errorKey, errorMessage));
			return;
		}
		String value_1 = request.getParameter(field_1);
		String value_2 = request.getParameter(field_2);
		if (value_1 == null || value_2 == null || (! value_1.equals(value_2)))
			addError(errorKey, errorMessage);
	}
	
	protected void validateEqualString(String s1, String s2, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateEqualString('%s', '%s', '%s', '%s');", s1, s2, errorKey, errorMessage));
			return;
		}
		if (s1 == null || s2 == null || (! s1.equals(s2)))
			addError(errorKey, errorMessage);
	}
	
	protected void validateEqualInteger(Integer i1, Integer i2, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateEqualInteger(%d, %d, '%s', '%s');", i1, i2, errorKey, errorMessage));
			return;
		}
		if (i1 == null || i2 == null || (i1.intValue() != i2.intValue()))
			addError(errorKey, errorMessage);
	}
	
	protected void validateEmail(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateEmail('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		validateRegex(field, emailAddressPattern, false, errorKey, errorMessage);
	}
	
	protected void validateMobile(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateMobile('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		validateRegex(field, mobileNumberPattern, false, errorKey, errorMessage);
	}
	
	protected void validateUrl(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateUrl('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
        if (value == null) {
        	addError(errorKey, errorMessage);
        	return ;
        }
        try {
			value = value.trim();
			if (value.startsWith("https://"))
				value = "http://" + value.substring(8);
			new URL(value);
		} catch (MalformedURLException e) {
			addError(errorKey, errorMessage);
		}
	}
	
	protected void validateRegex(String field, String regExpression, boolean isCaseSensitive, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateRegex('%s', '%s', %b, '%s', '%s');", field, regExpression, isCaseSensitive, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
        if (value == null) {
        	addError(errorKey, errorMessage);
        	return ;
        }
        Pattern pattern = isCaseSensitive ? Pattern.compile(regExpression) : Pattern.compile(regExpression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        	addError(errorKey, errorMessage);
	}
	
	protected void validateRegex(String field, String regExpression, String errorKey, String errorMessage) {
		validateRegex(field, regExpression, true, errorKey, errorMessage);
	}
	
	protected void validateString(String field, int minLen, int maxLen, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateString('%s', %d, %d, '%s', '%s');", field, minLen, maxLen, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
        if (value == null) {
        	addError(errorKey, errorMessage);
        	return ;
        }
        if (value.length() < minLen || value.length() > maxLen)
			addError(errorKey, errorMessage);
	}
	
	protected void validateBoolean(String field, String errorKey, String errorMessage) {
		if (scriptMode) {
			addScript(String.format("validateBoolean('%s', '%s', '%s');", field, errorKey, errorMessage));
			return;
		}
		String value = request.getParameter(field);
        if (value == null) {
        	addError(errorKey, errorMessage);
        	return ;
        }
        value = value.trim().toLowerCase();
		if ("1".equals(value) || "true".equals(value))
			return ;
		else if ("0".equals(value) || "false".equals(value))
			return ;
		addError(errorKey, errorMessage);
	}
	
	/*protected void validateToken(String tokenName, String errorKey, String errorMessage) {
		
	}
	
	protected void validateToken(String errorKey, String errorMessage) {
		
	}*/
}
