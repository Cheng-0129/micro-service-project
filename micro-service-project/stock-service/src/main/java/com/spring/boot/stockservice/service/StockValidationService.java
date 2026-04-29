package com.spring.boot.stockservice.service;

import com.spring.boot.commoncore.exception.BusinessException;
import com.spring.boot.stockservice.convert.StockConvertMapper;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.vo.StockCreateVO;
import com.spring.boot.stockservice.vo.StockUpdateVO;
import com.spring.boot.stockservice.vo.StockVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.spring.boot.commoncore.result.ResultCode.*;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/28 14:13
 */
@Slf4j
@Service
public class StockValidationService {

	@Autowired
	@Qualifier("stockServiceCache")
	private StockService stockService;

	@Resource
	private StockConvertMapper stockConvertMapper;

	public void addStock(StockCreateVO VO) {

		log.info("【校验层】开始新增库存，请求参数：stock={}", VO);

		Stock stock = stockConvertMapper.toEntity(VO);

		Stock created = stockService.addStock(stock);
		if(created == null) {
			log.error("【校验层】数据库新增失败，productId={}", created);
			throw BusinessException.of(STOCK_ADD_FAILED);
		}

		log.info("【校验层】新增成功，productId={}, productName={}",
				created.getProductId(), created.getProductName());
	}

	public StockVO getStockByProductId(Long productId) {

		log.debug("【校验层】开始查询库存，productId={}", productId);

		Stock stock = stockService.getStockByProductId(productId);

		if(stock == null) {
			log.warn("【校验层】库存不存在，productId={}", productId);
			throw BusinessException.of(STOCK_NOT_EXIST);
		}

		StockVO stockVO = stockConvertMapper.toVO(stock);

		log.info("【校验层】库存查询成功，productId={}, productName={}, stock={}, price={}",
				stockVO.getProductId(), stockVO.getProductName(), stockVO.getStock(), stockVO.getPrice());

		return stockVO;
	}

	public void updateStockByProductId(Long productId, StockUpdateVO stock) {

		log.debug("【校验层】开始更新库存，productId={}", productId);

		Stock existing = stockService.getStockByProductId(productId);

		if (existing == null) {
			log.warn("【校验层】更新失败，库存不存在，productId={}", productId);
			throw BusinessException.of(STOCK_NOT_EXIST);
		}

		Stock updated = stockConvertMapper.toEntity(stock);

		updated.setId(existing.getId());
		updated.setProductId(productId);

		log.debug("【校验层】查询到已有记录，dbId={}, productId={}", existing.getId(), productId);

		boolean success = stockService.updateStock(updated);
		if(!success) {
			log.error("【校验层】数据库更新失败，productId={}", updated.getProductId());
			throw BusinessException.of(STOCK_UPDATE_FAILED);
		}

		log.info("【校验层】库存更新成功，productId={}, productName={}, stock={}, price={}",
				updated.getProductId(), updated.getProductName(), updated.getStock(), updated.getPrice());
	}

	public void deleteStockByProductId(Long productId) {

		log.debug("【校验层】开始删除库存，productId={}", productId);

		Stock existing = stockService.getStockByProductId(productId);

		if (existing == null) {
			log.warn("【校验层】删除失败，库存不存在");
			throw BusinessException.of(STOCK_NOT_EXIST);
		}

		log.debug("【校验层】查询到已有记录，dbId={}, productId={}", existing.getId(), productId);

		boolean success = stockService.deleteStock(existing.getId(), productId);

		if(!success) {
			log.error("【校验层】数据删除失败，productId={}", productId);
			throw BusinessException.of(STOCK_DELETE_FAILED);
		}

		log.info("【校验层】库存删除成功");
	}
}
