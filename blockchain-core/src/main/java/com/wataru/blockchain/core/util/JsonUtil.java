package com.wataru.blockchain.core.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 使用Jackson序列化
 * 序列化过程参照 Spring Data Redis 中的序列化
 * {@link GenericJackson2JsonRedisSerializer}
 * Created by zzq on 2017-08-23.
 */
@Slf4j
public class JsonUtil {

    private static final GenericJackson2JsonRedisSerializer genericSerializer = new GenericJackson2JsonRedisSerializer();
    private static final ObjectMapper jsonSerializerMapper = new ObjectMapper();

    static {
        /**
         * 反序列化时忽略不存在的字段
         */
        jsonSerializerMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jsonSerializerMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonSerializerMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        jsonSerializerMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        jsonSerializerMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);

        Class<? extends GenericJackson2JsonRedisSerializer> cls = genericSerializer.getClass();
        try {
            Field field = cls.getDeclaredField("mapper");
            field.setAccessible(true);
            ObjectMapper mapper = (ObjectMapper) field.get(genericSerializer);
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
            mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("", e);
        }
    }

    /**
     * 对象转json
     *
     * @param object
     * @return
     */
    public static String toPrettyJson(Object object) {
        try {
            return jsonSerializerMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 对象转json
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        try {
            return jsonSerializerMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * json转对象
     *
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return jsonSerializerMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return jsonSerializerMapper.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 序列化
     *
     * @param object
     * @return
     */
    public static byte[] serialize(Object object) {
        return genericSerializer.serialize(object);
    }

    /**
     * 反序列化
     *
     * @param source
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] source, Class<T> clazz) {
        return genericSerializer.deserialize(source, clazz);
    }

    public static class A {
        private int ss = 4;
        public Integer getSs() {
            return 6;
        }
        public void setSs() {
            ss = 5;
        }
        public String getHash() {
            return "333444gggg";
        }
    }

    public static void main(String[] args) {
        byte[] s = JsonUtil.serialize(new A());
        System.out.println(new String(s));
        A a = JsonUtil.deserialize(s, A.class);
        System.out.println(a.toString());
    }
}
