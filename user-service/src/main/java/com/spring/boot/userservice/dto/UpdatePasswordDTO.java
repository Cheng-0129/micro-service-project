package com.spring.boot.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/18 09:32
 */
@Data
@Schema(description = "更新密码DTO")
public class UpdatePasswordDTO {

	@Schema(description = "旧密码",
			example = "11111111",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "密码不能为空")
	@Size(min = 6, max = 16, message = "密码的长度为6-16")
	private String oldPassword;

	@Schema(description = "新密码",
			example = "22222222",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "密码不能为空")
	@Size(min = 6, max = 16, message = "密码的长度为6-16")
	private String newPassword;

	@Override
	public String toString() {
		return "UpdatePasswordDTO{" +
				"oldPassword='" + maskPassword(oldPassword) + '\'' +
				", newPassword='" + maskPassword(newPassword) + '\'' +
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
