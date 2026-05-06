package com.spring.boot.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/23 17:05
 */
@Data
@Schema(description = "用户查询DTO")
public class UserQueryDTO {

	@Schema(description = "当前页", example = "1")
	@NotNull(message = "当前页不能为空")
	@Min(value = 1, message = "当前页不能小于1")
	private Integer pageNum = 1;

	@Schema(description = "每页条数", example = "10")
	@NotNull(message = "每页条数不能为空")
	@Min(value = 1, message = "每页条数不能小于1")
	@Max(value = 100, message = "每页条数不能超过100")
	private Integer pageSize = 10;

	@Schema(description = "用户ID(精确匹配)", example = "1")
	@Positive(message = "用户ID必须为正整数")
	private Long userId;
	@Schema(description = "用户名(模糊匹配)", example = "张三")
	@Size(min = 2, max = 10, message = "用户名的长度为2-10")
	private String name;
	@Schema(description = "年龄(精确匹配)", example = "20")
	@Min(value = 0, message = "年龄不能小于0")
	@Max(value = 150, message = "年龄必须小于150")
	private Integer age;
	@Schema(description = "邮箱(模糊匹配)", example = "zhangsan@test.com")
	private String email;

	@Schema(description = "创建时间开始", example = "2026-01-01 00:00:00")
	private LocalDateTime startTime;
	@Schema(description = "创建时间结束",
			example = "2027-01-01 00:00:00")
	private LocalDateTime endTime;

	@Schema(description = "修改时间开始", example = "2026-01-01 00:00:00")
	private LocalDateTime updateStartTime;
	@Schema(description = "修改时间结束", example = "2027-01-01 00:00:00")
	private LocalDateTime updateEndTime;
}
