package com.spring.boot.orderservice.unit;

import com.spring.boot.commoncore.result.*;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.constant.FeignHeaders;
import com.spring.boot.commonweb.component.IdGenerator;
import com.spring.boot.orderservice.common.OrderStatus;
import com.spring.boot.orderservice.convert.OrderConvertMapper;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.entity.Order;
import com.spring.boot.orderservice.feign.StockClient;
import com.spring.boot.orderservice.feign.UserClient;
import com.spring.boot.orderservice.mapper.OrderMapper;
import com.spring.boot.orderservice.mq.OrderMessageProducer;
import com.spring.boot.orderservice.service.impl.OrderServiceImpl;
import com.spring.boot.orderservice.vo.OrderAddBackVO;
import com.spring.boot.orderservice.vo.OrderCreateVO;
import com.spring.boot.orderservice.vo.OrderDetailVO;
import com.spring.boot.orderservice.vo.feign.StockAddBackFeignVO;
import com.spring.boot.orderservice.vo.feign.StockDeductFeignVO;
import com.spring.boot.orderservice.vo.feign.UserFeignVO;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/28 09:03
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 单元测试")
public class OrderServiceTest {

	@Mock
	private OrderMapper orderMapper;
	@Mock
	private StockClient stockClient;
	@Mock
	private UserClient userClient;
	@Mock
	private OrderConvertMapper orderConvertMapper;
	@Mock
	private OrderMessageProducer orderMessageProducer;
	@Mock
	private IdGenerator orderIdGenerator;

	@InjectMocks
	private OrderServiceImpl orderService;

	private OrderCreateDTO validDTO;
	private UserFeignVO mockUser;
	private StockDeductFeignVO mockStockDeduct;
	private Order mockOrder;

	private static final Long TEST_USER_ID = 1L;
	private static final Long TEST_PRODUCT_ID = 100L;
	private static final Integer TEST_QUANTITY = 2;
	private static final Long TEST_ORDER_NO = 100000000001L;
	private static final Long TEST_ORDER_ID = 1L;

	// ==================== 工具方法 ====================

	/**
	 * 构建一个 FeignException，用于模拟下游服务抛异常的场景
	 */
	private static FeignException buildFeignException(int status, String message) {
		Request request = Request.create(
				Request.HttpMethod.GET,
				"http://localhost",
				Collections.emptyMap(),
				null, null, null
		);
		Response response = Response.builder()
				.status(status)
				.reason(message)
				.request(request)
				.build();
		return FeignException.errorStatus("mock", response);
	}

	@BeforeEach
	void setUp() {
		validDTO = new OrderCreateDTO();
		validDTO.setUserId(TEST_USER_ID);
		validDTO.setProductId(TEST_PRODUCT_ID);
		validDTO.setNum(TEST_QUANTITY);

		mockUser = new UserFeignVO();
		mockUser.setUserId(TEST_USER_ID);

		mockStockDeduct = new StockDeductFeignVO();
		mockStockDeduct.setProductName("测试商品");
		mockStockDeduct.setPrice(new BigDecimal("99.00"));
		mockStockDeduct.setStock(98);

		mockOrder = new Order();
		mockOrder.setId(TEST_ORDER_ID);
		mockOrder.setOrderNo(TEST_ORDER_NO);
		mockOrder.setUserId(TEST_USER_ID);
		mockOrder.setProductId(TEST_PRODUCT_ID);
		mockOrder.setNum(TEST_QUANTITY);
	}

	// ==================== createOrder 测试 ====================

	@Nested
	@DisplayName("createOrder - 正常场景")
	class CreateOrderSuccess {

		@Test
		@DisplayName("外部调用时，应校验用户并成功创建订单")
		void shouldCreateOrderSuccessfullyWhenExternalCall() {
			// Given
			when(userClient.getById(TEST_USER_ID)).thenReturn(Result.success(mockUser));
			when(orderConvertMapper.toEntity(validDTO)).thenReturn(mockOrder);
			when(orderIdGenerator.nextId()).thenReturn(TEST_ORDER_NO);
			when(orderMapper.insert(mockOrder)).thenReturn(1);
			when(stockClient.deductStock(TEST_PRODUCT_ID, TEST_QUANTITY))
					.thenReturn(Result.success(mockStockDeduct));
			when(orderMapper.updateById(mockOrder)).thenReturn(1);

			OrderCreateVO mockVO = new OrderCreateVO();
			mockVO.setOrderNo(TEST_ORDER_NO);
			mockVO.setProductName("测试商品");
			mockVO.setStock(98);
			mockVO.setAmount(new BigDecimal("198.00"));
			when(orderConvertMapper.toOrderCreateVO(mockOrder)).thenReturn(mockVO);

			// When
			OrderCreateVO result = orderService.createOrder(validDTO, "external");

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getOrderNo()).isEqualTo(TEST_ORDER_NO);
			assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("198.00"));

