package com.jgc.helloworld.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 队列配置
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Queue queue(){
        return new Queue("hello");
    }
}
