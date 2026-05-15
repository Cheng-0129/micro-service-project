package com.spring.boot.commonweb.config;

import com.spring.boot.commonweb.component.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/15 09:46
 */
@Configuration
public class IdGeneratorConfig {

	/**
	 * 上线使用可选步长依次为100，500，20
	 * 开发测试，暂选用10，50，2
	*/
	@Bean
	public IdGenerator userIdGenerator(JdbcTemplate jdbcTemplate) {
		return new IdGenerator(jdbcTemplate, "t_user", 10);
	}

	@Bean
	public IdGenerator orderIdGenerator(JdbcTemplate jdbcTemplate) {
		return new IdGenerator(jdbcTemplate, "t_order", 50);
	}

	@Bean
	public IdGenerator stockIdGenerator(JdbcTemplate jdbcTemplate) {
		return new IdGenerator(jdbcTemplate, "t_stock", 2);
	}
}
