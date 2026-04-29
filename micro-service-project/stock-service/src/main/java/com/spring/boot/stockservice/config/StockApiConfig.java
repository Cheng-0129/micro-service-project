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
						.title("库存服务 API 文档")  // ← 自己的标题
						.description("库存管理相关接口，提供库存的增删改查功能")
						.version("v1.0.0")
						.contact(new Contact().name("Chi Shoucheng")));
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
