package com.spring.boot.orderservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.spring.boot.commoncore.constant.FeignHeaders;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.util.ExceptionUtil;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.dto.OrderQueryDTO;
import com.spring.boot.orderservice.service.OrderService;
import com.spring.boot.orderservice.vo.OrderAddBackVO;
import com.spring.boot.orderservice.vo.OrderCreateVO;
import com.spring.boot.orderservice.vo.OrderDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 15:01
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理模块", description = "订单接口")
public class OrderController {

	@Resource
	private OrderService orderService;

	@Operation(summary = "创建订单",
			description = "传入用户ID、商品ID、数量，生成订单号并调用库存模块扣减库存。" +
					"生成订单号失败/插入失败/更新失败返回 30001，远程调用失败返回 1002。触发熔断/限流返回 30006/30007")
	@PostMapping("/create")
	@SentinelResource(
			value = "createOrder",
			blockHandler = "handleCreateOrderBlock",
			blockHandlerClass = OrderBlockHandler.class,
			fallback = "handleCreateOrderFallback",
			fallbackClass = OrderBlockHandler.class)
	public Result<OrderCreateVO> createOrder(@RequestBody @Valid OrderCreateDTO order,
	                                         @RequestHeader(value = FeignHeaders.SOURCE, required = false) String source) {

		try {
			log.info("【订单模块】创建订单，请求参数：{}", order);
			OrderCreateVO vo = orderService.createOrder(order, source);
			log.info("【订单模块】订单创建成功，订单号：{}", vo.getOrderNo());
			return Result.success(vo, "创建订单成功");
		} catch (RuntimeException e) {
			Throwable cause = ExceptionUtil.unwind(e);
			if (cause instanceof BusinessException bizEx) {
				log.warn("【订单模块】业务异常：{}", bizEx.getMessage());
				return Result.fail(bizEx.getCode(), bizEx.getMessage());
			}
			throw e;
		}
	}

	@Operation(summary = "查询订单",
			description = "根据订单号查询订单详细信息，订单不存在返回 30002")
	@GetMapping("/{orderNo}")
	public Result<OrderDetailVO> getByOrderNo(@PathVariable("orderNo")
	                                          @Parameter(
			                                          description = "订单号",
			                                          example = "1")
	                                          @Min(value = 1L, message = "订单号必须大于0")
	                                          Long orderNo) {
		log.info("【订单模块】根据订单号查询订单，请求参数：{}", orderNo);
		OrderDetailVO vo = orderService.getByOrderNo(orderNo);
		log.info("【订单模块】根据订单号查询订单成功，订单号：{}", vo.getOrderNo());
		return Result.success(vo, "查询订单成功");
	}


	@Operation(summary = "分页查询订单",
			description = "支持按条件分页查询订单列表，无匹配数据返回空列表")
	@GetMapping("/page")
	public Result<PageVO<OrderDetailVO>> getOrderPage(@Valid OrderQueryDTO query) {

		log.info("【订单模块】收到分页查询请求，参数：{}", query);
		PageVO<OrderDetailVO> pageVO = orderService.getOrderPage(query);
		log.info("【订单模块】分页查询响应，返回{}条", pageVO.getRecords().size());
		return Result.success(pageVO, "查询成功");
	}

	@Operation(summary = "取消订单",
			description = "根据订单号取消订单。PENDING 状态直接取消并返回 null，CREATED 状态取消并回滚库存后返回库存信息。" +
					"PAID 状态不可取消需走退款，CANCELLED 状态不可重复取消。" +
					"订单不存在返回 30002，已取消返回 30003，订单状态异常返回 30004，库存回滚失败返回 30005。触发熔断/限流返回 30006/30007")
	@PutMapping("/cancel/{orderNo}")
	@SentinelResource(
			value = "cancelOrder",
			blockHandler = "handleCancelOrderBlock",
			blockHandlerClass = OrderBlockHandler.class,
			fallback = "handleCancelOrderFallback",
			fallbackClass = OrderBlockHandler.class)
	public Result<OrderAddBackVO> cancelOrder(@PathVariable("orderNo")
	                                          @Parameter(
			                                          description = "订单号",
			                                          example = "1")
	                                          @Min(value = 1L, message = "订单号必须大于0")
	                                          Long orderNo) {

		log.info("【订单模块】取消订单，请求参数：{}", orderNo);
		OrderAddBackVO vo = orderService.cancelOrder(orderNo);
		log.info("【订单模块】订单取消成功，订单号：{}", orderNo);
		return Result.success(vo, "取消订单成功");
	}

	@Operation(summary = "删除订单",
			description = "根据订单号删除订单记录，仅可删除已取消状态的订单。订单不存在返回 30002，状态不允许删除返回 30004")
	@DeleteMapping("{orderNo}")
	public Result<Void> deleteOrder(@PathVariable("orderNo")
	                                @Parameter(
			                                description = "订单号",
			                                example = "1")
	                                @Min(value = 1, message = "订单号必须大于0")
	                                Long orderNo) {

		log.info("【订单模块】删除订单信息，请求参数：orderNo={}", orderNo);

		orderService.deleteOrder(orderNo);

		log.info("【订单模块】删除订单信息成功，orderNo={}", orderNo);

		return Result.success("订单删除成功");
	}

}
