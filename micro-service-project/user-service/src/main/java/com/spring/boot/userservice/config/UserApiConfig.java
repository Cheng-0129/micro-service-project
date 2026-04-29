package com.spring.boot.userservice.config;

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
 * @datetime 2026/4/29 11:11
 */
@Configuration
public class UserApiConfig {

	@Bean
	public OpenAPI userOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("用户服务 API 文档")  // ← 自己的标题
						.description("用户管理相关接口")
						.version("v1.0.0")
						.contact(new Contact().name("Chi Shoucheng")));
	}

	@Bean
	public GroupedOpenApi userApi() {
		return GroupedOpenApi.builder()
				.group("用户模块，提供用户的增删改查、分页查询功能")
				.pathsToMatch("/user/**")
				.packagesToScan("com.spring.boot.userservice.controller")
				.build();
	}
}
