package com.spring.boot.stockservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/29 16:48
 */
@Data
@Schema(description = "库存扣除信息响应VO")
public class StockDeductVO {

	@Schema(description = "产品ID", example = "1")
	private Long productId;
	@Schema(description = "产品名称", example = "产品A")
	private String productName;
	@Schema(description = "库存数量", example = "100")
	private Integer stock;

}
