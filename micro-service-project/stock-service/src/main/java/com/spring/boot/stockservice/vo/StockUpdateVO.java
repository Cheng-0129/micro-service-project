package com.spring.boot.stockservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/29 10:31
 */
@Data
@Schema(description = "更新库存请求")
public class StockUpdateVO {

	@Schema(description = "产品名称", example = "产品A")
	@NotBlank(message = "产品名称不能为空")
	private String productName;

	@Schema(description = "库存数量", example = "100")
	@Min(value = 0, message = "库存数量不能小于0")
	private Integer stock;

	@Schema(description = "单价", example = "10")
	@Min(value = 0, message = "单价不能小于0")
	private Integer price;
}
