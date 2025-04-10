package com.bank.webservice.controller;

import com.bank.webservice.service.SwaggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SwaggerController {

    private final SwaggerService swaggerService;

    @GetMapping("/api-docs")
    public ResponseEntity<String> getGatewayServiceSwagger() {
        // Fetch the Swagger JSON from the gateway-service
        String swaggerJson = swaggerService.getGatewayServiceSwagger();
        return ResponseEntity.ok(swaggerJson);
    }
}
