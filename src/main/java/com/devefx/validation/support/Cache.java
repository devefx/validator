package com.devefx.validation.support;

import com.devefx.validation.Validator;
import com.devefx.validation.annotation.Valid;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache
 * Created by YYQ on 2016/5/27.
 */
public class Cache {

    protected Map<Method, SoftReference<Validator>> cache = new HashMap<Method, SoftReference<Validator>>();

    public Validator get(Method method) throws Exception {
        Validator validator;
        SoftReference<Validator> reference = cache.get(method);
        if (reference == null || (validator = reference.get()) == null) {
            if (method.isAnnotationPresent(Valid.class)) {
                Valid valid = method.getAnnotation(Valid.class);
                validator = valid.value().newInstance();
                validator.setup();
                cache.put(method, new SoftReference<Validator>(validator));
                return validator;
            }
            return null;
        }
        validator.reset();
        return validator;
    }
}
