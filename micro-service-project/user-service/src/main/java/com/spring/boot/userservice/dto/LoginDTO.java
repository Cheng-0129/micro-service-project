package com.spring.boot.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/14 11:38
 */
@Data
@Schema(description = "用户登录DTO")
public class LoginDTO {

	@Schema(description = "用户名",
			example = "测试员",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "用户名不能为空")
	@Size(min = 2, max = 10, message = "用户名的长度为2-10")
	private String username;

	@Schema(description = "密码",
			example = "1234567890",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "密码不能为空")
	@Size(min = 6, max = 16, message = "密码的长度为6-16")
	private String password;

	@Override
	public String toString() {
		return "LoginDTO{" +
				"username='" + username + '\'' +
				", password='" + maskPassword(password) + '\'' +
				'}';
	}

	/**
	 * 密码脱敏：不管多长都显示 ******
	 */
	private String maskPassword(String password) {
		if (password == null) return null;
		return "******";
	}
}
