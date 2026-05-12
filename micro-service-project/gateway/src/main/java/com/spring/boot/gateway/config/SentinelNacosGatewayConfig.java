package com.spring.boot.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/11 11:40
 */
@Configuration
public class SentinelNacosGatewayConfig {

	@Value("${spring.cloud.nacos.config.server-addr:${spring.cloud.nacos.discovery.server-addr}}")
	private String nacosServerAddr;

	@Value("${spring.application.name}")
	private String applicationName;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Data
	private static class RuleWrapper {
		private Set<GatewayFlowRule> rules;
	}

	@PostConstruct
	public void init() {
		String dataId = applicationName + "-gateway-flow-rules";
		String group = "SENTINEL_GROUP";

		ReadableDataSource<String, Set<GatewayFlowRule>> dataSource =
				new NacosDataSource<>(
						nacosServerAddr,
						group,
						dataId,
						source -> {
							try {
								RuleWrapper wrapper = objectMapper.readValue(source, new TypeReference<RuleWrapper>() {});
								return wrapper.getRules();
							} catch (Exception e) {
								throw new RuntimeException("解析网关流控规则失败", e);
							}
						}
				);

		GatewayRuleManager.register2Property(dataSource.getProperty());
	}
}
