package com.spring.boot.commoncore.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum ResultCode {

	//2xx 成功状态码 ============================
	OK(200, "操作成功"),

	//通用错误(临时使用） ============================
	FAILED(1000, "操作失败"),

	//4xx 客户端错误状态码 ============================
	BAD_REQUEST(400, "请求参数错误"),
	UNAUTHORIZED(401, "未登录或登录已过期"),
	FORBIDDEN(403, "无权限访问"),
	NOT_FOUND(404, "请求资源不存在"),
	METHOD_NOT_ALLOWED(405, "请求方法不支持"),

	//5xx 服务器错误状态码 ============================
	INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

	//参数校验 ============================
	PARAM_VALID_ERROR(5001, "参数校验失败"),

	//用户模块 ============================
	USER_ADD_FAILED(10001, "用户添加失败"),
	USER_NOT_EXIST(10002, "用户不存在"),
	DATA_NO_CHANGE(10003, "数据未改变"),

	//库存模块 ============================
	STOCK_ADD_FAILED(20001, "库存添加失败"),
	STOCK_DEDUCT_FAILED(20002, "库存扣除失败"),
	STOCK_NOT_EXIST(20003, "库存不存在"),
	STOCK_UPDATE_FAILED(20004, "库存更新失败"),
	STOCK_DELETE_FAILED(20005, "库存删除失败"),
	STOCK_BIZID_DUPLICATE(20006, "业务ID已存在")
	;

	private final int code;
	private final String msg;

	public static Optional<ResultCode> getByCode(int code) {

		return Arrays.stream(values())
				.filter(code1 -> code1.getCode() == code)
				.findFirst();
	}

}
