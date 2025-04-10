package com.bank.gatewayservice.config;

import com.bank.gatewayservice.dto.Account;
import com.bank.gatewayservice.serialization.CustomRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
@Slf4j
public class RedisConfigurationTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    public void testRedisTemplateBean() {
        try {
            // Arrange
            RedisConfiguration redisConfiguration = new RedisConfiguration();

            // Act
            RedisTemplate<String, Object> redisTemplate = redisConfiguration.redisTemplate(redisConnectionFactory);

            // Assert
            assertNotNull(redisTemplate, "RedisTemplate should not be null");
            assertEquals(redisConnectionFactory, redisTemplate.getConnectionFactory(),
                    "Connection factory should match");

            // Verify key serializer
            assertInstanceOf(StringRedisSerializer.class, redisTemplate.getKeySerializer(),
                    "Key serializer should be StringRedisSerializer");

            // Verify value serializer
            assertInstanceOf(CustomRedisSerializer.class, redisTemplate.getValueSerializer(),
                    "Value serializer should be CustomRedisSerializer");

            // Verify that the custom serializer is correctly configured
            CustomRedisSerializer<?> customSerializer = (CustomRedisSerializer<?>) redisTemplate.getValueSerializer();

            // Indirectly test the serializer by serializing and deserializing an object
            Account account = new Account();
            account.setAccountId(1L);
            account.setCustomerNumber(101L);

            // Cast the serializer to the appropriate type
            @SuppressWarnings("unchecked")
            CustomRedisSerializer<Account> accountSerializer = (CustomRedisSerializer<Account>) customSerializer;

            // Serialize the account object
            byte[] serialized = accountSerializer.serialize(account);
            assertNotNull(serialized, "Serialized data should not be null");

            // Deserialize the serialized data
            Account deserialized = accountSerializer.deserialize(serialized);
            assertNotNull(deserialized, "Deserialized object should not be null");
            assertEquals(account.getAccountId(), deserialized.getAccountId(), "Account ID should match");
            assertEquals(account.getCustomerNumber(), deserialized.getCustomerNumber(),
                    "Customer number should match");
        } catch (Exception e) {
            log.error("testRedisTemplateBean() failed: {}", e.getMessage());
            fail("Serialization/deserialization should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    public void testTypeMappingInDeserialization() {
        // Arrange
        Map<String, Class<?>> typeMapping = Map.of(
                "com.bank.webservice.dto.Account", Account.class
        );

        CustomRedisSerializer<Object> serializer = new CustomRedisSerializer<>(Object.class, typeMapping);

        Account account = new Account();
        account.setAccountId(1L);
        account.setCustomerNumber(101L);

        // Act
        byte[] serialized = serializer.serialize(account);
        Account deserialized = (Account) serializer.deserialize(serialized);

        // Assert
        assertNotNull(deserialized, "Deserialized object should not be null");
        assertEquals(account.getAccountId(), deserialized.getAccountId(), "Account ID should match");
        assertEquals(account.getCustomerNumber(), deserialized.getCustomerNumber(),
                "Customer number should match");
    }

    @Test
    public void testDeserializationWithInvalidData() {
        // Arrange
        CustomRedisSerializer<Account> serializer = new CustomRedisSerializer<>(Account.class, Map.of());

        byte[] invalidData = "Invalid JSON".getBytes();

        // Act & Assert
        assertThrows(SerializationException.class, () -> serializer.deserialize(invalidData),
                "Should throw SerializationException for invalid JSON");
    }

    @Test
    public void testSerializationOfNullValue() {
        // Arrange
        CustomRedisSerializer<Account> serializer = new CustomRedisSerializer<>(Account.class, Map.of());

        // Act
        byte[] serialized = serializer.serialize(null);

        // Assert
        assertNotNull(serialized, "Serialized value should not be null");
        assertArrayEquals("null".getBytes(), serialized, "Serialized null should be 'null' as a byte array");
    }

}