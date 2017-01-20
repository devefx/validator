package com.devefx.validation.web.config;

import com.devefx.validation.Cache;
import com.devefx.validation.Validator;
import com.devefx.validation.annotation.Mapping;
import com.devefx.validation.support.Interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routes
 * Created by YYQ on 2016/5/30.
 */
public class Routes {
    
    private Logger log = LoggerFactory.getLogger(Routes.class);

    private AtomicInteger atomicLong = new AtomicInteger(0);
    private final Map<String, Validator> map = new HashMap<String, Validator>();
    private String basePath = null;

    public void setBasePath(String basePath) {
        if (basePath == null)
            throw new IllegalArgumentException("The basePath can not be null");
        basePath = basePath.trim();
        if (basePath.isEmpty())
            throw new IllegalArgumentException("The basePath can not be blank");
        if (!basePath.startsWith("/"))
            basePath = "/" + basePath;
        if (basePath.endsWith("/"))
            basePath = basePath.substring(0, basePath.length() - 1);
        this.basePath = basePath;
    }

    public Routes add(String visitPath, Class<? extends Validator> validatorClass) {
        if (visitPath == null)
            throw new IllegalArgumentException("The visitPath can not be null");
        visitPath = visitPath.trim();
        if (visitPath.isEmpty())
            throw new IllegalArgumentException("The visitPath can not be blank");
        if (validatorClass == null)
            throw new IllegalArgumentException("The validatorClass can not be null");
        if (!visitPath.startsWith("/"))
            visitPath = "/" + visitPath;
        if (basePath != null)
            visitPath = basePath + visitPath;
        if (map.containsKey(visitPath))
            throw new IllegalArgumentException("The visitPath already exists: " + visitPath);
        try {
            final Cache cache = Interceptor.getCache();
            Validator validator = cache.acquireInstance(validatorClass);
            validator.setGlobalId(atomicLong.incrementAndGet());
            validator.setup();
            map.put(visitPath, validator);
            if (log.isInfoEnabled()) {
                log.info("Resgister validator [" + validator + "] path [" + visitPath + "]");
            }
        } catch (Exception e) {
            throw new RuntimeException("The validatorClass can not initialized: " + validatorClass);
        }
        return this;
    }
    
    public Validator get(String path) {
        return map.get(path);
    }
    
    public Validator get(int globalId) {
        for (Validator validator : map.values()) {
            if (globalId == validator.getGlobalId()) {
                return validator;
            }
        }
        return null;
    }

    public Routes add(Class<? extends Validator> validatorClass) {
        if (!validatorClass.isAnnotationPresent(Mapping.class))
            throw new IllegalArgumentException("The validatorClass not have @Mapping : " + validatorClass);
        Mapping mapping = validatorClass.getAnnotation(Mapping.class);
        return add(mapping.value(), validatorClass);
    }
}
