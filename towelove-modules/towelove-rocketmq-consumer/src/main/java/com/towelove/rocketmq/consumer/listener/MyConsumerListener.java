package com.towelove.rocketmq.consumer.listener;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author: 张锦标
 * @date: 2023/3/2 19:34
 * MyConsumer类
 */
@Component
@RocketMQMessageListener(
        consumerGroup = "towelove-consumer",
        topic = "${Towelove-Topic}",
        consumeMode = ConsumeMode.ORDERLY
)
public class MyConsumerListener implements RocketMQListener<String>{
    @Override
    public void onMessage(String message) {
        System.out.println("收到消息："+message);
    }
}