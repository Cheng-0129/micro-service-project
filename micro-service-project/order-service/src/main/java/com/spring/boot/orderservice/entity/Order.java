package com.spring.boot.orderservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/29 16:15
 */
@Data
@TableName("t_order")
@Schema(description = "订单信息")
public class Order {

	@TableId(type = IdType.AUTO)
	@Schema(description = "数据库主键ID", example = "1")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)  // 只序列化，不反序列化
	private Long id;

	@Schema(description = "订单号", example = "1")
	private Long orderNo;

	@Schema(description = "用户Id", example = "1")
	private Long userId;

	@Schema(description = "产品Id", example = "1")
	private Long productId;

	@Schema(description = "商品数量", example = "10")
	private Integer num;

	@Schema(description = "金额", example = "100")
	private BigDecimal amount;

	@Schema(description = "订单状态", example = "0")
	private Integer status;

	@Schema(description = "创建时间")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Timestamp createTime;

	@Schema(description = "更新时间")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Timestamp updateTime;
}
