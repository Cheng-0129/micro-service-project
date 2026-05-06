package com.spring.boot.orderservice.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.orderservice.common.OrderStatus;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.entity.Order;
import com.spring.boot.orderservice.feign.StockClient;
import com.spring.boot.orderservice.mapper.OrderMapper;
import com.spring.boot.orderservice.service.OrderService;
import com.spring.boot.orderservice.vo.OrderVO;
import com.spring.boot.orderservice.vo.StockDeductVO;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.spring.boot.commoncore.result.ResultCode.FEIGN_ERROR;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 15:14
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

	@Resource
	private OrderMapper orderMapper;

	@Resource
	private StockClient stockClient;

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public OrderVO createOrder(OrderCreateDTO dto) {

		log.info("【订单模块】开始创建订单，userId={}, productId={}, num={}",
				dto.getUserId(), dto.getProductId(), dto.getNum());
		Order order = new Order();

		Long orderNo = jdbcTemplate.queryForObject(
				"UPDATE biz_id_counter SET current_max_id = current_max_id + 1 WHERE table_name = 't_order' RETURNING current_max_id",
				Long.class
		);
		log.info("【订单模块】生成订单号：{}", orderNo);

		order.setOrderNo(orderNo);
		order.setUserId(dto.getUserId());
		order.setProductId(dto.getProductId());
		order.setNum(dto.getNum());
		order.setAmount(BigDecimal.ZERO);
		order.setStatus(OrderStatus.PENDING.getCode());
		orderMapper.insert(order);
		log.info("【订单模块】预订单创建成功，订单ID：{}，订单状态：PENDING", order.getId());

		log.info("【订单模块】调用库存服务扣减库存，订单号：{}，productId={}, num={}",
				orderNo, dto.getProductId(), dto.getNum());
		Result<StockDeductVO> result = stockClient.deductStock(dto.getProductId(), dto.getNum());

		if (result == null || result.isFail() || result.getData() == null) {
			log.warn("【订单模块】扣减库存失败，订单号：{}", orderNo);
			throw new BusinessException(FEIGN_ERROR.getCode(), "扣库存失败，订单回滚");
		}

		StockDeductVO deductVO = result.getData();
		log.info("【订单模块】库存扣减成功，productName={}, price={}, 剩余库存={}",
				deductVO.getProductName(), deductVO.getPrice(), deductVO.getStock());

		BigDecimal amount = deductVO.getPrice()
				.multiply(new BigDecimal(dto.getNum()));
		order.setAmount(amount);
		order.setStatus(OrderStatus.CREATED.getCode());
		orderMapper.updateById(order);
		log.info("【订单模块】订单更新成功，订单ID：{}，金额：{}，状态：CREATED", order.getId(), amount);

		OrderVO vo = new OrderVO();
		vo.setOrderNo(orderNo);
		vo.setUserId(dto.getUserId());
		vo.setProductId(dto.getProductId());
		vo.setProductName(deductVO.getProductName());
		vo.setStock(deductVO.getStock());
		vo.setAmount(amount);

		log.info("【订单模块】订单创建完成，订单号：{}，产品：{}，数量：{}，金额：{}",
				orderNo, deductVO.getProductName(), dto.getNum(), amount);
		return vo;
	}
}
