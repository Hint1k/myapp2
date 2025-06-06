package com.bank.webservice.config;

import com.bank.webservice.serialization.CustomRedisSerializer;
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
                "com.bank.gatewayservice.dto.Account", com.bank.webservice.dto.Account.class,
                "com.bank.gatewayservice.dto.Transaction", com.bank.webservice.dto.Transaction.class,
                "com.bank.gatewayservice.dto.Customer", com.bank.webservice.dto.Customer.class
        );

        // Use a custom serializer for Redis to handle type mapping
        CustomRedisSerializer<Object> customSerializer = new CustomRedisSerializer<>(Object.class, typeMapping);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(customSerializer);

        return template;
    }
}