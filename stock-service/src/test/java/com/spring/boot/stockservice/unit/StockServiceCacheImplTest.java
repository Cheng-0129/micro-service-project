package com.spring.boot.stockservice.unit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spring.boot.stockservice.dto.StockQueryDTO;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.service.impl.StockServiceCacheImpl;
import com.spring.boot.stockservice.service.impl.StockServiceDBImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/6/1 08:58
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockServiceCacheImpl 单元测试")
class StockServiceCacheImplTest {

	@Mock
	private StockServiceDBImpl stockServiceDB;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private StockServiceCacheImpl stockServiceCache;

	private static final Long PRODUCT_ID = 1L;
	private static final String CACHE_KEY = "product:" + PRODUCT_ID;
	private static final String LOCK_KEY = "lock:product:" + PRODUCT_ID;

	@BeforeEach
	void setUp() {
		// 所有测试通用：opsForValue() 返回 mock 的 ValueOperations
		lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	// ==================== getStockByProductId 测试 ====================

	@Test
	@DisplayName("查询库存 - 缓存命中，直接返回")
	void getStockByProductId_ShouldReturnFromCache_WhenCacheHit() {
		Stock mockStock = buildStock();
		String cachedJson = "{\"id\":1,\"productId\":1,\"productName\":\"测试商品\",\"stock\":100,\"price\":99.99}";

		when(valueOperations.get(CACHE_KEY)).thenReturn(cachedJson);

		Stock result = stockServiceCache.getStockByProductId(PRODUCT_ID);

		assertNotNull(result);
		assertEquals(PRODUCT_ID, result.getProductId());
		// 验证没有查数据库
		verify(stockServiceDB, never()).getStockByProductId(anyLong());
	}

	@Test
	@DisplayName("查询库存 - 缓存命中空值标记，返回 null")
	void getStockByProductId_ShouldReturnNull_WhenEmptyCacheHit() {
		when(valueOperations.get(CACHE_KEY)).thenReturn("NULL");

		Stock result = stockServiceCache.getStockByProductId(PRODUCT_ID);

		assertNull(result);
		verify(stockServiceDB, never()).getStockByProductId(anyLong());
	}

	@Test
	@DisplayName("查询库存 - 缓存未命中，加锁查库并回写缓存")
	void getStockByProductId_ShouldQueryDbAndWriteCache_WhenCacheMiss() {
		Stock dbStock = buildStock();

		// 第一次 get(CACHE_KEY) 返回 null（缓存未命中）
		// 获取锁后双重检查 get(CACHE_KEY) 也返回 null
		when(valueOperations.get(CACHE_KEY)).thenReturn(null);

		// 成功获取锁
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
				.thenReturn(true);

		// DB 有数据
		when(stockServiceDB.getStockByProductId(PRODUCT_ID)).thenReturn(dbStock);

		// Mock Lua 脚本执行解锁，返回 1 表示成功
		when(redisTemplate.execute(any(DefaultRedisScript.class),
				eq(Collections.singletonList(LOCK_KEY)), anyString()))
				.thenReturn(1L);

		Stock result = stockServiceCache.getStockByProductId(PRODUCT_ID);

		assertNotNull(result);
		assertEquals(PRODUCT_ID, result.getProductId());
		assertEquals("测试商品", result.getProductName());

		// 验证数据库被查询了一次
		verify(stockServiceDB).getStockByProductId(PRODUCT_ID);
		// 验证缓存被写入（set 方法被调用）
		verify(valueOperations).set(eq(CACHE_KEY), anyString(), anyLong(), eq(TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("查询库存 - 加锁后双重检查命中缓存，不查库")
	void getStockByProductId_ShouldUseDoubleCheckCache_WhenLockAcquired() {
		Stock dbStock = buildStock();
		String cachedJson = "{\"id\":1,\"productId\":1,\"productName\":\"测试商品\",\"stock\":100,\"price\":99.99}";

		// 第一次 get 返回 null（缓存未命中，触发加锁）
		when(valueOperations.get(CACHE_KEY)).thenReturn(null)
				// 加锁后双重检查，此时缓存已被其他线程写入
				.thenReturn(cachedJson);

		// 成功获取锁
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
				.thenReturn(true);

		// Mock 解锁
		when(redisTemplate.execute(any(DefaultRedisScript.class),
				eq(Collections.singletonList(LOCK_KEY)), anyString()))
				.thenReturn(1L);

		Stock result = stockServiceCache.getStockByProductId(PRODUCT_ID);

		assertNotNull(result);
		assertEquals(PRODUCT_ID, result.getProductId());

		// 双重检查命中缓存后，不应再查数据库
		verify(stockServiceDB, never()).getStockByProductId(anyLong());
		// 也不应再写缓存
		verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
	}

	@Test
	@DisplayName("查询库存 - 数据库返回 null，写入空缓存")
	void getStockByProductId_ShouldWriteEmptyCache_WhenDbReturnsNull() {
		// 缓存未命中
		when(valueOperations.get(CACHE_KEY)).thenReturn(null);
		// 成功获取锁
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
				.thenReturn(true);
		// DB 也无数据
		when(stockServiceDB.getStockByProductId(PRODUCT_ID)).thenReturn(null);
		// 解锁
		when(redisTemplate.execute(any(DefaultRedisScript.class),
				eq(Collections.singletonList(LOCK_KEY)), anyString()))
				.thenReturn(1L);

		Stock result = stockServiceCache.getStockByProductId(PRODUCT_ID);

		assertNull(result);
		// 验证写入了空缓存标记 "NULL"
		verify(valueOperations).set(eq(CACHE_KEY), eq("NULL"), anyLong(), eq(TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("查询库存 - 获取锁失败，降级直接查库")
	void getStockByProductId_ShouldFallbackToDb_WhenLockAcquireFailed() {
		// 缓存未命中
		when(valueOperations.get(CACHE_KEY)).thenReturn(null);
		// 三次尝试获取锁都失败
		when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
				.thenReturn(false);

		// 降级查库
		when(stockServiceDB.getStockByProductId(PRODUCT_ID)).thenReturn(buildStock());

		Stock result = stockServiceCache.getStockByProductId(PRODUCT_ID);

		assertNotNull(result);
		// 验证重试了 3 次
		verify(valueOperations, times(3))
				.setIfAbsent(eq(LOCK_KEY), anyString(), eq(10L), eq(TimeUnit.SECONDS));
		// 验证降级查库
		verify(stockServiceDB).getStockByProductId(PRODUCT_ID);
	}

	// ==================== deductStock 测试 ====================

	@Test
	@DisplayName("扣减库存 - 成功扣减，返回剩余库存")
	void deductStock_ShouldReturnRemainingStock_WhenSuccess() {
		when(stockServiceDB.deductStock(PRODUCT_ID, 10)).thenReturn(90);

		Integer remaining = stockServiceCache.deductStock(PRODUCT_ID, 10);

		assertEquals(90, remaining);
		verify(stockServiceDB).deductStock(PRODUCT_ID, 10);
	}

	@Test
	@DisplayName("扣减库存 - DB 返回 null（库存不足）")
	void deductStock_ShouldReturnNull_WhenDbReturnsNull() {
		when(stockServiceDB.deductStock(PRODUCT_ID, 999)).thenReturn(null);

		Integer remaining = stockServiceCache.deductStock(PRODUCT_ID, 999);

		assertNull(remaining);
	}

	// ==================== addBackStock 测试 ====================

	@Test
	@DisplayName("回滚库存 - 成功回滚，返回新库存")
	void addBackStock_ShouldReturnNewStock_WhenSuccess() {
		when(stockServiceDB.addBackStock(PRODUCT_ID, 10)).thenReturn(110);

		Integer remaining = stockServiceCache.addBackStock(PRODUCT_ID, 10);

		assertEquals(110, remaining);
	}

	// ==================== updateStock 测试 ====================

	@Test
	@DisplayName("更新库存 - 成功更新后清除缓存")
	void updateStock_ShouldEvictCache_WhenUpdateSuccess() {
		Stock stock = buildStock();
		when(stockServiceDB.updateStock(stock)).thenReturn(true);

		boolean result = stockServiceCache.updateStock(stock);

		assertTrue(result);
		verify(redisTemplate).delete(CACHE_KEY);
	}

	@Test
	@DisplayName("更新库存 - 更新失败不清除缓存")
	void updateStock_ShouldNotEvictCache_WhenUpdateFailed() {
		Stock stock = buildStock();
		when(stockServiceDB.updateStock(stock)).thenReturn(false);

		boolean result = stockServiceCache.updateStock(stock);

		assertFalse(result);
		verify(redisTemplate, never()).delete(anyString());
	}

	// ==================== deleteStock 测试 ====================

	@Test
	@DisplayName("删除库存 - 成功删除后清除缓存")
	void deleteStock_ShouldEvictCache_WhenDeleteSuccess() {
		when(stockServiceDB.deleteStock(1L, PRODUCT_ID)).thenReturn(true);

		boolean result = stockServiceCache.deleteStock(1L, PRODUCT_ID);

		assertTrue(result);
		verify(redisTemplate).delete(CACHE_KEY);
	}

	@Test
	@DisplayName("删除库存 - 删除失败不清除缓存")
	void deleteStock_ShouldNotEvictCache_WhenDeleteFailed() {
		when(stockServiceDB.deleteStock(1L, PRODUCT_ID)).thenReturn(false);

		boolean result = stockServiceCache.deleteStock(1L, PRODUCT_ID);

		assertFalse(result);
		verify(redisTemplate, never()).delete(anyString());
	}

	// ==================== addStock 测试 ====================

	@Test
	@DisplayName("新增库存 - 直接委托给 DB 层")
	void addStock_ShouldDelegateToDb() {
		Stock stock = buildStock();
		when(stockServiceDB.addStock(stock)).thenReturn(stock);

		Stock result = stockServiceCache.addStock(stock);

		assertNotNull(result);
		verify(stockServiceDB).addStock(stock);
	}

	// ==================== getStockPage 测试 ====================

	@Test
	@DisplayName("分页查询 - 不缓存，直接委托 DB 层")
	void getStockPage_ShouldDelegateToDb() {
		com.spring.boot.stockservice.dto.StockQueryDTO query =
				new StockQueryDTO();
		com.baomidou.mybatisplus.core.metadata.IPage<Stock> mockPage =
				new Page<>(1, 10);
		when(stockServiceDB.getStockPage(query)).thenReturn(mockPage);

		var result = stockServiceCache.getStockPage(query);

		assertNotNull(result);
		verify(stockServiceDB).getStockPage(query);
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
}
