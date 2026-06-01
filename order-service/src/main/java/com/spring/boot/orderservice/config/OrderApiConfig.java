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
						.title("订单服务")
						.description("提供订单创建（含库存扣减）、取消（含库存回滚）、查询、删除功能，集成 Seata 分布式事务与 Sentinel 熔断降级" +
								"\n\n---\n" +
								"\n\n邮箱：1017191272@qq.com")
						.version("v2.0.0")
						.contact(new Contact().name("池守城")));
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
