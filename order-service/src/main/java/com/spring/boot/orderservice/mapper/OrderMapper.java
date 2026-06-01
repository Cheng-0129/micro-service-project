package com.spring.boot.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spring.boot.orderservice.dto.OrderQueryDTO;
import com.spring.boot.orderservice.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 14:38
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

	Order selectByOrderNo(@Param("orderNo") Long orderNo);
	IPage<Order> selectOrderPage(Page<Order> page, @Param("query") OrderQueryDTO query);
}
