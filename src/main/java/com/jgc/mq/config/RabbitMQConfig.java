package com.jgc.mq.config;

import cn.hutool.core.lang.Console;
import com.jgc.mq.component.ConsumerByHands;
import com.jgc.mq.entity.RabbitMetaMesage;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Configuration
public class RabbitMQConfig {

    public final static String QUEUE_NAME = "spring-boot-quque";

    public final static String EXCHANGE_NAME = "spring-boot-exchange";

    public final static String ROUTING_KEY = "spring-boot-key";

    @Resource
    private RedisTemplate redisTemplate;

    //创建队列
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }


    //创建一个topic类型交换器
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }


    //使用routingKey绑定队列到交换器
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(topicExchange()).with(ROUTING_KEY);
    }


    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory("192.168.99.100", 32796);

        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setPublisherConfirms(true);
        cachingConnectionFactory.setPublisherReturns(true);

        return cachingConnectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());


        // 必须设置为 true，不然当 发送到交换器成功，但是没有匹配的队列，不会触发 ReturnCallback 回调
        // 而且 ReturnCallback 比 ConfirmCallback 先回调，意思就是 ReturnCallback 执行完了才会执行 ConfirmCallback
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback(((correlationData, ack, cause) ->{

            Console.log("configrmcallback, correlationData:{}, ack:{}, cause:{}", correlationData, ack, cause);

            if(ack){
                String messageId = correlationData.getId();

                RabbitMetaMesage rabbitMetaMesage = (RabbitMetaMesage) redisTemplate.opsForHash().get(RedisConfig.RETRY_KEY, messageId);

                Console.log("rabbitMetaMessage = {}", rabbitMetaMesage);
                if (!rabbitMetaMesage.isRertrunCallback()) {

                    // 到这一步才能完全保证消息成功发送到了 rabbitmq
                    // 删除 redis 里面的消息
                    redisTemplate.opsForHash().delete(RedisConfig.RETRY_KEY, messageId);
                }
            }
        } ));


        // 设置 ReturnCallback 回调
        // 如果发送到交换器成功，但是没有匹配的队列，就会触发这个回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText,
                                          exchange, routingKey) -> {
            Console.log("ReturnCallback unroutable messages, message = {} , replyCode = {} , replyText = {} , exchange = {} , routingKey = {} ", message, replyCode, replyText, exchange, routingKey);

            // 从 redis 取出消息，设置 returnCallback 设置为 true
            String messageId = message.getMessageProperties().getMessageId();
            RabbitMetaMesage rabbitMetaMessage = (RabbitMetaMesage) redisTemplate.opsForHash().get(RedisConfig.RETRY_KEY, messageId);
            rabbitMetaMessage.setRertrunCallback(true);
            redisTemplate.opsForHash().put(RedisConfig.RETRY_KEY, messageId, rabbitMetaMessage);
        });

        return rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(ChannelAwareMessageListener listener) {
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();

        simpleMessageListenerContainer.setConnectionFactory(connectionFactory());


        //指定消费者
        simpleMessageListenerContainer.setMessageListener(listener);
        //指定监听的队列
        simpleMessageListenerContainer.setQueueNames(QUEUE_NAME);
        //设置消费者的ack模式为手动确认模式
        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        simpleMessageListenerContainer.setPrefetchCount(100);

        return simpleMessageListenerContainer;

    }


    @Bean
    public SimpleMessageConverter simpleMessageConverter() {
        return new SimpleMessageConverter();
    }
}
