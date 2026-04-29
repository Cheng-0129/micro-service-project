package com.spring.boot.stockservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/28 11:07
 */
@Slf4j
@Service("stockServiceCache")
public class StockServiceCacheImpl implements StockService{

	@Autowired
	@Qualifier("stockServiceDB")
	private StockService stockServiceDB;

	@Resource(name = "stringRedisTemplate")
	private StringRedisTemplate redisTemplate;

	// Lua脚本：原子性释放锁
	private static final String UNLOCK_SCRIPT =
			"if redis.call('get', KEYS[1]) == ARGV[1] then " +
					"    return redis.call('del', KEYS[1]) " +
					"else " +
					"    return 0 " +
					"end";

	// 空值缓存标记
	private static final String EMPTY_CACHE = "NULL";

	// 缓存过期时间基础值（秒）
	private static final long CACHE_EXPIRE_BASE = 30 * 60;  // 30分钟
	private static final long EMPTY_CACHE_EXPIRE_BASE = 5 * 60;  // 5分钟

	// 随机过期时间范围（秒）
	private static final int CACHE_EXPIRE_RANDOM = 300;  // 0-5分钟随机
	private static final int EMPTY_CACHE_EXPIRE_RANDOM = 60;  // 0-1分钟随机

	@Override
	public Stock addStock(Stock stock) {

		log.debug("【缓存层】开始新增库存，请求参数：stock={}", stock);

		Stock result = stockServiceDB.addStock(stock);

		log.debug("【缓存层】新增库存，productId={}", result != null ? result.getProductId() : null);

		return result;
	}

	@Override
	public Stock getStockByProductId(Long productId) {

		String cacheKey = "product:" + productId;
		log.debug("【缓存层】开始查询缓存，key={}", cacheKey);

		// 1. 尝试从缓存获取
		String json = redisTemplate.opsForValue().get(cacheKey);

		if (json != null) {
			// 如果是空值标记，返回null
			if (EMPTY_CACHE.equals(json)) {
				log.debug("【缓存层】命中空缓存，key={}", cacheKey);
				return null;
			}
			log.debug("【缓存层】命中缓存，key={}", cacheKey);
			return JSON.parseObject(json, Stock.class);
		}

		log.debug("【缓存层】缓存未命中，准备加锁查询数据库，key={}", cacheKey);

		// 2. 缓存未命中，使用分布式锁防止缓存击穿
		String lockKey = "lock:product:" + productId;
		String lockValue = UUID.randomUUID().toString();

		try {
			// 尝试获取锁，最多等待3次
			for (int i = 0; i < 3; i++) {
				if (tryLock(lockKey, lockValue, 10)) {
					log.debug("【缓存层】获取锁成功，lockKey={}, 第{}次尝试", lockKey, i + 1);
					try {
						// 双重检查，可能其他线程已经加载了缓存
						json = redisTemplate.opsForValue().get(cacheKey);
						if (json != null) {
							log.debug("【缓存层】双重检查命中缓存，key={}", cacheKey);
							if (EMPTY_CACHE.equals(json)) {
								return null;
							}
							return JSON.parseObject(json, Stock.class);
						}

						log.debug("【缓存层】缓存仍为空，查询数据库，productId={}", productId);
						// 从数据库加载数据
						Stock stock = stockServiceDB.getStockByProductId(productId);

						// 写入缓存，包括空值
						setCacheWithRandomExpire(cacheKey, stock);

						if (stock != null) {
							log.debug("【缓存层】数据库查询成功，已写入缓存，productId={}", productId);
						} else {
							log.debug("【缓存层】数据库查询为空，已写入空缓存，productId={}", productId);
						}

						return stock;
					} finally {
						unlock(lockKey, lockValue);
					}
				}
				log.debug("【缓存层】获取锁失败，第{}次尝试，等待重试", i + 1);
				// 获取锁失败，短暂等待后重试
				Thread.sleep(50);
			}

			// 重试失败，直接查数据库（降级方案）
			log.warn("【缓存层】获取锁失败，3次重试后降级查询数据库，productId={}", productId);
			return stockServiceDB.getStockByProductId(productId);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("【缓存层】线程中断，productId={}", productId, e);
			return null;
		}
	}

	@Override
	public boolean updateStock(Stock stock) {

		log.debug("【缓存层】开始更新库存，id={}, productId={}", stock.getId(), stock.getProductId());
		// 更新数据库
		boolean success = stockServiceDB.updateStock(stock);

		if (success) {
			evictCache(stock.getProductId());
			log.info("【缓存层】数据库更新成功，已清除缓存，id={}, productId={}", stock.getId(), stock.getProductId());
		} else {
			log.error("【缓存层】数据库更新失败，id={}, productId={}", stock.getId(), stock.getProductId());
		}

		return success;
	}

	@Override
	public boolean deleteStock(Long id, Long productId) {

		log.debug("【缓存层】开始删除库存，id={}", id);

		boolean success = stockServiceDB.deleteStock(id, productId);

		if (success) {
			evictCache(productId);
			log.info("【缓存层】数据库删除成功，已清除缓存");
		} else {
			log.error("【缓存层】数据库删除失败");
		}

		return success;
	}

	private void setCacheWithRandomExpire(String key, Stock stock) {
		if (stock != null) {
			// 正常数据缓存30分钟 + 随机0-5分钟
			long expire = CACHE_EXPIRE_BASE + new Random().nextInt(CACHE_EXPIRE_RANDOM + 1);
			redisTemplate.opsForValue()
					.set(key, JSON.toJSONString(stock), expire, TimeUnit.SECONDS);
			log.debug("【缓存层】设置缓存，key={}, 过期时间={}秒", key, expire);
		} else {
			// 空值缓存5分钟 + 随机0-1分钟
			long expire = EMPTY_CACHE_EXPIRE_BASE + new Random().nextInt(EMPTY_CACHE_EXPIRE_RANDOM + 1);
			redisTemplate.opsForValue()
					.set(key, EMPTY_CACHE, expire, TimeUnit.SECONDS);
			log.debug("【缓存层】设置空缓存，key={}, 过期时间={}秒", key, expire);
		}
	}

	private boolean tryLock(String key, String value, int timeout) {
		Boolean success = redisTemplate.opsForValue().
				setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
		boolean result = Boolean.TRUE.equals(success);
		if (result) {
			log.debug("【缓存层】加锁成功，key={}", key);
		}
		return result;
	}

	private void unlock(String key, String value) {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setScriptText(UNLOCK_SCRIPT);
		script.setResultType(Long.class);

		Long result = redisTemplate.execute(script, Collections.singletonList(key), value);
		if (result != null && result == 1) {
			log.debug("【缓存层】释放锁成功，key={}", key);
		} else {
			log.warn("【缓存层】释放锁失败，锁可能已过期，key={}", key);
		}
	}

	private void evictCache(Long id) {
		String cacheKey = "product:" + id;
		redisTemplate.delete(cacheKey);
		log.debug("【缓存层】已清除缓存，key={}", cacheKey);
	}

}
