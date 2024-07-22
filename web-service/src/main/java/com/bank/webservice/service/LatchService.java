package com.bank.webservice.service;

import java.util.concurrent.CountDownLatch;

public interface LatchService {

    void setLatch(CountDownLatch latch);

    CountDownLatch getLatch();

    void resetLatch();
}