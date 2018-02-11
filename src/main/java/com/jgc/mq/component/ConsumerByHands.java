package com.jgc.mq.component;

import cn.hutool.core.lang.Console;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ConsumerByHands implements ChannelAwareMessageListener {

    @Resource
    private SimpleMessageConverter simpleMessageConverter;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {

        MessageProperties messageProperties = message.getMessageProperties();

        //代表投递的标识符，唯一标识了当前信道上的投递，通过deliverTag,消费者可以告诉RbbitMQ确认收到了当前消息
        long deliverTag = messageProperties.getDeliveryTag();

        //如果重复投递的消息，redelivered为true
        Boolean redelivered = messageProperties.getRedelivered();

        Object originalMessage = simpleMessageConverter.fromMessage(message);

        Console.log("Consume message:{}, deliverTag:{}, redelivered:{}", originalMessage, deliverTag, redelivered);


        //代表消费者确认收到当前消息，第二个参数表示一次是否ack多条消息
        channel.basicAck(deliverTag, false);
    }
}
