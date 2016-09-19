package com.devefx.validation.support;

import java.lang.reflect.AnnotatedElement;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.devefx.validation.Cache;
import com.devefx.validation.Validator;

/**
 * Interceptor
 * Created by YYQ on 2016/9/19.
 */
public class Interceptor {
	
	static Cache validCahce = new CacheImpl();
	
	public static void setCache(Cache cache) {
		validCahce = cache;
	}
	
	public static boolean valid(AnnotatedElement annotatedElement, HttpServletRequest request, HttpServletResponse response) {
		List<Validator> validSet = validCahce.get(annotatedElement);
		if (validSet != null) {
			Iterator<Validator> it = validSet.iterator();
			while (it.hasNext()) {
				Validator valid = it.next();
				valid.reset();
				if (!valid.process(request, response)) {
					return false;
				}
			}
		}
		return true;
	}
}
