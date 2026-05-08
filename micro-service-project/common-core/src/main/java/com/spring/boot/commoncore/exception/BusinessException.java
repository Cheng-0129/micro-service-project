package com.spring.boot.commoncore.exception;

import com.spring.boot.commoncore.result.ResultCode;
import lombok.Getter;

import java.io.Serial;


/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/14 15:44
 */
@Getter
public class BusinessException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final int code;

	//构造器 ============================

	public BusinessException() {
		this(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMsg(), null);
	}

	public BusinessException(String msg) {
		this(ResultCode.FAILED.getCode(), msg, null);
	}

	public BusinessException(ResultCode code) {
		this(code.getCode(), code.getMsg(), null);
	}

	public BusinessException(int code, String msg) {
		this(code, msg, null);
	}

	private BusinessException(int code, String msg, Throwable cause) {
		super(msg, cause);
		this.code = code;
	}

	//工厂方法 ============================

	public static BusinessException of(ResultCode code) {
		return new BusinessException(code.getCode(), code.getMsg(), null);
	}

	public static BusinessException of(ResultCode code, String customMsg) {
		return new BusinessException(code.getCode(), customMsg, null);
	}

	public static BusinessException of(int code, String customMsg) {
		return new BusinessException(code, customMsg, null);
	}

	public static BusinessException of(ResultCode code, Throwable cause) {
		return new BusinessException(code.getCode(), code.getMsg(), cause);
	}

	public static BusinessException of(ResultCode code, String customMsg, Throwable cause) {
		return new BusinessException(code.getCode(), customMsg, cause);
	}
}
