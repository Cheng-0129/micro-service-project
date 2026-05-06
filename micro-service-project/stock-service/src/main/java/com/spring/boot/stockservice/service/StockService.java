package com.spring.boot.stockservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spring.boot.stockservice.dto.StockQueryDTO;
import com.spring.boot.stockservice.entity.Stock;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/9 14:35
 */
public interface StockService {

	Stock addStock(Stock stock);

	Stock getStockByProductId(Long productId);

	boolean updateStock(Stock stock);

	boolean deleteStock(Long id, Long productId);

	Integer deductStock(Long productId, Integer num);

	IPage<Stock> getStockPage(StockQueryDTO query);
}

