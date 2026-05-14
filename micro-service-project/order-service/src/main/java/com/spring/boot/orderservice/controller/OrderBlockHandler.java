package com.spring.boot.orderservice.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.vo.OrderAddBackVO;
import com.spring.boot.orderservice.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;

import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/13 16:35
 */
@Slf4j
public final class OrderBlockHandler {

	// 创建订单
	public static Result<OrderCreateVO> handleCreateOrderBlock(OrderCreateDTO order, BlockException ex) {
		log.warn("【订单服务】创建订单降级/限流触发，用户ID：{}，产品ID：{}，商品数量：{}，异常类型：{}",
				order.getUserId(), order.getProductId(), order.getNum(), ex.getClass().getSimpleName());
		if (ex instanceof DegradeException) {
			return Result.fail(ORDER_SERVICE_DEGRADE);
		}
		return Result.fail(ORDER_SERVICE_RATE_LIMIT);
	}

	public static Result<OrderCreateVO> handleCreateOrderFallback(OrderCreateDTO order, Throwable t) {
		log.error("【订单服务】创建订单业务异常", t);
		return Result.fail(FAILED);
	}

	// 取消订单
	public static Result<OrderAddBackVO> handleCancelOrderBlock(Long orderNo, BlockException ex) {
		log.warn("【订单服务】取消订单降级/限流触发，订单号：{}，异常类型：{}",
				orderNo, ex.getClass().getSimpleName());
		if (ex instanceof DegradeException) {
			return Result.fail(ORDER_SERVICE_DEGRADE);
		}
		return Result.fail(ORDER_SERVICE_RATE_LIMIT);
	}

	public static Result<OrderAddBackVO> handleCancelOrderFallback(Long orderNo, Throwable t) {
		log.error("【订单服务】取消订单业务异常", t);
		return Result.fail(FAILED);
	}
}