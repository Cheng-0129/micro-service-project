package com.spring.boot.stockservice.unit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.stockservice.convert.StockConvertMapper;
import com.spring.boot.stockservice.dto.StockCreateDTO;
import com.spring.boot.stockservice.dto.StockQueryDTO;
import com.spring.boot.stockservice.dto.StockUpdateDTO;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.service.StockValidationService;
import com.spring.boot.stockservice.service.impl.StockServiceCacheImpl;
import com.spring.boot.stockservice.vo.StockAddBackVO;
import com.spring.boot.stockservice.vo.StockDeductVO;
import com.spring.boot.stockservice.vo.StockVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/6/1 09:05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockValidationService 单元测试")
class StockValidationServiceTest {

	@Mock
	private StockServiceCacheImpl stockService;

	@Mock
	private StockConvertMapper stockConvertMapper;

	@InjectMocks
	private StockValidationService validationService;

	private static final Long PRODUCT_ID = 1L;

	// ==================== addStock 测试 ====================

	@Test
	@DisplayName("新增库存 - 成功")
	void addStock_ShouldSucceed() {
		StockCreateDTO dto = new StockCreateDTO();
		Stock entity = buildStock();
		when(stockConvertMapper.toEntity(dto)).thenReturn(entity);
		when(stockService.addStock(entity)).thenReturn(entity);

		assertDoesNotThrow(() -> validationService.addStock(dto));
		verify(stockService).addStock(entity);
	}

	@Test
	@DisplayName("新增库存 - DB 返回 null，抛 STOCK_ADD_FAILED")
	void addStock_ShouldThrow_WhenDbReturnsNull() {
		StockCreateDTO dto = new StockCreateDTO();
		Stock entity = buildStock();
		when(stockConvertMapper.toEntity(dto)).thenReturn(entity);
		when(stockService.addStock(entity)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.addStock(dto));
		assertEquals(STOCK_ADD_FAILED.getCode(), ex.getCode());
	}

	// ==================== getStockByProductId 测试 ====================

	@Test
	@DisplayName("查询库存 - 存在，返回 StockVO")
	void getStockByProductId_ShouldReturnVO_WhenExists() {
		Stock stock = buildStock();
		StockVO vo = buildStockVO();
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(stock);
		when(stockConvertMapper.toStockVO(stock)).thenReturn(vo);

		StockVO result = validationService.getStockByProductId(PRODUCT_ID);

		assertNotNull(result);
		assertEquals(PRODUCT_ID, result.getProductId());
		assertEquals("测试商品", result.getProductName());
		assertEquals(100, result.getStock());
		assertEquals(new BigDecimal("99.99"), result.getPrice());
	}

	@Test
	@DisplayName("查询库存 - 不存在，抛 STOCK_NOT_EXIST")
	void getStockByProductId_ShouldThrow_WhenNotExists() {
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(null);

		com.spring.boot.commoncore.exception.BusinessException ex = assertThrows(com.spring.boot.commoncore.exception.BusinessException.class,
				() -> validationService.getStockByProductId(PRODUCT_ID));
		assertEquals(com.spring.boot.commoncore.result.ResultCode.STOCK_NOT_EXIST.getCode(), ex.getCode());
		assertTrue(ex.getMessage().contains("库存[ " + PRODUCT_ID + " ]不存在"));
	}

	// ==================== updateStockByProductId 测试 ====================

	@Test
	@DisplayName("更新库存 - 成功")
	void updateStockByProductId_ShouldSucceed() {
		Stock existing = buildStock();
		StockUpdateDTO dto = new StockUpdateDTO();
		Stock updatedEntity = new Stock();

		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(existing);
		when(stockConvertMapper.toEntity(dto)).thenReturn(updatedEntity);
		when(stockService.updateStock(any(Stock.class))).thenReturn(true);

		assertDoesNotThrow(() -> validationService.updateStockByProductId(PRODUCT_ID, dto));

		// 验证 updatedEntity 被正确设置了 id 和 productId
		assertEquals(existing.getId(), updatedEntity.getId());
		assertEquals(PRODUCT_ID, updatedEntity.getProductId());
		verify(stockService).updateStock(updatedEntity);
	}

