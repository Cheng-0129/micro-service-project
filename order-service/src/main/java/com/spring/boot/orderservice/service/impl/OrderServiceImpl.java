package com.spring.boot.orderservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spring.boot.commoncore.constant.FeignHeaders;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.commonweb.component.IdGenerator;
import com.spring.boot.orderservice.common.OrderStatus;
import com.spring.boot.orderservice.convert.OrderConvertMapper;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.dto.OrderQueryDTO;
import com.spring.boot.orderservice.entity.Order;
import com.spring.boot.orderservice.feign.StockClient;
import com.spring.boot.orderservice.feign.UserClient;
import com.spring.boot.orderservice.mapper.OrderMapper;
import com.spring.boot.orderservice.mq.OrderMessageProducer;
import com.spring.boot.orderservice.service.OrderService;
import com.spring.boot.orderservice.vo.OrderAddBackVO;
import com.spring.boot.orderservice.vo.OrderCreateVO;
import com.spring.boot.orderservice.vo.OrderDetailVO;
import com.spring.boot.orderservice.vo.feign.StockAddBackFeignVO;
import com.spring.boot.orderservice.vo.feign.StockDeductFeignVO;
import com.spring.boot.orderservice.vo.feign.UserFeignVO;
import feign.FeignException;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;

import static com.spring.boot.commoncore.result.ResultCode.*;

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
	private UserClient userClient;

	@Resource
	private OrderConvertMapper orderConvertMapper;

	@Resource
	private OrderMessageProducer orderMessageProducer;

	@Autowired
	@Qualifier("orderIdGenerator")
	private IdGenerator orderIdGenerator;

	@Override
	@GlobalTransactional(rollbackFor = Exception.class)
	public OrderCreateVO createOrder(OrderCreateDTO dto, String source) {

		log.info("【订单模块】开始创建订单，userId={}, productId={}, num={}",
				dto.getUserId(), dto.getProductId(), dto.getNum());

		log.info("【订单模块】开始查询用户，userId={}", dto.getUserId());
		if (!FeignHeaders.SOURCE_USER_SERVICE.equals(source)) {
			UserFeignVO userVO = getUserById(dto.getUserId());
			log.info("【订单模块】经查询用户存在，userId={}", userVO.getUserId());
		} else {
			log.info("【订单模块】来源为用户模块，跳过用户校验，userId={}", dto.getUserId());
		}

		Order order = orderConvertMapper.toEntity(dto);

		Long orderNo = orderIdGenerator.nextId();
		log.info("【订单模块】生成订单号：{}", orderNo);

		order.setOrderNo(orderNo);
		order.setAmount(BigDecimal.ZERO);
		order.setStatus(OrderStatus.PENDING.getCode());
		int insertCount = orderMapper.insert(order);
		if (insertCount <= 0) {
			log.error("【订单模块】订单插入失败，订单ID：{}", order.getId());
			throw BusinessException.of(ORDER_ADD_FAILED);
		}
		log.info("【订单模块】预订单创建成功，订单ID：{}，订单状态：PENDING", order.getId());

		log.info("【订单模块】调用库存服务扣减库存，订单号：{}，productId={}, num={}",
				orderNo, dto.getProductId(), dto.getNum());
		Result<StockDeductFeignVO> result = stockClient.deductStock(dto.getProductId(), dto.getNum());

		if (result == null || result.getData() == null) {
			log.warn("【订单模块】扣减库存失败，返回结果为 null，订单号：{}", orderNo);
			throw BusinessException.of(FEIGN_ERROR);
		}

		StockDeductFeignVO deductVO = result.getData();
		log.info("【订单模块】库存扣减成功，productName={}, price={}, 剩余库存={}",
				deductVO.getProductName(), deductVO.getPrice(), deductVO.getStock());

		BigDecimal amount = deductVO.getPrice()
				.multiply(new BigDecimal(dto.getNum()));
		order.setAmount(amount);
		order.setStatus(OrderStatus.CREATED.getCode());

		int updateCount = orderMapper.updateById(order);
		if (updateCount <= 0) {
			log.error("【订单模块】订单状态更新失败，订单ID：{}", order.getId());
			throw BusinessException.of(ORDER_ADD_FAILED);
		}
		log.info("【订单模块】订单更新成功，订单ID：{}，金额：{}，状态：CREATED", order.getId(), amount);

		OrderCreateVO vo = orderConvertMapper.toOrderCreateVO(order);
		vo.setProductName(deductVO.getProductName());
		vo.setStock(deductVO.getStock());
		vo.setAmount(amount);

		String xid = RootContext.getXID();
		log.info("【订单模块】当前全局事务 XID: {}", xid);

		log.info("【订单模块】订单创建完成，订单号：{}，产品：{}，数量：{}，金额：{}",
				orderNo, deductVO.getProductName(), dto.getNum(), amount);
		return vo;
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
	@Transactional(rollbackFor = Exception.class)
	public OrderAddBackVO cancelOrder(Long orderNo) {

		log.info("【订单模块】开始取消订单，订单号：{}", orderNo);

		Order order = orderMapper.selectByOrderNo(orderNo);

		if (order == null) {
			log.warn("【订单模块】订单不存在，订单号：{}", orderNo);
			throw BusinessException.of(ORDER_NOT_EXIST);
		}
		switch (OrderStatus.fromCode(order.getStatus())) {
			case PENDING:
				log.info("【订单模块】订单待处理，开始取消订单，订单号：{}", orderNo);
				order.setStatus(OrderStatus.CANCELLED.getCode());
				orderMapper.updateById(order);
				log.info("【订单模块】订单取消成功，订单号：{}", orderNo);
				return null;

			case CREATED:
				log.info("【订单模块】订单已创建，开始取消订单，订单号：{}", orderNo);

				try {
					Result<StockAddBackFeignVO> result = stockClient.addBackStock(order.getProductId(), order.getNum());
					if (result == null || result.getData() == null) {
						log.error("【订单模块】库存回滚返回结果为 null，订单号：{}", orderNo);
						throw BusinessException.of(FEIGN_ERROR);
					}

					order.setStatus(OrderStatus.CANCELLED.getCode());
					orderMapper.updateById(order);

					StockAddBackFeignVO stockVO = result.getData();

					orderMessageProducer.sendOrderCancelMessage(order.getProductId());
					log.info("【订单模块】订单取消消息已发送至MQ，订单号：{}", orderNo);

					OrderAddBackVO vo = orderConvertMapper.toOrderAddBackVO(stockVO);
					vo.setOrderNo(orderNo);
					vo.setUserId(order.getUserId());
					return vo;
				} catch (Exception e) {
					log.error("【订单模块】取消订单异常，需人工处理！订单号={}, productId={}, num={}",
							orderNo, order.getProductId(), order.getNum(), e);
					throw BusinessException.of(ORDER_CANCEL_FAILED, "取消订单失败，请人工处理");
				}

			case PAID:
				log.info("【订单模块】订单已支付，无法取消订单，请走退款通道，订单号：{}", orderNo);
				throw BusinessException.of(ORDER_STATUS_ERROR, "订单已支付，无法取消订单，请走退款通道");

			case CANCELLED:
				log.warn("【订单模块】订单先前已取消，订单号：{}", orderNo);
				throw BusinessException.of(ORDER_WAS_CANCELED);
		}

		throw BusinessException.of(ORDER_STATUS_ERROR);
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


	private UserFeignVO getUserById(Long userId) {
		try {
			Result<UserFeignVO> result = userClient.getById(userId);
			if (result == null || result.getData() == null) {
				log.warn("【订单模块】用户查询返回结果为 null，userId={}", userId);
				throw BusinessException.of(FEIGN_ERROR);
			}
			return result.getData();
		} catch (FeignException e) {
			log.error("【订单模块】用户模块不可用，userId={}", userId);
			throw BusinessException.of(USER_SERVICE_DEGRADE);
		}
	}
}

