package com.jgc.mq.component;

import cn.hutool.core.lang.Console;
import com.jgc.mq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

//@Component
public class Consumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeMessage(String message) {
        Console.log("Consume message {}", message);
    }
}
