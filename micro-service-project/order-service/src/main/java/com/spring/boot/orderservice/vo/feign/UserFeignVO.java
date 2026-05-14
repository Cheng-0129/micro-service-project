package com.spring.boot.orderservice.vo.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/15 14:07
 */
@Data
@Schema(description = "用户信息响应VO（Feign）")
public class UserFeignVO {

	@Schema(description = "用户ID", example = "1")
	private Long userId;

	@Schema(description = "用户名", example = "张三")
	private String name;
}
