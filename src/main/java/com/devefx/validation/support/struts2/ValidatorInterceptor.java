package com.devefx.validation.support.struts2;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.devefx.validation.support.Interceptor;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

public class ValidatorInterceptor extends MethodFilterInterceptor {

	private static final long serialVersionUID = 1L;
	private static final String HTTP_REQUEST = "com.opensymphony.xwork2.dispatcher.HttpServletRequest";
    private static final String HTTP_RESPONSE = "com.opensymphony.xwork2.dispatcher.HttpServletResponse";
	
	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {
		ActionProxy proxy = invocation.getProxy();
		if (proxy.getAction() instanceof ActionSupport) {
			ActionContext context = invocation.getInvocationContext();
            HttpServletRequest request = (HttpServletRequest) context.get(HTTP_REQUEST);
            HttpServletResponse response = (HttpServletResponse) context.get(HTTP_RESPONSE);
            Class<?> actionClass = proxy.getAction().getClass();
			// valid
			if (!Interceptor.valid(actionClass, request, response)) {
				return null;
			}
			Method method = actionClass.getMethod(proxy.getMethod());
			if (!Interceptor.valid(method, request, response)) {
				return null;
			}
		}
        return invocation.invoke();
	}
}
