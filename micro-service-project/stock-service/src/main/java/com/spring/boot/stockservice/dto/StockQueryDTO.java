package com.spring.boot.stockservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/6 15:10
 */
@Data
@Schema(description = "库存分页查询DTO")
public class StockQueryDTO {

	@Schema(description = "当前页", example = "1")
	@NotNull(message = "当前页不能为空")
	@Min(value = 1, message = "当前页不能小于1")
	private Integer pageNum = 1;
	@Schema(description = "每页条数", example = "10")
	@NotNull(message = "每页条数不能为空")
	@Min(value = 1, message = "每页条数不能小于1")
	@Max(value = 100, message = "每页条数不能超过100")
	private Integer pageSize = 10;

	@Schema(description = "产品ID", example = "1")
	@Positive(message = "产品ID必须为正整数")
	private Long productId;
	@Schema(description = "产品名称", example = "产品A")
	@Size(max = 100, message = "产品名称不能超过100字符")
	private String productName;


	@Schema(description = "最低单价", example = "10.00")
	@DecimalMin(value = "0.00", inclusive = false, message = "单价必须大于0")
	private BigDecimal minPrice;
	@Schema(description = "最高单价", example = "100.00")
	@DecimalMin(value = "0.00", inclusive = false, message = "单价必须大于0")
	private BigDecimal maxPrice;
	@Schema(description = "最少库存数量", example = "10")
	@Min(value = 0, message = "库存数量不能小于0")
	private Integer minStock;
	@Schema(description = "最多库存数量", example = "100")
	@Min(value = 0, message = "库存数量不能小于0")
	private Integer maxStock;


	@Schema(description = "创建时间开始", example = "2026-01-01 00:00:00")
	private LocalDateTime startTime;
	@Schema(description = "创建时间结束", example = "2027-01-01 00:00:00")
	private LocalDateTime endTime;

	@Schema(description = "修改时间开始", example = "2026-01-01 00:00:00")
	private LocalDateTime updateStartTime;
	@Schema(description = "修改时间结束", example = "2027-01-01 00:00:00")
	private LocalDateTime updateEndTime;
}
