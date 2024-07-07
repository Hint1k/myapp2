package com.bank.webservice.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Setter
@Getter
@Service
public class LatchService {

    private volatile CountDownLatch latch;

    public void resetLatch() {
        latch = null;
    }
}