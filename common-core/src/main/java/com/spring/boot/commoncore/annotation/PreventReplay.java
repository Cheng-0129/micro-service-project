package com.spring.boot.commoncore.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/19 11:31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventReplay {

	long timeout() default 60000; // 默认1分钟
}
