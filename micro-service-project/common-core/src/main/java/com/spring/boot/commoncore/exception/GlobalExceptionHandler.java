package com.spring.boot.commoncore.exception;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/14 15:23
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// ==================== 业务异常 ====================

	@ExceptionHandler(BusinessException.class)
	public Result<?> handleBusinessException(BusinessException e) {
		log.error("业务异常，code={}, msg={}", e.getCode(), e.getMessage(), e);
		return Result.fail(e.getCode(), e.getMessage());
	}

	// ==================== 参数校验异常 ====================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

		List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

		String errorMsg = fieldErrors.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("；"));

		log.error("参数校验失败，msg={}", errorMsg, e);
		return Result.fail(ResultCode.PARAM_VALID_ERROR, errorMsg);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public Result<?> handleConstraintViolation(ConstraintViolationException e) {

		String errorMsg = e.getConstraintViolations().stream()
				.map(ConstraintViolation::getMessage)
				.collect(Collectors.joining("；"));

		log.error("参数校验失败，msg={}", errorMsg, e);
		return Result.fail(ResultCode.PARAM_VALID_ERROR, errorMsg);
	}

	// ==================== Spring MVC 内置异常 → 映射到 HTTP 状态码 ====================

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)          // HTTP 400
	public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.error("请求参数类型不匹配", e);
		return Result.fail(ResultCode.PARAM_VALID_ERROR, "请求参数类型错误");
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)            // HTTP 404
	public Result<?> handleNoHandlerFound(NoHandlerFoundException e) {
		log.error("接口不存在", e);
		return Result.fail(ResultCode.FAILED, "请求接口不存在");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)   // HTTP 405
	public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.error("请求方法不支持", e);
		return Result.fail(ResultCode.FAILED, "请求方法不支持");
	}

	// 静态资源 404，只返回 HTTP 404，不返回 JSON 体
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleNoResourceFound(NoResourceFoundException e) {
		// 不做处理，让 Spring 自己返回 404
	}

	// ==================== 兜底异常 ====================

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500
	public Result<?> handleUnknownException(Exception e) {
		log.error("系统未知异常", e);
		return Result.fail(ResultCode.FAILED, "系统繁忙，请稍后重试");
	}
}