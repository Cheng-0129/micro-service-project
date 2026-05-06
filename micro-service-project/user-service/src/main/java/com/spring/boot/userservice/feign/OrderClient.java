package com.spring.boot.userservice.feign;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.userservice.dto.OrderCreateDTO;
import com.spring.boot.userservice.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/6 08:32
 */
@FeignClient(name = "order-service", url = "http://127.0.0.1:8083")
public interface OrderClient {

	@PostMapping("/order/create")
	Result<OrderVO> createOrder(@RequestBody @Valid OrderCreateDTO order);
}
