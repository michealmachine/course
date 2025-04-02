package com.zhangziqi.online_course_mine.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // 缓存名称常量
    public static final String USER_CACHE = "userCache";
    public static final String PERMISSION_CACHE = "permissions";
    public static final String ROLE_CACHE = "roles";
    public static final String QUOTA_STATS_CACHE = "quotaStats_v2"; // 添加版本号，避免使用旧缓存
    public static final String MEDIA_ACTIVITY_CACHE = "mediaActivity"; // 媒体活动缓存

    // 缓存时间常量（分钟）
    private static final long DEFAULT_EXPIRE_MINUTES = 30;
    private static final long USER_EXPIRE_MINUTES = 60;
    private static final long PERMISSION_EXPIRE_MINUTES = 120; // 权限缓存2小时
    private static final long ROLE_EXPIRE_MINUTES = 120; // 角色缓存2小时
    private static final long QUOTA_STATS_EXPIRE_MINUTES = 15; // 配额统计缓存15分钟
    private static final long MEDIA_ACTIVITY_EXPIRE_MINUTES = 30; // 媒体活动缓存30分钟
    
    /**
     * 配置Redis缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 使用Jackson2JsonRedisSerializer作为序列化器
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        // 配置ObjectMapper，确保序列化时保留类型信息
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 支持Java 8时间类型
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        serializer.setObjectMapper(objectMapper);

        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(DEFAULT_EXPIRE_MINUTES))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // 自定义不同缓存名称的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户缓存配置
        cacheConfigurations.put(USER_CACHE, 
            defaultCacheConfig.entryTtl(Duration.ofMinutes(USER_EXPIRE_MINUTES)));

        // 权限缓存配置
        cacheConfigurations.put(PERMISSION_CACHE, 
            defaultCacheConfig.entryTtl(Duration.ofMinutes(PERMISSION_EXPIRE_MINUTES)));

        // 角色缓存配置
        cacheConfigurations.put(ROLE_CACHE, 
            defaultCacheConfig.entryTtl(Duration.ofMinutes(ROLE_EXPIRE_MINUTES)));
            
        // 配额统计缓存配置
        cacheConfigurations.put(QUOTA_STATS_CACHE, 
            defaultCacheConfig.entryTtl(Duration.ofMinutes(QUOTA_STATS_EXPIRE_MINUTES)));

        // 媒体活动缓存配置
        cacheConfigurations.put(MEDIA_ACTIVITY_CACHE, 
            defaultCacheConfig.entryTtl(Duration.ofMinutes(MEDIA_ACTIVITY_EXPIRE_MINUTES)));

        // 构建缓存管理器
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
} 