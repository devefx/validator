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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devefx.validation.support.Interceptor;

public class ValidatorFilter implements Filter {
	
	public static final Logger log = LoggerFactory.getLogger(ValidatorFilter.class);
	public static final String FIELD_NAME = "servlet";
	
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
				if (!Interceptor.valid(servlet.getClass(), (HttpServletRequest) request, (HttpServletResponse) response)) {
					return;
				}
			}
		} catch (Exception e) {
			log.error("An error occurred：", e);
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
