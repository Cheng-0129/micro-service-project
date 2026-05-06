package com.spring.boot.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 15:25
 */
@Data
public class OrderCreateDTO {

	@Schema(description = "用户Id", example = "1")
	@NotNull(message = "用户Id不能为空")
	@Min(value = 1, message = "用户Id不能小于0")
	private Long userId;

	@Schema(description = "产品Id", example = "1")
	@NotNull(message = "产品Id不能为空")
	@Min(value = 1, message = "产品Id不能小于0")
	private Long productId;

	@Schema(description = "商品数量", example = "10")
	@NotNull(message = "商品数量不能为空")
	@Min(value = 1, message = "商品数量不能小于0")
	private Integer num;
}
