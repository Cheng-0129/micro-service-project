package com.spring.boot.userservice;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.spring.boot.userservice", "com.spring.boot.commonweb"})
@MapperScan("com.spring.boot.userservice.mapper")
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(UserServiceApplication.class);
		app.addListeners(new ApplicationListener<ApplicationReadyEvent>() {
			@Override
			public void onApplicationEvent(ApplicationReadyEvent event) {
				StringRedisTemplate redisTemplate = event.getApplicationContext()
						.getBean(StringRedisTemplate.class);
				try {
					redisTemplate.hasKey("health:check");
					log.info("✅ Redis 连接正常");
				} catch (Exception e) {
					log.error("❌ Redis 连接失败", e);
				}
			}
		});
		app.run(args);
	}
}