package com.spring.boot.stockservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spring.boot.stockservice.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/9 14:30
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {

	Stock selectByProductId(@Param("productId") Long productId);

	Integer deductStock(@Param("productId") Long productId, @Param("num") Integer num);

}
