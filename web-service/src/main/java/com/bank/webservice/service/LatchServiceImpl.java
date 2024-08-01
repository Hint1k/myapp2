package com.bank.webservice.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class LatchServiceImpl implements LatchService {

    private volatile CountDownLatch latch;

    @Override
    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public CountDownLatch getLatch() {
        return latch;
    }

    @Override // synchronized to avoid race conditions
    public synchronized void resetLatch() {
        latch = null;
    }
}