package com.spring.boot.commonweb.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/4/23 16:07
 */
@Configuration
public class MyBatisPlusConfig {

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

		PaginationInnerInterceptor paginationInnerInterceptor =
				new PaginationInnerInterceptor(DbType.POSTGRE_SQL);

		paginationInnerInterceptor.setOverflow(true);

		paginationInnerInterceptor.setMaxLimit(1000L);

		interceptor.addInnerInterceptor(paginationInnerInterceptor);
		return interceptor;
	}
}
