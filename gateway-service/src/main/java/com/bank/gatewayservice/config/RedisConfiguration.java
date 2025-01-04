package com.bank.gatewayservice.config;

import com.bank.gatewayservice.serialization.CustomRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Define the type mappings between service-specific and local classes
        Map<String, Class<?>> typeMapping = Map.of(
                "com.bank.webservice.dto.Account", com.bank.gatewayservice.dto.Account.class,
                "com.bank.webservice.dto.Transaction", com.bank.gatewayservice.dto.Transaction.class,
                "com.bank.webservice.dto.Customer", com.bank.gatewayservice.dto.Customer.class
        );

        // Use a custom serializer for Redis to handle type mapping
        CustomRedisSerializer<Object> customSerializer = new CustomRedisSerializer<>(Object.class, typeMapping);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(customSerializer);

        return template;
    }
}