package com.spring.boot.commoncore.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/14 10:02
 */
@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

	@Schema(description = "响应码", example = "0")
	private int code;

	@Schema(description = "响应信息", example = "操作成功")
	private String msg;

	@Schema(description = "响应数据")
	private T data;

	//构造器 ============================

	private Result() {}

	private Result(int code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	//成功返回 ============================

	public static Result<Void> success() {
		return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
	}

	public static Result<Void> success(String msg) {
		String finalMsg = (msg == null || msg.isBlank()) ? ResultCode.SUCCESS.getMsg() : msg;
		return new Result<>(ResultCode.SUCCESS.getCode(), finalMsg, null);
	}

	public static <T> Result<T> success(T data) {
		return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
	}

	public static <T> Result<T> success(T data, String customMsg) {
		String finalMsg = (customMsg == null || customMsg.isBlank()) ? ResultCode.SUCCESS.getMsg() : customMsg;
		return new Result<>(ResultCode.SUCCESS.getCode(), finalMsg, data);
	}

	public static <T> Result<T> success(ResultCode code, T data) {
		return new Result<>(code.getCode(), code.getMsg(), data);
	}

	//失败返回 ============================

	public static <T> Result<T> fail() {
		return new Result<>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMsg(), null);
	}

	public static <T> Result<T> fail(String msg) {
		return new Result<>(ResultCode.FAILED.getCode(), msg, null);
	}

	public static <T> Result<T> fail(ResultCode code) {
		return new Result<>(code.getCode(), code.getMsg(), null);
	}

	public static <T> Result<T> fail(ResultCode code, String customMsg) {
		String finalMsg = (customMsg == null || customMsg.isBlank()) ? code.getMsg() : customMsg;
		return new Result<>(code.getCode(), finalMsg, null);
	}

	public static <T> Result<T> fail(int code, String msg) {
		return new Result<>(code, msg, null);
	}


	@JsonIgnore
	public boolean isSuccess() {
		return this.code == ResultCode.SUCCESS.getCode();
	}

	@JsonIgnore
	public boolean isFail() {
		return !isSuccess();
	}
}
