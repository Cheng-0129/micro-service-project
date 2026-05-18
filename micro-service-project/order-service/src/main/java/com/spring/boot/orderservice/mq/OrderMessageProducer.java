package com.spring.boot.orderservice.mq;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author Chi Shoucheng
 * @datetime 2026/5/12 15:05
 */
@Slf4j
@Component
public class OrderMessageProducer {

	@Resource
	private RocketMQTemplate rocketMQTemplate;

	private static final String TOPIC = "order-topic";
	private static final String TAG_CREATE = "order-create";
	private static final String TAG_CANCEL = "order-cancel";

	/**
	 * 异步发送订单创建消息
	 */
	public void sendOrderCreateMessage(Long productId) {
		if (rocketMQTemplate == null) {
			log.warn("【MQ】RocketMQ未启用，跳过发送订单创建消息: productId={}", productId);
			return;
		}
		String message = String.format("{\"productId\":%d,\"type\":\"create\"}", productId);
		String destination = TOPIC + ":" + TAG_CREATE;

		rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
			@Override
			public void onSuccess(SendResult sendResult) {
				log.info("【MQ】订单创建消息发送成功: productId={}, msgId={}", productId, sendResult.getMsgId());
			}
			@Override
			public void onException(Throwable e) {
				log.error("【MQ】订单创建消息发送失败: productId={}", productId, e);
			}
		});
	}

	/**
	 * 异步发送订单取消消息
	 */
	public void sendOrderCancelMessage(Long productId) {
		if (rocketMQTemplate == null) {
			log.warn("【MQ】RocketMQ未启用，跳过发送订单取消消息: productId={}", productId);
			return;
		}
		String message = String.format("{\"productId\":%d,\"type\":\"cancel\"}", productId);
		String destination = TOPIC + ":" + TAG_CANCEL;

		rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
			@Override
			public void onSuccess(SendResult sendResult) {
				log.info("【MQ】订单取消消息发送成功: productId={}, msgId={}", productId, sendResult.getMsgId());
			}
			@Override
			public void onException(Throwable e) {
				log.error("【MQ】订单取消消息发送失败: productId={}", productId, e);
			}
		});
	}

}
