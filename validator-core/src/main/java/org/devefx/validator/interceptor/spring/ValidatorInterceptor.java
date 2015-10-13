package org.devefx.validator.interceptor.spring;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devefx.validator.annotation.RequestValidator;
import org.devefx.validator.core.Validator;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ValidatorInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		if (handler instanceof HandlerMethod) {
			Method method = ((HandlerMethod)handler).getMethod();
			if (method.isAnnotationPresent(RequestValidator.class)) {
				RequestValidator requstValidator = method.getAnnotation(RequestValidator.class);
				Validator validator = requstValidator.value().newInstance();
				if (!validator.process(request, response)) {
					return false;
				}
			}
		}
		
		return super.preHandle(request, response, handler);
	}
}
