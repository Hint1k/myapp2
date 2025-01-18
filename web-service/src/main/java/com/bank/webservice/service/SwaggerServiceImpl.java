package com.bank.webservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SwaggerServiceImpl implements SwaggerService {

    private final RestTemplate restTemplate;

    @Value("${gateway-service.url}")
    private String gatewayServiceUrl;

    @Autowired
    public SwaggerServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getGatewayServiceSwagger() {
        String url = gatewayServiceUrl + "/v3/api-docs";
        return restTemplate.getForObject(url, String.class);
    }
}
