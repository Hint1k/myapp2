package com.bank.webservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
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
                    .uri(URI.create("http://localhost:8080/actuator/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            int retries = 10;
            while (retries > 0) {
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        Thread.sleep(10000); // Wait for logs to settle down
                        return;
                    }
                } catch (IOException e) {
                    log.info("Waiting for http://localhost:8080/actuator/health...");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    log.error("Thread was interrupted. Exiting...");
                    return;
                }

                retries--;
                try {
                    Thread.sleep(3000); // Pause before retrying
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Application interrupted during wait. Exiting...");
                    return;
                }
            }
        }

        log.error("Service did not respond after multiple retries. Exiting...");
        System.exit(1);
    }
}