			verify(userClient).getById(TEST_USER_ID);
			verify(orderMapper).insert(mockOrder);
			verify(stockClient).deductStock(TEST_PRODUCT_ID, TEST_QUANTITY);
			verify(orderMapper).updateById(mockOrder);

			ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
			verify(orderMapper).updateById(orderCaptor.capture());
			assertThat(orderCaptor.getValue().getStatus()).isEqualTo(OrderStatus.CREATED.getCode());
		}

		@Test
		@DisplayName("来源为用户服务时，应跳过用户校验")
		void shouldSkipUserValidationWhenSourceIsUserService() {
			// Given
			when(orderConvertMapper.toEntity(validDTO)).thenReturn(mockOrder);
			when(orderIdGenerator.nextId()).thenReturn(TEST_ORDER_NO);
			when(orderMapper.insert(mockOrder)).thenReturn(1);
			when(stockClient.deductStock(TEST_PRODUCT_ID, TEST_QUANTITY))
					.thenReturn(Result.success(mockStockDeduct));
			when(orderMapper.updateById(mockOrder)).thenReturn(1);

			OrderCreateVO mockVO = new OrderCreateVO();
			mockVO.setOrderNo(TEST_ORDER_NO);
			when(orderConvertMapper.toOrderCreateVO(mockOrder)).thenReturn(mockVO);

			// When
			orderService.createOrder(validDTO, FeignHeaders.SOURCE_USER_SERVICE);

			// Then
			verify(userClient, never()).getById(anyLong());
		}
	}

	@Nested
	@DisplayName("createOrder - 异常场景")
	class CreateOrderFail {

		@Test
		@DisplayName("用户服务不可用时应抛出 USER_SERVICE_DEGRADE 异常")
		void shouldThrowDegradeExceptionWhenUserServiceUnavailable() {
			when(userClient.getById(TEST_USER_ID))
					.thenThrow(buildFeignException(500, "Internal Server Error"));

			assertThatThrownBy(() -> orderService.createOrder(validDTO, "external"))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.USER_SERVICE_DEGRADE.getCode());

			verify(orderMapper, never()).insert(ArgumentMatchers.<Order>any());
			verify(stockClient, never()).deductStock(anyLong(), anyInt());
		}

		@Test
		@DisplayName("订单插入失败时应抛出 ORDER_ADD_FAILED 异常")
		void shouldThrowExceptionWhenOrderInsertFails() {
			when(userClient.getById(TEST_USER_ID)).thenReturn(Result.success(mockUser));
			when(orderConvertMapper.toEntity(validDTO)).thenReturn(mockOrder);
			when(orderIdGenerator.nextId()).thenReturn(TEST_ORDER_NO);
			when(orderMapper.insert(mockOrder)).thenReturn(0);

			assertThatThrownBy(() -> orderService.createOrder(validDTO, "external"))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_ADD_FAILED.getCode());
		}

		@Test
		@DisplayName("库存扣减失败时应抛出 FeignException")
		void shouldThrowExceptionWhenStockDeductFails() {
			when(userClient.getById(TEST_USER_ID)).thenReturn(Result.success(mockUser));
			when(orderConvertMapper.toEntity(validDTO)).thenReturn(mockOrder);
			when(orderIdGenerator.nextId()).thenReturn(TEST_ORDER_NO);
			when(orderMapper.insert(mockOrder)).thenReturn(1);
			when(stockClient.deductStock(TEST_PRODUCT_ID, TEST_QUANTITY))
					.thenThrow(buildFeignException(500, "Internal Server Error"));

			assertThatThrownBy(() -> orderService.createOrder(validDTO, "external"))
					.isInstanceOf(FeignException.class);
		}

		@Test
		@DisplayName("库存扣减返回 null 时应抛出 FEIGN_ERROR 异常")
		void shouldThrowFeignErrorWhenStockDeductReturnsNull() {
			when(userClient.getById(TEST_USER_ID)).thenReturn(Result.success(mockUser));
			when(orderConvertMapper.toEntity(validDTO)).thenReturn(mockOrder);
			when(orderIdGenerator.nextId()).thenReturn(TEST_ORDER_NO);
			when(orderMapper.insert(mockOrder)).thenReturn(1);
			when(stockClient.deductStock(TEST_PRODUCT_ID, TEST_QUANTITY)).thenReturn(null);

			assertThatThrownBy(() -> orderService.createOrder(validDTO, "external"))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.FEIGN_ERROR.getCode());
		}

		@Test
		@DisplayName("订单状态更新失败时应抛出 ORDER_ADD_FAILED 异常")
		void shouldThrowExceptionWhenOrderUpdateFails() {
			when(userClient.getById(TEST_USER_ID)).thenReturn(Result.success(mockUser));
			when(orderConvertMapper.toEntity(validDTO)).thenReturn(mockOrder);
			when(orderIdGenerator.nextId()).thenReturn(TEST_ORDER_NO);
			when(orderMapper.insert(mockOrder)).thenReturn(1);
			when(stockClient.deductStock(TEST_PRODUCT_ID, TEST_QUANTITY))
					.thenReturn(Result.success(mockStockDeduct));
			when(orderMapper.updateById(mockOrder)).thenReturn(0);

			assertThatThrownBy(() -> orderService.createOrder(validDTO, "external"))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_ADD_FAILED.getCode());
		}
	}

	// ==================== cancelOrder 测试 ====================

	@Nested
	@DisplayName("cancelOrder - 状态分支测试")
	class CancelOrderTest {

		@Test
		@DisplayName("PENDING 状态：直接取消，返回 null")
		void shouldCancelPendingOrderDirectly() {
			Order pendingOrder = new Order();
			pendingOrder.setId(1L);
			pendingOrder.setOrderNo(TEST_ORDER_NO);
			pendingOrder.setStatus(OrderStatus.PENDING.getCode());

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(pendingOrder);
			when(orderMapper.updateById(pendingOrder)).thenReturn(1);

			OrderAddBackVO result = orderService.cancelOrder(TEST_ORDER_NO);

			assertThat(result).isNull();
			assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED.getCode());
			verify(stockClient, never()).addBackStock(anyLong(), anyInt());
		}

		@Test
		@DisplayName("CREATED 状态：取消订单并回滚库存")
		void shouldCancelCreatedOrderAndRollbackStock() {
			Order createdOrder = new Order();
			createdOrder.setId(1L);
			createdOrder.setOrderNo(TEST_ORDER_NO);
			createdOrder.setUserId(TEST_USER_ID);
			createdOrder.setProductId(TEST_PRODUCT_ID);
			createdOrder.setNum(TEST_QUANTITY);
			createdOrder.setStatus(OrderStatus.CREATED.getCode());

			StockAddBackFeignVO stockVO = new StockAddBackFeignVO();
			stockVO.setStock(100);

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(createdOrder);
			when(stockClient.addBackStock(TEST_PRODUCT_ID, TEST_QUANTITY))
					.thenReturn(Result.success(stockVO));
			when(orderMapper.updateById(createdOrder)).thenReturn(1);

			OrderAddBackVO mockVO = new OrderAddBackVO();
			mockVO.setOrderNo(TEST_ORDER_NO);
			when(orderConvertMapper.toOrderAddBackVO(stockVO)).thenReturn(mockVO);

			OrderAddBackVO result = orderService.cancelOrder(TEST_ORDER_NO);

			assertThat(result).isNotNull();
			verify(stockClient).addBackStock(TEST_PRODUCT_ID, TEST_QUANTITY);
			verify(orderMessageProducer).sendOrderCancelMessage(TEST_PRODUCT_ID);
		}

		@Test
		@DisplayName("PAID 状态：不可取消，抛出 ORDER_STATUS_ERROR")
		void shouldThrowExceptionWhenCancellingPaidOrder() {
			Order paidOrder = new Order();
			paidOrder.setStatus(OrderStatus.PAID.getCode());

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(paidOrder);

			assertThatThrownBy(() -> orderService.cancelOrder(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_STATUS_ERROR.getCode());
		}

		@Test
		@DisplayName("CANCELLED 状态：重复取消抛出 ORDER_WAS_CANCELED")
		void shouldThrowExceptionWhenCancellingCancelledOrder() {
			Order cancelledOrder = new Order();
			cancelledOrder.setStatus(OrderStatus.CANCELLED.getCode());

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(cancelledOrder);

			assertThatThrownBy(() -> orderService.cancelOrder(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_WAS_CANCELED.getCode());
		}

		@Test
		@DisplayName("订单不存在：抛出 ORDER_NOT_EXIST")
		void shouldThrowExceptionWhenOrderNotFound() {
			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(null);

			assertThatThrownBy(() -> orderService.cancelOrder(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_NOT_EXIST.getCode());
		}

		@Test
		@DisplayName("CREATED 状态：回滚失败抛出 ORDER_CANCEL_FAILED")
		void shouldThrowExceptionWhenStockRollbackFails() {
			Order createdOrder = new Order();
			createdOrder.setId(1L);
			createdOrder.setProductId(TEST_PRODUCT_ID);
			createdOrder.setNum(TEST_QUANTITY);
			createdOrder.setStatus(OrderStatus.CREATED.getCode());

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(createdOrder);
			when(stockClient.addBackStock(TEST_PRODUCT_ID, TEST_QUANTITY))
					.thenThrow(new RuntimeException("库存服务不可用"));

			assertThatThrownBy(() -> orderService.cancelOrder(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_CANCEL_FAILED.getCode());
		}
	}

	// ==================== getByOrderNo 测试 ====================

	@Nested
	@DisplayName("getByOrderNo - 查询订单")
	class GetByOrderNoTest {

		@Test
		@DisplayName("订单存在时应返回订单详情")
		void shouldReturnOrderWhenExists() {
			Order order = new Order();
			order.setOrderNo(TEST_ORDER_NO);
			order.setUserId(TEST_USER_ID);
			order.setProductId(TEST_PRODUCT_ID);

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(order);

			OrderDetailVO mockVO = new OrderDetailVO();
			mockVO.setOrderNo(TEST_ORDER_NO);
			when(orderConvertMapper.toOrderDetailVO(order)).thenReturn(mockVO);

			OrderDetailVO result = orderService.getByOrderNo(TEST_ORDER_NO);

			assertThat(result).isNotNull();
			assertThat(result.getOrderNo()).isEqualTo(TEST_ORDER_NO);
		}

		@Test
		@DisplayName("订单不存在时应抛出 ORDER_NOT_EXIST")
		void shouldThrowExceptionWhenOrderNotExists() {
			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(null);

			assertThatThrownBy(() -> orderService.getByOrderNo(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_NOT_EXIST.getCode());
		}
	}

	// ==================== deleteOrder 测试 ====================

	@Nested
	@DisplayName("deleteOrder - 删除订单")
	class DeleteOrderTest {

		@Test
		@DisplayName("仅可删除已取消的订单")
		void shouldDeleteCancelledOrder() {
			Order cancelledOrder = new Order();
			cancelledOrder.setId(1L);
			cancelledOrder.setOrderNo(TEST_ORDER_NO);
			cancelledOrder.setStatus(OrderStatus.CANCELLED.getCode());

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(cancelledOrder);
			when(orderMapper.deleteById(1L)).thenReturn(1);

			assertThatCode(() -> orderService.deleteOrder(TEST_ORDER_NO))
					.doesNotThrowAnyException();
		}

		@Test
		@DisplayName("删除非已取消状态的订单应抛出 ORDER_STATUS_ERROR")
		void shouldThrowExceptionWhenDeletingNonCancelledOrder() {
			Order pendingOrder = new Order();
			pendingOrder.setStatus(OrderStatus.PENDING.getCode());

			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(pendingOrder);

			assertThatThrownBy(() -> orderService.deleteOrder(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_STATUS_ERROR.getCode());
		}

		@Test
		@DisplayName("删除不存在的订单应抛出 ORDER_NOT_EXIST")
		void shouldThrowExceptionWhenDeletingNonExistentOrder() {
			when(orderMapper.selectByOrderNo(TEST_ORDER_NO)).thenReturn(null);

			assertThatThrownBy(() -> orderService.deleteOrder(TEST_ORDER_NO))
					.isInstanceOf(BusinessException.class)
					.extracting("code")
					.isEqualTo(ResultCode.ORDER_NOT_EXIST.getCode());
		}
	}
}
