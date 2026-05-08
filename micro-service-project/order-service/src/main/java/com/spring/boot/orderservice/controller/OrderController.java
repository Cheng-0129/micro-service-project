package com.spring.boot.orderservice.controller;

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
			description = "创建订单接口，传入userId、productId、num来创建订单，并调用库存模块扣除对应库存，成功则返回订单信息和库存余量，" +
					"失败会触发订单和库存的回滚，" +
					"订单模块内的订单号生成失败、订单插入失败、订单状态更新失败则返回30001，" +
					"远程调用的订单模块出错（库存不足/库存不存在等情况），则返回1002。")
	@PostMapping("/create")
	public Result<OrderCreateVO> createOrder(@RequestBody @Valid OrderCreateDTO order) {

		try {
			log.info("【订单模块】创建订单，请求参数：{}", order);
			OrderCreateVO vo = orderService.createOrder(order);
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

	@Operation(summary = "根据订单号查询订单信息",
			description = "传入订单号，返回订单信息，若订单不存在则返回30002。")
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


	@Operation(summary = "分页查询订单信息",
			description = "传入分页参数和查询条件，返回分页后的库存列表，若库存不存在则返回空列表。")
	@GetMapping("/page")
	public Result<PageVO<OrderDetailVO>> getOrderPage(@Valid OrderQueryDTO query) {

		log.info("【订单模块】收到分页查询请求，参数：{}", query);
		PageVO<OrderDetailVO> pageVO = orderService.getOrderPage(query);
		log.info("【订单模块】分页查询响应，返回{}条", pageVO.getRecords().size());
		return Result.success(pageVO, "查询成功");
	}

	@Operation(summary = "取消订单",
			description = "传入订单号，取消订单，若取消未处理订单，则只返回成功取消提示，若取消已创建订单，则返回取消订单信息和库存数量，" +
					"若订单不存在则返回30002，若重复取消订单则返回30003，若无法取消订单则返回30004。")
	@PutMapping("/cancel/{orderNo}")
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


	@Operation(summary = "删除订单信息",
			description = "传入订单号，删除订单信息，只会删除已取消的订单，会报错阻止误删其他状态的订单号并返回30004，若订单不存在则返回30002。")
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
