package com.spring.boot.userservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/15 14:07
 */
@Data
@Schema(description = "用户信息响应VO")
public class UserVO {

	@Schema(
			description = "用户ID",
			example = "1"
	)
	private Long userId;

	@Schema(
			description = "用户名",
			example = "张三"
	)
	private String name;

	@Schema(
			description = "年龄",
			example = "20"
	)
	private Integer age;

	@Schema(
			description = "邮箱",
			example = "zhangsan@test.com"
	)
	private String email;

	@Schema(
			description = "创建时间",
			example = "2026-01-01 00:00:00"
	)
	private LocalDateTime createTime;

	@Schema(
			description = "修改时间",
			example = "2027-01-01 00:00:00"
	)
	private LocalDateTime updateTime;
}
