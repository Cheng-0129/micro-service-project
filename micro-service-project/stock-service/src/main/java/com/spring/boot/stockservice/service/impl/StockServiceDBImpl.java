package com.spring.boot.stockservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.mapper.StockMapper;
import com.spring.boot.stockservice.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/9 14:37
 */
@Slf4j
@Service("stockServiceDB")
public class StockServiceDBImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

	@Resource
	StockMapper stockMapper;

	@Resource
	private JdbcTemplate jdbcTemplate;

	@Override
	public Stock addStock(Stock stock) {
		log.debug("【数据层】开始新增数据，productName={}, stock={}, price={}",
				stock.getProductName(), stock.getStock(), stock.getPrice());

		Long productId = jdbcTemplate.queryForObject(
				"UPDATE biz_id_counter SET current_max_id = current_max_id + 1 WHERE table_name = 't_stock' RETURNING current_max_id",
				Long.class
		);

		stock.setProductId(productId);

		boolean success = this.save(stock);

		if (success) {
			log.debug("【数据层】数据库新增成功，id={}, productId={}", stock.getId(), productId);
			return stock;
		} else {
			log.error("【数据层】数据库新增失败");
			return null;
		}
	}

	@Override
	public Stock getStockByProductId(Long productId) {
		log.debug("【数据层】查询数据库，productId={}", productId);
		Stock stock = this.stockMapper.selectByProductId(productId);
		if (stock != null) {
			log.debug("【数据层】数据库查询成功，productId={}, productName={}", productId, stock.getProductName());
		} else {
			log.debug("【数据层】数据库查询结果为空，productId={}", productId);
		}
		return stock;
	}

	@Override
	public boolean updateStock(Stock stock) {
		log.debug("【数据层】更新数据库，id={}, productId={}, productName={}, stock={}, price={}",
				stock.getId(), stock.getProductId(), stock.getProductName(), stock.getStock(), stock.getPrice());
		boolean result = this.updateById(stock);
		if (result) {
			log.debug("【数据层】数据库更新成功，id={}", stock.getId());
		} else {
			log.error("【数据层】数据库更新失败，id={}", stock.getId());
		}
		return result;
	}

	@Override
	public boolean deleteStock(Long id, Long productId) {
		log.debug("【数据层】开始删除数据，id={}", id);
		boolean result = this.stockMapper.deleteById(id) > 0;
		if (result) {
			log.debug("【数据层】数据库删除成功，id={}", id);
		} else {
			log.error("【数据层】数据库删除失败，id={}", id);
		}
		return result;
	}

	@Override
	public Integer deductStock(Long productId, Integer num) {
		log.debug("【数据层】开始扣减库存，productId={}, num={}", productId, num);
		Integer stockAfter = this.stockMapper.deductStock(productId, num);
		if (stockAfter != null) {
			log.debug("【数据层】扣减库存成功，剩余库存={}", stockAfter);
		} else {
			log.error("【数据层】扣减库存失败，productId={}, num={}", productId, num);
		}
		return stockAfter;
	}
}
