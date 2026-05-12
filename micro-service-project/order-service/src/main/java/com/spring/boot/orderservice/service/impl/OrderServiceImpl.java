package com.spring.boot.orderservice.service.impl;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.orderservice.common.OrderStatus;
import com.spring.boot.orderservice.convert.OrderConvertMapper;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.dto.OrderQueryDTO;
import com.spring.boot.orderservice.entity.Order;
import com.spring.boot.orderservice.feign.StockClient;
import com.spring.boot.orderservice.mapper.OrderMapper;
import com.spring.boot.orderservice.service.OrderService;
import com.spring.boot.orderservice.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.spring.boot.commoncore.result.ResultCode.*;
import static com.spring.boot.commoncore.util.ExceptionUtil.unwind;

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

	@Resource
	private OrderConvertMapper orderConvertMapper;

	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	@SentinelResource(value = "createOrder",
			fallback = "createOrderFallback",
			blockHandler = "createOrderBlock")
	public OrderCreateVO createOrder(OrderCreateDTO dto) {

		log.info("【订单模块】开始创建订单，userId={}, productId={}, num={}",
				dto.getUserId(), dto.getProductId(), dto.getNum());
		Order order = orderConvertMapper.toEntity(dto);

		Long orderNo = jdbcTemplate.queryForObject(
				"UPDATE biz_id_counter SET current_max_id = current_max_id + 1 WHERE table_name = 't_order' RETURNING current_max_id",
				Long.class
		);
		if (orderNo == null) {
			log.error("【订单模块】生成订单号失败");
			throw BusinessException.of(ORDER_ADD_FAILED, "生成订单号失败");
		}
		log.info("【订单模块】生成订单号：{}", orderNo);

		order.setOrderNo(orderNo);
		order.setAmount(BigDecimal.ZERO);
		order.setStatus(OrderStatus.PENDING.getCode());
		int insertCount = orderMapper.insert(order);
		if (insertCount <= 0) {
			log.error("【订单模块】订单插入失败，订单ID：{}", order.getId());
			throw BusinessException.of(ORDER_ADD_FAILED, "订单插入数据库失败");
		}
		log.info("【订单模块】预订单创建成功，订单ID：{}，订单状态：PENDING", order.getId());

		log.info("【订单模块】调用库存服务扣减库存，订单号：{}，productId={}, num={}",
				orderNo, dto.getProductId(), dto.getNum());
		Result<StockDeductVO> result = stockClient.deductStock(dto.getProductId(), dto.getNum());

		if (result == null || result.isFail() || result.getData() == null) {
			log.warn("【订单模块】扣减库存失败，订单号：{}", orderNo);
			throw BusinessException.of(FEIGN_ERROR, "扣库存失败，订单回滚");
		}

		StockDeductVO deductVO = result.getData();
		log.info("【订单模块】库存扣减成功，productName={}, price={}, 剩余库存={}",
				deductVO.getProductName(), deductVO.getPrice(), deductVO.getStock());

		BigDecimal amount = deductVO.getPrice()
				.multiply(new BigDecimal(dto.getNum()));
		order.setAmount(amount);
		order.setStatus(OrderStatus.CREATED.getCode());

		int updateCount = orderMapper.updateById(order);
		if (updateCount <= 0) {
			log.error("【订单模块】订单状态更新失败，订单ID：{}", order.getId());
			throw BusinessException.of(ORDER_ADD_FAILED, "订单更新数据库失败");
		}
		log.info("【订单模块】订单更新成功，订单ID：{}，金额：{}，状态：CREATED", order.getId(), amount);

		OrderCreateVO vo = orderConvertMapper.toOrderCreateVO(order);
		vo.setProductName(deductVO.getProductName());
		vo.setStock(deductVO.getStock());
		vo.setAmount(amount);

		log.info("【订单模块】订单创建完成，订单号：{}，产品：{}，数量：{}，金额：{}",
				orderNo, deductVO.getProductName(), dto.getNum(), amount);
		return vo;
	}
	public OrderCreateVO createOrderFallback(OrderCreateDTO order, Throwable e) {
		Throwable cause = unwind(e);
		// 业务异常：原样抛出去，Controller 的 catch 会接住并返回正确 code
		if (cause instanceof BusinessException) {
			throw (BusinessException) cause;
		}
		// 系统异常：抛通用降级异常
		log.error("【订单模块】创建订单降级，参数：{}，异常：{}", order, cause.getMessage());
		throw BusinessException.of(ORDER_DEGRADE, "创建订单失败，服务降级，请稍后重试");
	}
	public OrderCreateVO createOrderBlock(OrderCreateDTO order, BlockException e) {
		log.warn("【订单模块】创建订单被限流/熔断，参数：{}", order);
		throw BusinessException.of(ORDER_FLOWING);
	}

	@Override
	public OrderDetailVO getByOrderNo(Long orderNo) {

		log.info("【订单模块】开始查询订单，订单号：{}", orderNo);

		Order order = orderMapper.selectByOrderNo(orderNo);

		if (order == null) {
			log.warn("【订单模块】订单不存在，订单号：{}", orderNo);
			throw BusinessException.of(ORDER_NOT_EXIST);
		}

		OrderDetailVO vo = orderConvertMapper.toOrderDetailVO(order);

		log.info("【订单模块】查询订单成功，orderNo={}, userId={}, productId={}", orderNo, vo.getUserId(), vo.getProductId());

		return vo;
	}

	@Override
	public PageVO<OrderDetailVO> getOrderPage(OrderQueryDTO query) {

		log.info("【订单模块】开始分页查询订单，参数：{}", query);
		Page<Order> page = new Page<>(query.getPageNum(), query.getPageSize());

		IPage<Order> orderPage = orderMapper.selectOrderPage(page, query);

		PageVO<OrderDetailVO> pageVO = orderConvertMapper.toPageVO(orderPage);

		if (pageVO.getRecords().isEmpty()) {
			log.info("【订单模块】查询结果为空，参数：{}", query);
		}
		log.info("【订单模块】分页查询订单成功，返回{}条", pageVO.getRecords().size());
		return pageVO;
	}


	@Override
	@SentinelResource(value = "cancelOrder",
			fallback = "cancelOrderFallback",
			blockHandler = "cancelOrderBlock")
	public OrderAddBackVO cancelOrder(Long orderNo) {

		log.info("【订单模块】开始取消订单，订单号：{}", orderNo);

		Order order = orderMapper.selectByOrderNo(orderNo);

		if (order == null) {
			log.warn("【订单模块】订单不存在，订单号：{}", orderNo);
			throw BusinessException.of(ORDER_NOT_EXIST);
		}
		switch (OrderStatus.fromCode(order.getStatus())) {
			case PENDING:
				// TODO: 接入异步消息后，PENDING 状态需要调库存服务确认是否已扣库存
				log.info("【订单模块】订单待处理，开始取消订单，订单号：{}", orderNo);
				order.setStatus(OrderStatus.CANCELLED.getCode());
				orderMapper.updateById(order);
				log.info("【订单模块】订单取消成功，订单号：{}", orderNo);
				return null;

			case CREATED:
				log.info("【订单模块】订单已创建，开始取消订单，订单号：{}", orderNo);
				order.setStatus(OrderStatus.CANCELLED.getCode());
				orderMapper.updateById(order);

				try {
					Result<StockAddBackVO> result = stockClient.addBackStock(order.getProductId(), order.getNum());
					if (result == null || result.isFail()) {
						throw BusinessException.of(ORDER_ROLLBACK_FAILED, "库存回滚失败");
					}
					StockAddBackVO stockVO = result.getData();

					OrderAddBackVO vo = orderConvertMapper.toOrderAddBackVO(stockVO);
					vo.setOrderNo(orderNo);
					vo.setUserId(order.getUserId());
					return vo;

				} catch (Exception e) {
					log.error("【订单模块】库存回滚失败，需人工处理！订单号={}, productId={}, num={}",
							orderNo, order.getProductId(), order.getNum(), e);
					return null;
				}

			case PAID:
				log.info("【订单模块】订单已支付，无法取消订单，请走退款通道，订单号：{}", orderNo);
				throw BusinessException.of(ORDER_STATUS_ERROR, "订单已支付，无法取消订单，请走退款通道");

			case CANCELLED:
				log.warn("【订单模块】订单先前已取消，订单号：{}", orderNo);
				throw BusinessException.of(ORDER_WAS_CANCELED, "订单已取消，无法重复操作");
		}

		throw BusinessException.of(ORDER_STATUS_ERROR, "订单状态异常，无法取消");
	}
	public OrderAddBackVO cancelOrderFallback(Long orderNo, Throwable e) {
		Throwable cause = unwind(e);
		if (cause instanceof BusinessException) {
			throw (BusinessException) cause;
		}
		log.error("【订单模块】取消订单降级，订单号：{}，异常：{}", orderNo, cause.getMessage());
		throw BusinessException.of(ORDER_DEGRADE, "取消订单失败，服务降级，请稍后重试");
	}
	public OrderAddBackVO cancelOrderBlock(Long orderNo, BlockException e) {
		log.warn("【订单模块】取消订单被限流/熔断，订单号：{}", orderNo);
		throw BusinessException.of(ORDER_FLOWING);
	}

	@Override
	public void deleteOrder(Long orderNo) {
		log.info("【订单模块】开始删除订单，订单号：{}", orderNo);
		Order existing = orderMapper.selectByOrderNo(orderNo);
		if (existing == null) {
			log.warn("【校验层】删除失败，订单不存在");
			throw BusinessException.of(ORDER_NOT_EXIST);
		}

		log.debug("【订单模块】查询到已有记录，dbId={}, orderNo={}, status={}", existing.getId(), orderNo, existing.getStatus());
		if (!OrderStatus.fromCode(existing.getStatus()).equals(OrderStatus.CANCELLED)) {
			throw BusinessException.of(ORDER_STATUS_ERROR, "只能删除已取消的订单");
		}
		log.debug("【订单模块】订单已取消，开始删除");
		int success = orderMapper.deleteById(existing.getId());
		if (success != 1) {
			log.warn("【订单模块】订单删除失败，可能已被并发删除，orderNo={}", orderNo);
			throw BusinessException.of(ORDER_NOT_EXIST);
		}
		log.info("【订单模块】订单删除成功");
	}
}

