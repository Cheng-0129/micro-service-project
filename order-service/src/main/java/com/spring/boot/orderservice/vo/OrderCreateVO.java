package com.spring.boot.orderservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 16:51
 */
@Data
@Schema(description = "订单信息")
public class OrderCreateVO {

	@Schema(description = "订单号", example = "1")
	private Long orderNo;
	@Schema(description = "用户ID", example = "1")
	private Long userId;
	@Schema(description = "产品ID", example = "1")
	private Long productId;
	@Schema(description = "产品名称", example = "产品A")
	private String productName;
	@Schema(description = "库存余量", example = "50")
	private Integer stock;
	@Schema(description = "金额", example = "100")
	private BigDecimal amount;
}
