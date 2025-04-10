package com.bank.webservice.service.impl;

import com.bank.webservice.service.SwaggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SwaggerServiceImpl implements SwaggerService {

    private final RestTemplate restTemplate;

    @Value("${gateway-service.url}")
    private String gatewayServiceUrl;

    @Override
    public String getGatewayServiceSwagger() {
        String url = gatewayServiceUrl + "/v3/api-docs";
        return restTemplate.getForObject(url, String.class);
    }
}
