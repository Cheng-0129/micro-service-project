package com.spring.boot.orderservice.feign;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.orderservice.vo.feign.StockAddBackFeignVO;
import com.spring.boot.orderservice.vo.feign.StockDeductFeignVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/30 14:40
 */
@FeignClient(name = "stock-service", url = "http://127.0.0.1:8082")
public interface StockClient {

	@PostMapping("/stock/deduct")
	Result<StockDeductFeignVO> deductStock(@RequestParam("productId") Long productId, @RequestParam("num") Integer num);

	@PostMapping("/stock/addBack")
	Result<StockAddBackFeignVO> addBackStock(@RequestParam("productId") Long productId, @RequestParam("num") Integer num);
}
