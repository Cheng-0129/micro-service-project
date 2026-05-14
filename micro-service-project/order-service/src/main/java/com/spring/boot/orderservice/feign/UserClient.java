package com.spring.boot.orderservice.feign;

import com.spring.boot.commoncore.result.Result;
import com.spring.boot.orderservice.vo.feign.UserFeignVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/14 09:14
 */
@FeignClient(name = "user-service", url = "http://127.0.0.1:8081")
public interface UserClient {

	@GetMapping("/user/get/{id}")
	Result<UserFeignVO> getById(@PathVariable("id") Long id);
}
