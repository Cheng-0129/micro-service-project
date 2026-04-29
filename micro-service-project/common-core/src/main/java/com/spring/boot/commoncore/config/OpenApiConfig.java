package com.spring.boot.commoncore.config;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/17 10:25
 */
/**
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("micro-service项目文档")
						.description("微服务系统后端API接口文档")
						.version("v2.0.0")
						.contact(new Contact().name("Chi Shoucheng")));
	}

}
 */
