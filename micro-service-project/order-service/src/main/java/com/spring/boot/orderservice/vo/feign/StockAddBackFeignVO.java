package com.spring.boot.orderservice.vo.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/7 14:27
 */
@Data
@Schema(description = "库存回滚信息响应VO（Feign）")
public class StockAddBackFeignVO {

	@Schema(description = "产品ID", example = "1")
	private Long productId;
	@Schema(description = "产品名称", example = "产品A")
	private String productName;
	@Schema(description = "库存数量", example = "100")
	private Integer stock;
}
