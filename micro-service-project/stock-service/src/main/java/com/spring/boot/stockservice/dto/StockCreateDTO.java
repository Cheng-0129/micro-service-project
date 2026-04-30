package com.spring.boot.stockservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/29 08:31
 */
@Data
@Schema(description = "创建库存请求")
public class StockCreateDTO {

	@NotBlank(message = "产品名称不能为空")
	@Schema(description = "产品名称", example = "产品A")
	private String productName;
	@NotNull(message = "库存数量不能为空")
	@Min(value = 0, message = "库存数量不能小于0")
	@Schema(description = "库存数量", example = "100")
	private Integer stock;
	@NotNull(message = "单价不能为空")
	@Min(value = 0, message = "单价不能小于0")
	@Schema(description = "单价", example = "10")
	private BigDecimal price;
}
