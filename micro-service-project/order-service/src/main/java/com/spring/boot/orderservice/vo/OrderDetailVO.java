package com.spring.boot.orderservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/7 08:52
 */
@Data
@Schema(description = "订单详情信息")
public class OrderDetailVO {

	@Schema(description = "订单号", example = "1")
	private Long orderNo;
	@Schema(description = "用户ID", example = "1")
	private Long userId;
	@Schema(description = "产品ID", example = "1")
	private Long productId;
	@Schema(description = "商品余量", example = "50")
	private Integer num;
	@Schema(description = "金额", example = "100")
	private BigDecimal amount;
	@Schema(description = "订单状态：-1-已取消, 0-待处理, 1-已创建, 2-已支付", example = "0")
	private Integer status;
	@Schema(description = "创建时间", example = "2026-01-01 00:00:00")
	private LocalDateTime createTime;
	@Schema(description = "更新时间", example = "2027-01-01 00:00:00")
	private LocalDateTime updateTime;
}
