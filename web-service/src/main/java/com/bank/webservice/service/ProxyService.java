package com.bank.webservice.service;

import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

public interface ProxyService {

    Mono<Void> addUserInfoToModel(String token, Model model);
}