package com.spring.boot.userservice.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.userservice.dto.feign.OrderCreateFeignDTO;
import com.spring.boot.userservice.vo.feign.OrderFeignVO;
import lombok.extern.slf4j.Slf4j;

import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 * UserController 的 Sentinel 降级处理类
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/13 15:47
 */
@Slf4j
public final class UserBlockHandler {

	/**
	 * 限流/降级触发时的处理方法
	 * 方法签名必须和原方法一致，最后加一个 BlockException 参数
	 */
	public static Result<OrderFeignVO> handleBlock(OrderCreateFeignDTO orderCreateFeignDTO, BlockException ex) {
		log.warn("【用户服务】下单降级/限流触发，用户ID：{}，商品ID：{}，异常类型：{}",
				orderCreateFeignDTO.getUserId(), orderCreateFeignDTO.getProductId(),
				ex.getClass().getSimpleName());

		if (ex instanceof DegradeException) {
			return Result.fail(USER_SERVICE_DEGRADE);
		}
		// FlowException 或其他
		return Result.fail(USER_SERVICE_RATE_LIMIT);
	}

	/**
	 * 业务异常兜底方法
	 * 方法签名必须和原方法一致，最后加一个 Throwable 参数
	 */
	public static Result<OrderFeignVO> handleFallback(OrderCreateFeignDTO orderCreateFeignDTO, Throwable t) {
		log.error("【用户服务】下单业务异常", t);
		return Result.fail(FAILED);
	}
}
