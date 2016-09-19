package com.devefx.validation.annotation;

import com.devefx.validation.Validator;

import java.lang.annotation.*;

/**
 * Valid
 * Created by YYQ on 2016/5/27.
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Valid {
    Class<? extends Validator>[] value();
}
