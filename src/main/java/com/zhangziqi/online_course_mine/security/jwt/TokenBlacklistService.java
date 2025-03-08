package com.zhangziqi.online_course_mine.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将令牌加入黑名单
     *
     * @param token 令牌
     * @param expireTime 过期时间（毫秒）
     */
    public void addToBlacklist(String token, long expireTime) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expireTime, TimeUnit.MILLISECONDS);
        log.info("Token已加入黑名单: {}", token);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
} 