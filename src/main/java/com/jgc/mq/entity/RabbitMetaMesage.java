package com.jgc.mq.entity;

import java.io.Serializable;

public class RabbitMetaMesage implements Serializable {


    private boolean rertrunCallback;
    private Object payload;

    public RabbitMetaMesage(Object payload) {
        this.payload = payload;
    }

    public RabbitMetaMesage() {
    }

    ;


    public boolean isRertrunCallback() {
        return rertrunCallback;
    }

    public void setRertrunCallback(boolean rertrunCallback) {
        this.rertrunCallback = rertrunCallback;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
