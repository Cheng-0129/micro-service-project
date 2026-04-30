package com.spring.boot.orderservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spring.boot.orderservice.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 14:38
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
