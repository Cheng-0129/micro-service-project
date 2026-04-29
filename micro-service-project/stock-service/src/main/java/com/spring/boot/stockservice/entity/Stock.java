package com.spring.boot.stockservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Timestamp;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/9 14:28
 */
@Data
@TableName("t_stock")
@Schema(description = "库存信息")
public class Stock {

	@TableId(type = IdType.AUTO)
	@Schema(description = "数据库主键ID", example = "1")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)  // 只序列化，不反序列化
	private Long id;

	@Schema(description = "产品ID", example = "1")
	private Long productId;

	@Schema(description = "产品名称", example = "产品A")
	@NotBlank(message = "产品名称不能为空")
	private String productName;

	@Schema(description = "库存数量", example = "100")
	@Min(value = 0, message = "库存数量不能小于0")
	private Integer stock;

	@Schema(description = "单价", example = "10")
	@Min(value = 0, message = "单价不能小于0")
	private Integer price;

	@Schema(description = "创建时间")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Timestamp createTime;

	@Schema(description = "更新时间")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Timestamp updateTime;
}
