package com.bank.webservice.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class LatchService {

    private volatile CountDownLatch latch;

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void resetLatch() {
        latch = null;
    }
}