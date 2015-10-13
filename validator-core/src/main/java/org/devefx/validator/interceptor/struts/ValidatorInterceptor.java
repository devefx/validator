package org.devefx.validator.interceptor.struts;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.devefx.validator.annotation.RequestValidator;
import org.devefx.validator.core.Validator;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionProxy;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

public class ValidatorInterceptor extends MethodFilterInterceptor {
	
	private static final long serialVersionUID = -2053627113817188237L;

	private static final String HTTP_REQUEST = "com.opensymphony.xwork2.dispatcher.HttpServletRequest";
	
	private static final String HTTP_RESPONSE = "com.opensymphony.xwork2.dispatcher.HttpServletResponse";
	
	@Override
	protected String doIntercept(ActionInvocation invocation) throws Exception {
		ActionProxy actionProxy = invocation.getProxy();
		if (actionProxy.getAction() instanceof ActionSupport) {
			Class<?> actionClass = actionProxy.getAction().getClass();
			Method method = actionClass.getMethod(invocation.getProxy().getMethod());
			if (method.isAnnotationPresent(RequestValidator.class)) {
				RequestValidator requstValidator = method.getAnnotation(RequestValidator.class);
				Validator validator = requstValidator.value().newInstance();
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
