package com.devefx.validation.web.config;

import com.devefx.validation.Validator;
import com.devefx.validation.annotation.Mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Routes
 * Created by YYQ on 2016/5/30.
 */
public class Routes {

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
            Validator validator = (Validator) validatorClass.newInstance();
            validator.setup();
            map.put(visitPath, validator);
        } catch (Exception e) {
            throw new RuntimeException("The validatorClass can not initialized");
        }
        return this;
    }

    public Routes add(Class<? extends Validator> validatorClass) {
        if (!validatorClass.isAnnotationPresent(Mapping.class))
            throw new IllegalArgumentException("The validatorClass not have @Mapping : " + validatorClass);
        Mapping mapping = validatorClass.getAnnotation(Mapping.class);
        return add(mapping.value(), validatorClass);
    }

    public Validator get(String path) {
        return map.get(path);
    }
}
