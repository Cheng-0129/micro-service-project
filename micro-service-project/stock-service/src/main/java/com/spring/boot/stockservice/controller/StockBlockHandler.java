package com.spring.boot.stockservice.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.stockservice.vo.StockDeductVO;
import lombok.extern.slf4j.Slf4j;

import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/13 16:24
 */
@Slf4j
public final class StockBlockHandler {

	/**
	 * 限流/降级触发时的处理方法
	 * 方法签名必须和原方法一致，最后加一个 BlockException 参数
	 */
	public static Result<StockDeductVO> handleBlock(Long productId, Integer num, BlockException ex) {
		log.warn("【库存服务】扣减库存降级/限流触发，产品ID：{}，购买数量：{}，异常类型：{}",
				productId, num, ex.getClass().getSimpleName());

		if (ex instanceof DegradeException) {
			return Result.fail(STOCK_SERVICE_DEGRADE);
		}
		// FlowException 或其他
		return Result.fail(STOCK_SERVICE_RATE_LIMIT);
	}

	/**
	 * 业务异常兜底方法
	 * 方法签名必须和原方法一致，最后加一个 Throwable 参数
	 */
	public static Result<StockDeductVO> handleFallback(Long productId, Integer num, Throwable t) {
		log.error("【库存服务】扣减库存业务异常", t);
		return Result.fail(FAILED);
	}
}
