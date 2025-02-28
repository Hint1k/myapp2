package com.bank.webservice.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
public class CustomRedisSerializerTest {

    // Helper test class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestObject {
        private String key;
    }

    @Test
    public void testSerialize_Success() {
        try {
            // Given: Create a new CustomRedisSerializer instance with an empty typeMapping
            CustomRedisSerializer<TestObject> serializer = new CustomRedisSerializer<>(TestObject.class, Map.of());
            TestObject testObject = new TestObject("testValue");

            // When: Serialize the test object to bytes
            byte[] result = serializer.serialize(testObject);

            // Then: Parse the result as JSON and assert that the "key" field matches "testValue"
            assertNotNull(result, "Serialized result should not be null");

            // Use a standard ObjectMapper to parse the output
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(result);

            // Verify that the 'key' field is correct
            assertNotNull(jsonNode.get("key"), "JSON should contain a 'key' field");
            assertEquals("testValue", jsonNode.get("key").asText(),
                    "The 'key' field should match the expected value");
        } catch (IOException e) {
            log.error("testSerialize_Success() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSerialize_ThrowsException() {
        try {
            // Given: Create a CustomRedisSerializer instance and override its ObjectMapper to simulate failure.
            CustomRedisSerializer<TestObject> serializer = new CustomRedisSerializer<>(TestObject.class, Map.of());
            // Replace the internal ObjectMapper with a spy so we can force a failure
            ObjectMapper mapperSpy = spy(new ObjectMapper().registerModule(new JavaTimeModule()));
            java.lang.reflect.Field field = CustomRedisSerializer.class.getDeclaredField("redisObjectMapper");
            field.setAccessible(true);
            field.set(serializer, mapperSpy);

            TestObject testObject = new TestObject("testValue");

            // Simulate a failure during serialization by throwing a JsonProcessingException
            doThrow(new com.fasterxml.jackson.core.JsonProcessingException("Serialization error") {
            })
                    .when(mapperSpy).writeValueAsBytes(testObject);

            // When & Then: Expect a SerializationException when calling serialize()
            assertThrows(org.springframework.data.redis.serializer.SerializationException.class,
                    () -> serializer.serialize(testObject));
        } catch (Exception e) {
            log.error("testSerialize_ThrowsException() fails: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeserialize_Success() {
        // Given: Create a CustomRedisSerializer instance with an empty type mapping and a valid TestObject.
        CustomRedisSerializer<TestObject> serializer = new CustomRedisSerializer<>(TestObject.class, Map.of());
        TestObject original = new TestObject("testValue");
        // Serialize the object to produce valid JSON bytes
        byte[] bytes = serializer.serialize(original);

        // When: Deserialize the bytes back into a TestObject
        TestObject result = serializer.deserialize(bytes);

        // Then: Verify that the deserialized object matches the original values
        assertNotNull(result, "Deserialized object should not be null");
        assertEquals("testValue", result.getKey(),
                "Deserialized object's key should match the original value");
    }

    @Test
    public void testDeserialize_HandlesTypeMapping() {
        // Given: Create a CustomRedisSerializer with a type mapping that maps "ExternalClass" to TestObject.class.
        Map<String, Class<?>> mapping = Map.of("ExternalClass", TestObject.class);
        CustomRedisSerializer<TestObject> serializer = new CustomRedisSerializer<>(TestObject.class, mapping);

        // Create a JSON string with an external class name in the @class field.
        String json = "{\"@class\":\"ExternalClass\", \"key\":\"testValue\"}";
        byte[] bytes = json.getBytes();

        // When: Deserialize the byte array
        TestObject result = serializer.deserialize(bytes);

        // Then: Verify that the deserialized object is created correctly using the type mapping.
        assertNotNull(result, "Deserialized object should not be null");
        assertEquals("testValue", result.getKey(),
                "Deserialized object's key should match expected value");
    }

    @Test
    public void testDeserialize_ThrowsException() {
        // Given: An invalid JSON byte array that cannot be parsed.
        byte[] invalidJsonBytes = "invalid json".getBytes();
        CustomRedisSerializer<TestObject> serializer = new CustomRedisSerializer<>(TestObject.class, Map.of());

        // When & Then: Expect a SerializationException when trying to deserialize invalid JSON.
        assertThrows(SerializationException.class, () -> serializer.deserialize(invalidJsonBytes));
    }
}