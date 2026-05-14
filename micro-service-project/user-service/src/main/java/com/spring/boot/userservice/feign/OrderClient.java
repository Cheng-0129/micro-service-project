package com.spring.boot.userservice.feign;

import com.spring.boot.commoncore.constant.FeignHeaders;
import com.spring.boot.commoncore.result.Result;
import com.spring.boot.userservice.dto.feign.OrderCreateFeignDTO;
import com.spring.boot.userservice.vo.feign.OrderFeignVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/6 08:32
 */
@FeignClient(name = "order-service", url = "http://127.0.0.1:8083")
public interface OrderClient {

	@PostMapping("/order/create")
	Result<OrderFeignVO> createOrder(@RequestBody OrderCreateFeignDTO order, @RequestHeader(FeignHeaders.SOURCE) String source);
}
