package com.spring.boot.stockservice.config;

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
public class StockApiConfig {

	@Bean
	public OpenAPI stockOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("库存服务")
						.description("提供库存的增删改查、分页查询、扣减库存、回滚库存功能，集成 Redis 缓存与 Sentinel 熔断降级" +
								"\n\n---\n" +
								"\n\n邮箱：1017191272@qq.com")
						.version("v2.0.0")
						.contact(new Contact().name("池守城")));
	}

	@Bean
	public GroupedOpenApi stockApi() {
		return GroupedOpenApi.builder()
				.group("库存模块")
				.pathsToMatch("/stock/**")
				.packagesToScan("com.spring.boot.stockservice.controller")
				.build();
	}
}
