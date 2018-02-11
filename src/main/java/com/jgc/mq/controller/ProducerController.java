package com.jgc.mq.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.Console;
import com.jgc.helloworld.config.RabbitConfig;
import com.jgc.mq.config.RabbitMQConfig;
import com.jgc.mq.config.RedisConfig;
import com.jgc.mq.entity.RabbitMetaMesage;
import com.jgc.mq.util.MsgSeqUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ProducerController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisTemplate redisTemplate;


    @GetMapping("/sendMessage")
    public String sendMesage() {
        new Thread(() -> {

            for (int i = 0; i < 1000000; i++) {
                String value = new DateTime().toString("yyyy-MM-dd HH: mm:ss");
                Console.log("send message {}", value);
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, value);
            }

        }).start();

        return "ok";
    }

    @GetMapping("/send_message_callback")
    public String sendMessageCallback(){
        new Thread(()->{

            HashOperations hashOperations = redisTemplate.opsForHash();

            for(int i = 0; i<1; i++){
                String id = String.valueOf(MsgSeqUtils.getMsgSeq());

                String value = "message"+i;

                RabbitMetaMesage rabbitMetaMesage = new RabbitMetaMesage(value);

                //存入redis
                hashOperations.put(RedisConfig.RETRY_KEY, id, rabbitMetaMesage);

                Console.log("send messge:{}", value);

                //发送mq
                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, value, (message)->{
                    message.getMessageProperties().setMessageId(id);
                    return message;
                }, new CorrelationData(id));






            }

        }).start();

        return "ok";
    }


}
