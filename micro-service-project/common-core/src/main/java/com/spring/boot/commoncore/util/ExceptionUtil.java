package com.spring.boot.commoncore.util;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/6 11:38
 */
public class ExceptionUtil {
	public static Throwable unwind(Throwable e) {
		Throwable cause = e;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause;
	}
}
