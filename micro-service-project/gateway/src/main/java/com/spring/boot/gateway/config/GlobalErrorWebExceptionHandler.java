package com.spring.boot.gateway.config;

import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.result.ResultCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/9 11:01
 */
@Slf4j
@Order(-1)
@Configuration
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
		Result<Void> result;

		if (ex instanceof BusinessException bizEx) {
			log.warn("【网关】业务异常：{}", bizEx.getMessage());
			result = Result.fail(bizEx.getCode(), bizEx.getMessage());
		} else {
			log.error("【网关】系统异常", ex);
			result = Result.fail(ResultCode.FAILED);
		}

		exchange.getResponse().setStatusCode(HttpStatus.OK);
		exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(result);
			DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
			return exchange.getResponse().writeWith(Mono.just(buffer));
		} catch (JsonProcessingException e) {
			log.error("【网关】响应序列化失败", e);
			return Mono.error(e);
		}
	}
}
