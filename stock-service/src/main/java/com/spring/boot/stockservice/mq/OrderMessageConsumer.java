package com.spring.boot.stockservice.mq;

import com.spring.boot.stockservice.service.impl.StockServiceCacheImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/12 15:05
 */
@Slf4j
@Component
@RocketMQMessageListener(
		topic = "order-topic",
		consumerGroup = "stock-consumer-group",
		selectorExpression = "order-create || order-cancel"
)
public class OrderMessageConsumer implements RocketMQListener<String> {

	@Resource
	private StockServiceCacheImpl stockServiceCache;

	@Override
	public void onMessage(String message) {
		log.info("【库存模块】收到订单消息: {}", message);

		Long productId = extractValue(message, "productId");
		String type = extractStringValue(message, "type");

		if ("create".equals(type)) {
			stockServiceCache.evictCache(productId);
			log.info("【库存模块】订单创建，清除缓存成功: productId={}", productId);
		} else if ("cancel".equals(type)) {
			stockServiceCache.evictCache(productId);
			log.info("【库存模块】订单取消，清除缓存成功: productId={}", productId);
		}
	}

	private Long extractValue(String json, String key) {
		return Long.parseLong(json.split("\"" + key + "\":")[1].split(",")[0].trim());
	}

	private String extractStringValue(String json, String key) {
		return json.split("\"" + key + "\":\"")[1].split("\"")[0];
	}
}
