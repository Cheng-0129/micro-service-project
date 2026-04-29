package com.spring.boot.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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

	@Schema(
			description = "当前页",
			example = "1",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@Min(value = 1, message = "当前页不能小于1")
	@JsonProperty(index = 1)
	private Integer pageNum = 1;
	@Schema(
			description = "每页条数",
			example = "10",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@Min(value = 1, message = "每页条数不能小于1")
	@JsonProperty(index = 2)
	private Integer pageSize = 10;

	@Schema(
			description = "用户ID",
			example = "1",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@JsonProperty(value = "id", index = 3)
	private Long userId;
	@Schema(
			description = "用户名",
			example = "张三",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@Size(min = 2, max = 10, message = "用户名的长度为2-10")
	@JsonProperty(index = 4)
	private String name;
	@Schema(
			description = "年龄",
			example = "20",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@Min(value = 0, message = "年龄不能小于0")
	@Max(value = 150, message = "年龄必须小于150")
	@JsonProperty(index = 5)
	private Integer age;
	@Schema(
			description = "邮箱",
			example = "zhangsan@test.com",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@Email(message = "邮箱格式不正确")
	@JsonProperty(index = 6)
	private String email;


	@Schema(
			description = "创建时间开始",
			example = "2026-01-01 00:00:00",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@JsonProperty(index = 7)
	private LocalDateTime startTime;
	@Schema(
			description = "创建时间结束",
			example = "2027-01-01 00:00:00",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@JsonProperty(index = 8)
	private LocalDateTime endTime;

	@Schema(
			description = "修改时间开始",
			example = "2026-01-01 00:00:00",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@JsonProperty(index = 9)
	private LocalDateTime updateStartTime;
	@Schema(
			description = "修改时间结束",
			example = "2027-01-01 00:00:00",
			requiredMode = Schema.RequiredMode.NOT_REQUIRED
	)
	@JsonProperty(index = 10)
	private LocalDateTime updateEndTime;
}
