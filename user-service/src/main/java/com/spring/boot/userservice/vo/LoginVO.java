package com.spring.boot.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/14 11:40
 */
@Data
@Schema(description = "用户登录响应VO")
public class LoginVO {

	@Schema(description = "用户ID", example = "1")
	private Long userId;
	@Schema(description = "用户名", example = "张三")
	private String name;
	@Schema(description = "JWT访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
	private String accessToken;
	@Schema(description = "JWT刷新令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
	private String refreshToken;
}