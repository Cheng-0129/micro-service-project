package com.spring.boot.commoncore.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

	// ==================== 成功 ====================
	SUCCESS(0, "操作成功"),

	// ==================== 通用错误 1000-1999 ====================
	FAILED(1000, "操作失败"),
	PARAM_VALID_ERROR(1001, "参数校验失败"),
	FEIGN_ERROR(1002, "远程服务调用失败"),

	// ==================== 用户模块 10000-19999 ====================
	USER_ADD_FAILED(10001, "用户添加失败"),
	USER_NOT_EXIST(10002, "用户不存在"),
	DATA_NO_CHANGE(10003, "数据未改变"),
	USER_SERVICE_DEGRADE(10004, "用户服务繁忙，请稍后重试"),
	USER_SERVICE_RATE_LIMIT(10005, "请求过于频繁，请稍后重试"),

	// ==================== 库存模块 20000-29999 ====================
	STOCK_ADD_FAILED(20001, "库存添加失败"),
	STOCK_NOT_EXIST(20002, "库存不存在"),
	STOCK_UPDATE_FAILED(20003, "库存更新失败"),
	STOCK_INSUFFICIENT(20004, "库存不足"),
	STOCK_SERVICE_DEGRADE(20005, "库存服务繁忙，请稍后重试"),
	STOCK_SERVICE_RATE_LIMIT(20006, "请求过于频繁，请稍后重试"),

	// ==================== 订单模块 30000-39999 ====================
	ORDER_ADD_FAILED(30001, "订单添加失败"),
	ORDER_NOT_EXIST(30002, "订单不存在"),
	ORDER_WAS_CANCELED(30003, "订单已取消"),
	ORDER_STATUS_ERROR(30004, "订单状态异常"),
	ORDER_ROLLBACK_FAILED(30005, "订单回滚失败"),
	ORDER_SERVICE_DEGRADE(30006, "订单服务繁忙，请稍后重试"),
	ORDER_SERVICE_RATE_LIMIT(30007, "请求过于频繁，请稍后重试"),

	// ==================== 网关模块 40000-49999 ====================
	GATEWAY_SERVICE_UNAVAILABLE(40001, "服务暂不可用"),
	GATEWAY_TIMEOUT(40002, "请求超时"),
	GATEWAY_RATE_LIMIT(40003, "请求过于频繁，请稍后重试"),
	GATEWAY_AUTH_FAILED(40004, "鉴权失败"),
	GATEWAY_NOT_FOUND(40005, "请求路径不存在"),

	// 鉴权细分
	GATEWAY_TOKEN_MISSING(40006, "缺少访问令牌"),
	GATEWAY_TOKEN_EXPIRED(40007, "登录已过期，请重新登录"),
	GATEWAY_TOKEN_INVALID(40008, "无效的访问令牌"),
	GATEWAY_FORBIDDEN(40009, "无访问权限")
	;

	private final int code;
	private final String msg;

}
