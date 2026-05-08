package com.spring.boot.orderservice.convert;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.spring.boot.commoncore.vo.PageVO;
import com.spring.boot.orderservice.dto.OrderCreateDTO;
import com.spring.boot.orderservice.entity.Order;
import com.spring.boot.orderservice.vo.OrderAddBackVO;
import com.spring.boot.orderservice.vo.OrderCreateVO;
import com.spring.boot.orderservice.vo.OrderDetailVO;
import com.spring.boot.orderservice.vo.StockAddBackVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/7 08:57
 */
@Mapper(componentModel = "spring")
public interface OrderConvertMapper {

	void fillDetailVO(Order order, @MappingTarget OrderDetailVO vo);

	void fillCreateVO(Order order, @MappingTarget OrderCreateVO vo);

	void fillAddBackVO(StockAddBackVO stockAddBackVO, @MappingTarget OrderAddBackVO vo);

	void fillEntity(OrderCreateDTO dto, @MappingTarget Order order);

	default OrderDetailVO toOrderDetailVO(Order order) {
		if (order == null) {
			return null;
		}
		OrderDetailVO vo = new OrderDetailVO();
		fillDetailVO(order, vo);
		return vo;
	}

	default Order toEntity(OrderCreateDTO dto) {
		if (dto == null) {
			return null;
		}
		Order order = new Order();
		fillEntity(dto, order);
		return order;
	}

	default OrderCreateVO toOrderCreateVO(Order order) {
		if (order == null) {
			return null;
		}
		OrderCreateVO vo = new OrderCreateVO();
		fillCreateVO(order, vo);
		return vo;
	}

	default OrderAddBackVO toOrderAddBackVO(StockAddBackVO stockAddBackVO) {
		if (stockAddBackVO == null) {
			return null;
		}
		OrderAddBackVO vo = new OrderAddBackVO();
		fillAddBackVO(stockAddBackVO, vo);
		return vo;
	}

	default List<OrderDetailVO> toVOList(List<Order> orderList) {
		if (orderList == null) {
			return null;
		}
		return orderList.stream()
				.map(this::toOrderDetailVO)
				.collect(Collectors.toList());
	}

	default PageVO<OrderDetailVO> toPageVO(IPage<Order> orderPage) {
		if (orderPage == null) return null;

		PageVO<OrderDetailVO> pageVO = new PageVO<>();
		pageVO.setCurrent(orderPage.getCurrent());
		pageVO.setSize(orderPage.getSize());
		pageVO.setTotal(orderPage.getTotal());
		pageVO.setPages(orderPage.getPages());
		pageVO.setRecords(toVOList(orderPage.getRecords()));

		return pageVO;
	}
}
