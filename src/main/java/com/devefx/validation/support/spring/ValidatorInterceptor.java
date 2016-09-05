package com.devefx.validation.support.spring;

import com.devefx.validation.Validator;
import com.devefx.validation.support.Cache;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ValidatorInterceptor
 * Created by YYQ on 2016/5/27.
 */
public class ValidatorInterceptor extends HandlerInterceptorAdapter {

    private final Cache cache = new Cache();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            Validator validator = cache.get(((HandlerMethod) handler).getMethod());
            if (validator != null) {
                if (!validator.process(request, response)) {
                    return false;
                }
            }
        }
        return super.preHandle(request, response, handler);
    }
}
