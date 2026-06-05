package com.spring.boot.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = UserServiceApplication.class,
		properties = {
				"spring.autoconfigure.exclude=" +
						"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
						"com.alibaba.cloud.nacos.NacosConfigAutoConfiguration," +
						"com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientAutoConfiguration," +
						"com.alibaba.cloud.sentinel.SentinelWebAutoConfiguration," +
						"io.seata.spring.boot.autoconfigure.SeataAutoConfiguration"
		}
)
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
