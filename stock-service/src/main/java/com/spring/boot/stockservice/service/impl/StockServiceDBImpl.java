package com.spring.boot.stockservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spring.boot.commonweb.component.IdGenerator;
import com.spring.boot.stockservice.dto.StockQueryDTO;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.mapper.StockMapper;
import com.spring.boot.stockservice.service.StockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

	@Autowired
	@Qualifier("stockIdGenerator")
	private IdGenerator stockIdGenerator;

	@Override
	public Stock addStock(Stock stock) {
		log.debug("【数据层】开始新增数据，productName={}, stock={}, price={}",
				stock.getProductName(), stock.getStock(), stock.getPrice());

		Long productId = stockIdGenerator.nextId();

		stock.setProductId(productId);

		boolean success = this.save(stock);

		if (success) {
			log.debug("【数据层】数据库新增成功，productId={}", productId);
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

	@Override
	public IPage<Stock> getStockPage(StockQueryDTO query) {

		long start = System.currentTimeMillis();
		log.info("【数据层】开始分页查询库存，参数：{}", query);

		Page<Stock> page = new Page<>(query.getPageNum(), query.getPageSize());
		IPage<Stock> stockPage = stockMapper.selectStockPage(page, query);

		long cost = System.currentTimeMillis() - start;

		if (stockPage.getRecords().isEmpty()) {
			log.info("【数据层】分页查询无匹配库存数据，参数：{}，耗时：{}ms", query, cost);
		} else {
			log.info("【数据层】分页查询成功，命中{}条，总{}条，耗时：{}ms",
					stockPage.getRecords().size(), stockPage.getTotal(), cost);
		}
		return stockPage;
	}

	@Override
	public Integer addBackStock(Long productId, Integer num) {
		log.debug("【数据层】开始回滚库存，productId={}, num={}", productId, num);
		Integer stockAfter = this.stockMapper.addBackStock(productId, num);
		log.debug("【数据层】回滚库存成功，剩余库存={}", stockAfter);
		return stockAfter;
	}
}
