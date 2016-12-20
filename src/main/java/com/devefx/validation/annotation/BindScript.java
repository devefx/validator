package com.devefx.validation.annotation;

import java.lang.annotation.*;

/**
 * BindScript
 * Created by YYQ on 2016/5/26.
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BindScript {
    /**
     * script file path
     */
    public String value();
}
