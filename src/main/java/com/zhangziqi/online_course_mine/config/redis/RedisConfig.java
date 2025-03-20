package com.zhangziqi.online_course_mine.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置
 */
@Configuration
public class RedisConfig {

    /**
     * 创建支持Java 8日期时间类型的ObjectMapper
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * Redis模板配置
     * 使用支持Java 8日期时间的GenericJackson2JsonRedisSerializer作为默认序列化器
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 使用GenericJackson2JsonRedisSerializer来序列化和反序列化redis的value值
        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
} 