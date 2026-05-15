package com.spring.boot.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/14 08:39
 */
@Data
@Schema(description = "用户注册/创建DTO")
public class UserCreateDTO {

	@Schema(description = "用户名",
			example = "张三",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "用户名不能为空")
	@Size(min = 2, max = 10, message = "用户名的长度为2-10")
	private String name;

	@Schema(description = "密码",
			example = "11111111",
			requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "密码不能为空")
	@Size(min = 6, max = 16, message = "密码的长度为6-16")
	private String password;

	@Schema(description = "年龄",
			example = "20",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED)
	@Min(value = 0, message = "年龄不能小于0")
	@Max(value = 150, message = "年龄必须小于150")
	private Integer age;

	@Schema(description = "邮箱",
			example = "zhangsan@test.com",
			requiredMode = Schema.RequiredMode.AUTO)
	@NotBlank(message = "邮箱不能为空")
	@Email(message = "邮箱格式不正确")
	private String email;

	@Override
	public String toString() {
		return "UserCreateDTO{" +
				"name='" + name + '\'' +
				", password='" + maskPassword(password) + '\'' +
				", age=" + age +
				", email='" + maskEmail(email) + '\'' +
				'}';
	}

	/**
	 * 密码脱敏：不管多长都显示 ******
	 */
	private String maskPassword(String password) {
		if (password == null) return null;
		return "******";
	}

	/**
	 * 邮箱脱敏：只显示前2位和@后面的部分
	 * 例：zhangsan@test.com → zh***@test.com
	 */
	private String maskEmail(String email) {
		if (email == null || !email.contains("@")) return email;
		String prefix = email.substring(0, email.indexOf("@"));
		String suffix = email.substring(email.indexOf("@"));
		if (prefix.length() <= 2) {
			return prefix + "***" + suffix;
		}
		return prefix.substring(0, 2) + "***" + suffix;
	}
}
