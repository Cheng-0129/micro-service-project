package com.spring.boot.orderservice.controller;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.service.OrderService;
import com.spring.boot.orderservice.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@Operation(
			summary = "创建订单",
			description = "创建订单接口"
	)
	@PostMapping("/create")
	public Result<OrderVO> createOrder(@RequestBody @Valid OrderCreateDTO order) {

		log.info("【订单模块】创建订单，请求参数：{}", order);
		OrderVO vo = orderService.createOrder(order);
		log.info("【订单模块】订单创建成功，订单号：{}", vo.getOrderNo());

		return Result.success(vo, "创建订单成功");
	}
}
