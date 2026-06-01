package com.spring.boot.orderservice.dto;

import com.spring.boot.orderservice.common.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/7 09:30
 */
@Data
@Schema(description = "订单查询DTO")
public class OrderQueryDTO {

	@Schema(description = "当前页", example = "1")
	@NotNull(message = "当前页不能为空")
	@Min(value = 1, message = "当前页不能小于1")
	private Integer pageNum = 1;
	@Schema(description = "每页条数", example = "10")
	@NotNull(message = "每页条数不能为空")
	@Min(value = 1, message = "每页条数不能小于1")
	@Max(value = 100, message = "每页条数不能超过100")
	private Integer pageSize = 10;

	@Schema(description = "订单号", example = "1")
	@Positive(message = "订单号必须为正整数")
	private Long orderNo;
	@Schema(description = "用户ID", example = "1")
	@Positive(message = "用户ID必须为正整数")
	private Long userId;
	@Schema(description = "产品ID", example = "1")
	@Positive(message = "产品ID必须为正整数")
	private Long productId;

	@Schema(description = "最少商品数量", example = "10")
	@Positive(message = "商品数量必须为正数")
	private Integer minNum;
	@Schema(description = "最多商品数量", example = "100")
	@Positive(message = "商品数量必须为正数")
	private Integer maxNum;
	@Schema(description = "最低金额", example = "10.00")
	@Positive(message = "金额必须为正数")
	private BigDecimal minAmount;
	@Schema(description = "最高金额", example = "100.00")
	@Positive(message = "金额必须为正数")
	private BigDecimal maxAmount;
	@Schema(description = "订单状态：-1-已取消, 0-待处理, 1-已创建, 2-已支付", example = "1",
			allowableValues = {"-1", "0", "1", "2"})
	private OrderStatus status;

	@Schema(description = "创建时间开始", example = "2026-01-01 00:00:00")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;
	@Schema(description = "创建时间结束", example = "2027-01-01 00:00:00")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

	@Schema(description = "修改时间开始", example = "2026-01-01 00:00:00")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateStartTime;
	@Schema(description = "修改时间结束", example = "2027-01-01 00:00:00")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updateEndTime;
}
