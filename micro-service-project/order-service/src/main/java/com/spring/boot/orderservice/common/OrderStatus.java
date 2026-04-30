package com.spring.boot.orderservice.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 15:57
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
	PENDING(0, "待处理"),
	CREATED(1, "已创建"),
	PAID(2, "已支付"),
	CANCELLED(-1, "已取消");

	private final int code;
	private final String desc;
}
