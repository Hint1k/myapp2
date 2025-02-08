package com.bank.webservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@SpringBootApplication
@PropertySource(value = "classpath:kafka.properties")
@Slf4j
public class WebServiceMain {
    public static void main(String[] args) {
        SpringApplication.run(WebServiceMain.class, args);
        waitForService();
        log.info("\n✅ Application started! Go to: \033[34mhttp://localhost:8080\033[0m\n");
    }

    private static void waitForService() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            int retries = 12;
            while (retries > 0) {
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) return;
                } catch (Exception e) {
                    log.info("Waiting for http://localhost:8080/health...");
                }
                retries--;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}