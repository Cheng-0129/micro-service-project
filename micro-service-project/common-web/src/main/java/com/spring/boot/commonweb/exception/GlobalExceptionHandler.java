package com.spring.boot.commonweb.exception;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.result.ResultCode;
import com.spring.boot.commoncore.util.ExceptionUtil;
import feign.FeignException;
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

	@ExceptionHandler(com.spring.boot.commoncore.exception.BusinessException.class)
	@ResponseStatus(HttpStatus.OK)
	public Result<?> handleBusinessException(com.spring.boot.commoncore.exception.BusinessException e) {
		log.warn("业务异常，code={}, msg={}", e.getCode(), e.getMessage());
		return Result.fail(e.getCode(), e.getMessage());
	}

	// ==================== 参数校验异常 ====================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

		List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

		String errorMsg = fieldErrors.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("；"));

		log.error("参数校验失败，msg={}", errorMsg, e);
		return Result.fail(ResultCode.PARAM_VALID_ERROR, errorMsg);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
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

		Throwable cause = ExceptionUtil.unwind(e);

		if (cause instanceof com.spring.boot.commoncore.exception.BusinessException bizEx) {
			log.warn("业务异常，code={}, msg={}", bizEx.getCode(), bizEx.getMessage());
			return Result.fail(bizEx.getCode(), bizEx.getMessage());
		}

		Result<?> feignResult = handleFeignException(cause);
		if (feignResult != null) {
			return feignResult;
		}

		log.error("系统未知异常", e);
		return Result.fail(500, "系统内部错误");
	}

	private Result<?> handleFeignException(Throwable cause) {
		if (cause instanceof FeignException fe) {
			log.error("远程服务调用异常，url={}, status={}, body={}",
					fe.request().url(), fe.status(), fe.contentUTF8());
			return switch (fe.status()) {
				case 503 -> Result.fail(503, "服务正在维护，请稍后重试");
				case 500 -> Result.fail(500, "服务器繁忙，请稍后重试");
				case 404 -> Result.fail(404, "请求的资源不存在");
				default -> Result.fail(500, "服务器繁忙，请稍后重试");
			};
		}

		if (cause instanceof java.net.ConnectException ||
				cause instanceof java.net.SocketTimeoutException) {
			log.error("服务连接失败：{}", cause.getMessage());
			return Result.fail(503, "服务暂不可用，请稍后重试");
		}

		return null;
	}
}