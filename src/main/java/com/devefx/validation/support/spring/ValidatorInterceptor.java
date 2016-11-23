package com.devefx.validation.support.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.devefx.validation.support.Interceptor;

public class ValidatorInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod handlerMethod = (HandlerMethod) handler;
			if (!Interceptor.valid(handlerMethod.getBeanType(), request, response)) {
				return false;
			}
			if (!Interceptor.valid(handlerMethod.getMethod(), request, response)) {
				return false;
			}
        } else {
            if (!Interceptor.valid(handler.getClass(), request, response)) {
                return false;
            }
        }
		return super.preHandle(request, response, handler);
	}
}
