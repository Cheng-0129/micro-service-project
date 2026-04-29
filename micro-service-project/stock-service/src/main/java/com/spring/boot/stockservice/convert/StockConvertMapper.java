package com.spring.boot.stockservice.convert;

import com.spring.boot.stockservice.entity.Stock;
import com.spring.boot.stockservice.vo.StockCreateVO;
import com.spring.boot.stockservice.vo.StockUpdateVO;
import com.spring.boot.stockservice.vo.StockVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
	Stock toEntity(StockCreateVO VO);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "productId", ignore = true)
	@Mapping(target = "createTime", ignore = true)
	@Mapping(target = "updateTime", ignore = true)
	Stock toEntity(StockUpdateVO VO);

	StockVO toVO(Stock stock);

}
