package com.spring.boot.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = OrderServiceApplication.class,
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
class OrderServiceImplApplicationTests {

	@Test
	void contextLoads() {
	}

}
