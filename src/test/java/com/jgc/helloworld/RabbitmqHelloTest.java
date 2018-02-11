package com.jgc.helloworld;

import com.jgc.helloworld.component.HelloSender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqHelloTest {

    @Resource
    private HelloSender helloSender;

    @Test
    public void hello(){
        helloSender.sender();
    }
}
