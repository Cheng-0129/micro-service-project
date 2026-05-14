package com.spring.boot.orderservice.service;

import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.dto.OrderQueryDTO;
import com.spring.boot.orderservice.vo.OrderAddBackVO;
import com.spring.boot.orderservice.vo.OrderDetailVO;
import com.spring.boot.orderservice.vo.OrderCreateVO;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 14:39
 */
public interface OrderService {

	OrderCreateVO createOrder(OrderCreateDTO dto, String source);
	OrderDetailVO getByOrderNo(Long orderNo);
	PageVO<OrderDetailVO> getOrderPage(OrderQueryDTO query);
	OrderAddBackVO cancelOrder(Long orderNo);
	void deleteOrder(Long orderNo);
}
