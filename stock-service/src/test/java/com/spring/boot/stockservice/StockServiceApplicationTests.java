package com.spring.boot.stockservice;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = StockServiceApplication.class,
		properties = {
				"spring.autoconfigure.exclude=" +
						"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
						"com.alibaba.cloud.nacos.NacosConfigAutoConfiguration," +
						"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientAutoConfiguration," +
						"com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration," +
						"io.seata.spring.boot.autoconfigure.SeataAutoConfiguration," +
						"org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
		}
)
class StockServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
