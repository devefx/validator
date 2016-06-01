package com.devefx.validation.support.struts2;

import com.devefx.validation.Validator;
import com.devefx.validation.support.Cache;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

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
public class ValidatorInterceptor extends MethodFilterInterceptor {

    private static final String HTTP_REQUEST = "com.opensymphony.xwork2.dispatcher.HttpServletRequest";
    private static final String HTTP_RESPONSE = "com.opensymphony.xwork2.dispatcher.HttpServletResponse";

    private final Cache cache = new Cache();

    @Override
    protected String doIntercept(ActionInvocation invocation) throws Exception {
        ActionProxy actionProxy = invocation.getProxy();
        if (actionProxy.getAction() instanceof ActionSupport) {
            Class<?> actionClass = actionProxy.getAction().getClass();
            Method method = actionClass.getMethod(invocation.getProxy().getMethod());
            Validator validator = cache.get(method);
            if (validator != null) {
                ActionContext context = invocation.getInvocationContext();
                HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);
                HttpServletResponse response = (HttpServletResponse) context.get(HTTP_RESPONSE);
                if (!validator.process(request, response)) {
                    return null;
                }
            }
        }
        return invocation.invoke();
    }
}
