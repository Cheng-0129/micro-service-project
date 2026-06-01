package com.spring.boot.stockservice;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.nacos.config.enabled=false",
		"spring.cloud.nacos.discovery.enabled=false"
})
@EnableAutoConfiguration(exclude = RocketMQAutoConfiguration.class)
class StockServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
