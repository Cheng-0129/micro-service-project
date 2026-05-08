package com.spring.boot.stockservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/9 14:28
 */
@Data
@TableName("t_stock")
public class Stock {

	@TableId(type = IdType.AUTO)
	private Long id;
	private Long productId;
	private String productName;
	private Integer stock;
	private BigDecimal price;
	private LocalDateTime createTime;
	private LocalDateTime updateTime;
}
