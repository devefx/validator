package com.devefx.validation.support.spring;

import com.devefx.validation.Validator;
import com.devefx.validation.annotation.Valid;
import com.devefx.validation.support.Cache;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
