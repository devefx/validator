package com.devefx.validation.support;

import com.devefx.validation.Validator;
import com.devefx.validation.annotation.Valid;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;

/**
 * Cache
 * Created by YYQ on 2016/5/27.
 */
public class Cache {

    protected Map<Method, SoftReference<Validator>> methodCache = new HashMap<Method, SoftReference<Validator>>();

    protected Map<Class<? extends Servlet>, SoftReference<Validator>> classCache =
    		new HashMap<Class<? extends Servlet>, SoftReference<Validator>>();
    
    public Validator get(Method method) throws Exception {
        Validator validator;
        SoftReference<Validator> reference = methodCache.get(method);
        if (reference == null || (validator = reference.get()) == null) {
            if (method.isAnnotationPresent(Valid.class)) {
                Valid valid = method.getAnnotation(Valid.class);
                validator = valid.value().newInstance();
                validator.setup();
                methodCache.put(method, new SoftReference<Validator>(validator));
                return validator;
            }
            return null;
        }
        validator.reset();
        return validator;
    }
    
    public Validator get(Class<? extends Servlet> servletClass) throws Exception {
    	Validator validator;
    	SoftReference<Validator> reference = classCache.get(servletClass);
    	if (reference == null || (validator = reference.get()) == null) {
			if (servletClass.isAnnotationPresent(Valid.class)) {
				Valid valid = servletClass.getAnnotation(Valid.class);
				validator = valid.value().newInstance();
				validator.setup();
				classCache.put(servletClass, new SoftReference<Validator>(validator));
				return validator;
			}
    		return null;
		}
    	validator.reset();
    	return validator;
    }
}
