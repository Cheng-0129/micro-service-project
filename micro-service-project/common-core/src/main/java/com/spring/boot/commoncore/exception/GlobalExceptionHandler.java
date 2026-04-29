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



	//业务异常 ============================

	@ExceptionHandler(BusinessException.class)
	public Result<?> handleBusinessException(BusinessException e) {

		if(e.getCode() == ResultCode.USER_ADD_FAILED.getCode()) {
			log.error("业务异常【用户添加失败】，msg={}", e.getMessage(), e);
		}else {
			log.error("业务异常，msg={}", e.getMessage(), e);
		}

		return Result.fail(e.getCode(), e.getMessage());
	}


	//参数异常 ============================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

		List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

		String errorMsg = fieldErrors.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("；"));

		log.error("参数异常【实体参数校验失败】，errorMsg={}, msg={}", errorMsg, e.getMessage(), e);
		return Result.fail(ResultCode.PARAM_VALID_ERROR, errorMsg);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public Result<?> handleConstraintViolation(ConstraintViolationException e) {

		String errorMsg = e.getConstraintViolations().stream()
				.map(ConstraintViolation::getMessage)
				.collect(Collectors.joining("；"));

		log.error("参数异常【请求参数校验失败】，errorMsg={}, msg={}", errorMsg, e.getMessage(), e);
		return Result.fail(ResultCode.PARAM_VALID_ERROR, errorMsg);
	}

	//客户端异常 ============================

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public Result<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
		log.error("系统异常【400 请求参数错误】", e);
		return Result.fail(ResultCode.BAD_REQUEST);
	}

	// 静态资源找不到 - 返回 404，让前端正常识别
	@ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e) {
		// 不做任何处理，让 Spring 返回 404 状态码
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public Result<?> handleNoHandlerFound(NoHandlerFoundException e) {
		log.error("系统异常【404 接口不存在】", e);
		return Result.fail(ResultCode.NOT_FOUND, "请求接口不存在");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Result<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.error("系统异常【405 请求方法不支持】", e);
		return Result.fail(ResultCode.METHOD_NOT_ALLOWED);
	}

	//系统异常 ============================

	@ExceptionHandler(Exception.class)
	public Result<?> handleException(Exception e) {
		log.error("系统未知异常", e);
		return Result.fail(ResultCode.INTERNAL_SERVER_ERROR, "系统繁忙，请稍后重试");
	}
}