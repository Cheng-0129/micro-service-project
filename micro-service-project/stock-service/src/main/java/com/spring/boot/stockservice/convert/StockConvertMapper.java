package com.spring.boot.stockservice.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.stockservice.dto.StockCreateDTO;
import com.spring.boot.stockservice.dto.StockUpdateDTO;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.vo.StockVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/28 10:12
 */
@Mapper(componentModel = "spring")
public interface StockConvertMapper {

	void fillStock(StockCreateDTO VO, @MappingTarget Stock stock);
	void fillStock(StockUpdateDTO VO, @MappingTarget Stock stock);
	void fillStockVO(Stock stock, @MappingTarget StockVO VO);

	default Stock toEntity(StockCreateDTO VO) {
		if (VO == null) return null;
		Stock stock = new Stock();
		fillStock(VO, stock);
		return stock;
	}
	default Stock toEntity(StockUpdateDTO VO) {
		if (VO == null) return null;
		Stock stock = new Stock();
		fillStock(VO, stock);
		return stock;
	}
	default StockVO toStockVO(Stock stock) {
		if (stock == null) return null;
		StockVO VO = new StockVO();
		fillStockVO(stock, VO);
		return VO;
	}

	default List<StockVO> toVOList(List<Stock> stockList) {
		if (stockList == null) return null;
		return stockList.stream().map(this::toStockVO).collect(Collectors.toList());
	}
	default PageVO<StockVO> toPageVO(IPage<Stock> stockPage) {
		if (stockPage == null) return null;
		PageVO<StockVO> pageVO = new PageVO<>();
		pageVO.setCurrent(stockPage.getCurrent());
		pageVO.setSize(stockPage.getSize());
		pageVO.setTotal(stockPage.getTotal());
		pageVO.setPages(stockPage.getPages());
		pageVO.setRecords(toVOList(stockPage.getRecords()));
		return pageVO;
	}
}