	@Test
	@DisplayName("更新库存 - 库存不存在，抛 STOCK_NOT_EXIST")
	void updateStockByProductId_ShouldThrow_WhenStockNotExist() {
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.updateStockByProductId(PRODUCT_ID, new StockUpdateDTO()));
		assertEquals(STOCK_NOT_EXIST.getCode(), ex.getCode());
		// 库存不存在时，不会调用 toEntity 和 updateStock
		verify(stockConvertMapper, never()).toEntity(ArgumentMatchers.<StockCreateDTO>any());
		verify(stockService, never()).updateStock(any());
	}

	@Test
	@DisplayName("更新库存 - DB 更新失败返回 false，抛 STOCK_UPDATE_FAILED")
	void updateStockByProductId_ShouldThrow_WhenUpdateFailed() {
		Stock existing = buildStock();
		Stock updatedEntity = new Stock();

		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(existing);
		// 修复：使用 any(StockUpdateDTO.class) 匹配实际传入的参数
		when(stockConvertMapper.toEntity(any(StockUpdateDTO.class))).thenReturn(updatedEntity);
		when(stockService.updateStock(any(Stock.class))).thenReturn(false);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.updateStockByProductId(PRODUCT_ID, new StockUpdateDTO()));
		assertEquals(STOCK_UPDATE_FAILED.getCode(), ex.getCode());
	}

	// ==================== deleteStockByProductId 测试 ====================

	@Test
	@DisplayName("删除库存 - 成功")
	void deleteStockByProductId_ShouldSucceed() {
		Stock existing = buildStock();
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(existing);
		when(stockService.deleteStock(existing.getId(), PRODUCT_ID)).thenReturn(true);

		assertDoesNotThrow(() -> validationService.deleteStockByProductId(PRODUCT_ID));
		verify(stockService).deleteStock(existing.getId(), PRODUCT_ID);
	}

	@Test
	@DisplayName("删除库存 - 不存在，抛 STOCK_NOT_EXIST")
	void deleteStockByProductId_ShouldThrow_WhenNotExist() {
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.deleteStockByProductId(PRODUCT_ID));
		assertEquals(STOCK_NOT_EXIST.getCode(), ex.getCode());
		verify(stockService, never()).deleteStock(anyLong(), anyLong());
	}

	@Test
	@DisplayName("删除库存 - DB 删除失败返回 false，抛 STOCK_NOT_EXIST")
	void deleteStockByProductId_ShouldThrow_WhenDeleteFailed() {
		Stock existing = buildStock();
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(existing);
		when(stockService.deleteStock(existing.getId(), PRODUCT_ID)).thenReturn(false);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.deleteStockByProductId(PRODUCT_ID));
		assertEquals(STOCK_NOT_EXIST.getCode(), ex.getCode());
	}

	// ==================== deductStock 测试 ====================

	@Test
	@DisplayName("扣减库存 - 成功，返回 StockDeductVO")
	void deductStock_ShouldReturnDeductVO_WhenSuccess() {
		Stock existing = buildStock();
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(existing);
		when(stockService.deductStock(PRODUCT_ID, 10)).thenReturn(90);

		StockDeductVO result = validationService.deductStock(PRODUCT_ID, 10);

		assertNotNull(result);
		assertEquals(PRODUCT_ID, result.getProductId());
		assertEquals(90, result.getStock());
		assertEquals(existing.getProductName(), result.getProductName());
		assertEquals(existing.getPrice(), result.getPrice());
	}

	@Test
	@DisplayName("扣减库存 - 库存不存在，抛 STOCK_NOT_EXIST")
	void deductStock_ShouldThrow_WhenStockNotExist() {
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.deductStock(PRODUCT_ID, 10));
		assertEquals(STOCK_NOT_EXIST.getCode(), ex.getCode());
		verify(stockService, never()).deductStock(anyLong(), anyInt());
	}

	@Test
	@DisplayName("扣减库存 - 库存不足，DB 返回 null，抛 STOCK_INSUFFICIENT")
	void deductStock_ShouldThrow_WhenInsufficient() {
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(buildStock());
		when(stockService.deductStock(PRODUCT_ID, 999)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.deductStock(PRODUCT_ID, 999));
		assertEquals(STOCK_INSUFFICIENT.getCode(), ex.getCode());
	}

	// ==================== addBackStock 测试 ====================

	@Test
	@DisplayName("回滚库存 - 成功，返回 StockAddBackVO")
	void addBackStock_ShouldReturnAddBackVO_WhenSuccess() {
		Stock existing = buildStock();
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(existing);
		when(stockService.addBackStock(PRODUCT_ID, 10)).thenReturn(110);

		StockAddBackVO result = validationService.addBackStock(PRODUCT_ID, 10);

		assertNotNull(result);
		assertEquals(PRODUCT_ID, result.getProductId());
		assertEquals(110, result.getStock());
		assertEquals(existing.getProductName(), result.getProductName());
	}

	@Test
	@DisplayName("回滚库存 - 库存不存在，抛 STOCK_NOT_EXIST")
	void addBackStock_ShouldThrow_WhenStockNotExist() {
		when(stockService.getStockByProductId(PRODUCT_ID)).thenReturn(null);

		BusinessException ex = assertThrows(BusinessException.class,
				() -> validationService.addBackStock(PRODUCT_ID, 10));
		assertEquals(STOCK_NOT_EXIST.getCode(), ex.getCode());
		verify(stockService, never()).addBackStock(anyLong(), anyInt());
	}

	// ==================== getStockPage 测试 ====================

	@Test
	@DisplayName("分页查询 - 有数据，正常返回")
	void getStockPage_ShouldReturnPageWithData() {
		StockQueryDTO query = new StockQueryDTO();
		query.setPageNum(1);
		query.setPageSize(10);

		// 构造 mybatis-plus IPage
		Page<Stock> stockPage = new Page<>(1, 10);
		stockPage.setRecords(List.of(buildStock()));
		stockPage.setTotal(1);

		// 构造 PageVO，必须有非 null 的 records 列表
		PageVO<StockVO> pageVO = new PageVO<>();
		pageVO.setRecords(List.of(buildStockVO()));
		pageVO.setTotal(1L);
		pageVO.setCurrent(1L);
		pageVO.setSize(10L);

		when(stockService.getStockPage(query)).thenReturn(stockPage);
		when(stockConvertMapper.toPageVO(stockPage)).thenReturn(pageVO);

		PageVO<StockVO> result = validationService.getStockPage(query);

		assertNotNull(result);
		assertEquals(1, result.getRecords().size());
		assertEquals(1L, result.getTotal());
		verify(stockService).getStockPage(query);
	}

	@Test
	@DisplayName("分页查询 - 无数据，返回空列表")
	void getStockPage_ShouldReturnEmptyPage_WhenNoData() {
		StockQueryDTO query = new StockQueryDTO();
		query.setPageNum(1);
		query.setPageSize(10);

		Page<Stock> emptyStockPage = new Page<>(1, 10);
		emptyStockPage.setRecords(List.of());
		emptyStockPage.setTotal(0);

		// 关键修复：PageVO 的 records 必须初始化为空列表，不能是 null
		PageVO<StockVO> emptyPageVO = new PageVO<>();
		emptyPageVO.setRecords(new ArrayList<>());  // ← 关键：设为空列表而非 null
		emptyPageVO.setTotal(0L);
		emptyPageVO.setCurrent(1L);
		emptyPageVO.setSize(10L);

		when(stockService.getStockPage(query)).thenReturn(emptyStockPage);
		when(stockConvertMapper.toPageVO(emptyStockPage)).thenReturn(emptyPageVO);

		PageVO<StockVO> result = validationService.getStockPage(query);

		assertNotNull(result);
		assertTrue(result.getRecords().isEmpty());
		assertEquals(0L, result.getTotal());
	}

	// ==================== 辅助方法 ====================

	private Stock buildStock() {
		Stock stock = new Stock();
		stock.setId(1L);
		stock.setProductId(PRODUCT_ID);
		stock.setProductName("测试商品");
		stock.setStock(100);
		stock.setPrice(new BigDecimal("99.99"));
		return stock;
	}

	private StockVO buildStockVO() {
		StockVO vo = new StockVO();
		vo.setProductId(PRODUCT_ID);
		vo.setProductName("测试商品");
		vo.setStock(100);
		vo.setPrice(new BigDecimal("99.99"));
		return vo;
	}
}

