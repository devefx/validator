package org.devefx.mirror.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Table
 * @author： youqian.yue
 * @date： 2015-10-29 下午2:49:04
 */
@Inherited
@Target(value={ElementType.TYPE,  ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	public String value() default "";
	public String key() default "id";
}