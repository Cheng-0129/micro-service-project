package com.spring.boot.stockservice.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.dto.StockCreateDTO;
import com.spring.boot.stockservice.dto.StockUpdateDTO;
import com.spring.boot.stockservice.vo.StockVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/28 10:12
 */
@Mapper(componentModel = "spring")
public interface StockConvertMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productId", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	Stock toEntity(StockCreateDTO VO);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productId", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	Stock toEntity(StockUpdateDTO VO);

	StockVO toVO(Stock stock);


	List<StockVO> toVOList(List<Stock> stockList);
	PageVO<StockVO> toPageVO(IPage<Stock> stockPage);
}
