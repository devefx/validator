package com.devefx.validation.annotation;

import java.lang.annotation.*;

/**
 * Mapping
 * Created by YYQ on 2016/5/31.
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
    String value();
}
