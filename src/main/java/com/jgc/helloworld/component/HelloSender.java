package com.jgc.helloworld.component;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 发送者
 */
@Component
public class HelloSender {

    @Resource
    private AmqpTemplate amqpTemplate;

    public void sender(){
        String content = "hello" + new Date();

        System.out.println("Sender:" + content);

        amqpTemplate.convertAndSend("hello", content);
    }
}
