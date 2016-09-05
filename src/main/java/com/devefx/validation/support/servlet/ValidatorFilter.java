package com.devefx.validation.support.servlet;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.devefx.validation.Validator;
import com.devefx.validation.support.Cache;

/**
 * ValidatorInterceptor
 * Created by YYQ on 2016/9/5.
 */
public class ValidatorFilter implements Filter {
	
	public static final String FIELD_NAME = "servlet";
	
	private final Cache cache = new Cache();
	
	public Class<?> filterChainClass;
	public Field filterChainField;
	
	protected Servlet getServlet(FilterChain chain) throws IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = chain.getClass();
		if (filterChainClass == null) {
		 	try {
		 		filterChainField = clazz.getDeclaredField(FIELD_NAME);
		 		filterChainField.setAccessible(true);
			} catch (NoSuchFieldException e) {
				return null;
			}
		 	filterChainClass = clazz;
		}
		if (clazz == filterChainClass) {
			return (Servlet) filterChainField.get(chain);
		}
		return null;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		try {
			Servlet servlet =  getServlet(chain);
			if (servlet != null) {
				Validator validator = cache.get(servlet.getClass());
				if (validator != null && !validator.process((HttpServletRequest) request,
						(HttpServletResponse) response)) {
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		chain.doFilter(request, response);
	}
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}
	
	@Override
	public void destroy() {

	}
}
