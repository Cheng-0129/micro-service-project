package com.spring.boot.orderservice.service;

import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.vo.OrderVO;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 14:39
 */
public interface OrderService {

	OrderVO createOrder(OrderCreateDTO dto);
}
