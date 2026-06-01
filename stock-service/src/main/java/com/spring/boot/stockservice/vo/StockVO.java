package com.spring.boot.stockservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/28 10:05
 */
@Data
@Schema(description = "库存信息响应VO")
public class StockVO {

	@Schema(description = "产品ID", example = "1")
	private Long productId;
	@Schema(description = "产品名称", example = "产品A")
	private String productName;
	@Schema(description = "库存数量", example = "100")
	private Integer stock;
	@Schema(description = "单价", example = "10")
	private BigDecimal price;
	@Schema(description = "创建时间", example = "2026-01-01 00:00:00")
	private LocalDateTime createTime;
	@Schema(description = "更新时间", example = "2027-01-01 00:00:00")
	private LocalDateTime updateTime;
}
