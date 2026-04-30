package com.spring.boot.orderservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/29 11:07
 */
@Configuration
public class OrderApiConfig {

	@Bean
	public OpenAPI orderOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("订单服务 API 文档")
						.description("订单管理相关接口，提供增加订单扣除库存的功能")
						.version("v1.0.0")
						.contact(new Contact().name("Chi Shoucheng")));
	}

	@Bean
	public GroupedOpenApi orderApi() {
		return GroupedOpenApi.builder()
				.group("订单模块")
				.pathsToMatch("/order/**")
				.packagesToScan("com.spring.boot.orderservice.controller")
				.build();
	}
}
