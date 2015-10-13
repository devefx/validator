package org.devefx.validator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.devefx.validator.core.Validator;

@Inherited
@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestValidator {
	public Class<? extends Validator> value();
}
