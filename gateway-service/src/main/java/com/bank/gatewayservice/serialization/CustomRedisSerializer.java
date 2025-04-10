package com.bank.gatewayservice.serialization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class CustomRedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper redisObjectMapper;
    private final Class<T> targetType;
    private final Map<String, Class<?>> typeMapping;

    public CustomRedisSerializer(Class<T> targetType, Map<String, Class<?>> typeMapping) {
        this.targetType = targetType;
        this.typeMapping = typeMapping;

        this.redisObjectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        try {
            return redisObjectMapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error serializing value", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            // Read JSON as a tree to inspect and modify
            JsonNode rootNode = redisObjectMapper.readTree(bytes);

            // Check and replace the `@class` field if needed
            JsonNode classNode = rootNode.get("@class");
            if (classNode != null) {
                String originalClassName = classNode.asText();
                if (typeMapping.containsKey(originalClassName)) {
                    // Replace external class name with the corresponding local class name.
                    ((ObjectNode) rootNode).put("@class", typeMapping.get(originalClassName).getName());
                }
            }

            // Deserialize the modified JSON tree
            return redisObjectMapper.treeToValue(rootNode, targetType);

        } catch (IOException e) {
            throw new SerializationException("Error deserializing value", e);
        }
    }
}