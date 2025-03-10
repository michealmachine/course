package com.zhangziqi.online_course_mine.security.jwt;

import com.zhangziqi.online_course_mine.config.security.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtConfig jwtConfig;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_BLACKLIST_PREFIX = "token:blacklist:user:";
    private static final long BLACKLIST_RETENTION_DAYS = 7;

    /**
     * 将令牌加入黑名单
     *
     * @param token 令牌
     * @param expirationMs 过期时间（毫秒）
     */
    public void addToBlacklist(String token, long expirationMs) {
        String key = TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "1", expirationMs, TimeUnit.MILLISECONDS);
        log.debug("令牌已加入黑名单: {}", token);
    }

    /**
     * 使用户的所有令牌失效
     * 通常在用户角色变更、密码修改等安全敏感操作时调用
     *
     * @param username 用户名
     */
    public void invalidateUserTokens(String username) {
        String key = USER_BLACKLIST_PREFIX + username;
        String timestamp = String.valueOf(System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, timestamp, BLACKLIST_RETENTION_DAYS, TimeUnit.DAYS);
        log.debug("用户 {} 的所有令牌已失效", username);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 令牌
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        // 首先检查token是否直接在黑名单中
        String tokenKey = TOKEN_BLACKLIST_PREFIX + token;
        Boolean isDirectlyBlacklisted = redisTemplate.hasKey(tokenKey);
        if (Boolean.TRUE.equals(isDirectlyBlacklisted)) {
            log.debug("令牌在黑名单中: {}", token);
            return true;
        }

        try {
            // 解析token获取信息
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            Date issuedAt = claims.getIssuedAt();

            // 检查用户级别的黑名单
            String userBlacklistKey = USER_BLACKLIST_PREFIX + username;
            String blacklistedTime = redisTemplate.opsForValue().get(userBlacklistKey);
            
            if (blacklistedTime != null) {
                long blacklistedTimestamp = Long.parseLong(blacklistedTime);
                boolean isBlacklisted = issuedAt.getTime() < blacklistedTimestamp;
                if (isBlacklisted) {
                    log.debug("用户 {} 的令牌已在黑名单中，发布时间: {}, 失效时间: {}", 
                            username, issuedAt, new Date(blacklistedTimestamp));
                }
                return isBlacklisted;
            }
        } catch (Exception e) {
            log.error("检查令牌黑名单状态时出错", e);
            return true; // 如果解析出错，出于安全考虑，将token视为无效
        }

        return false;
    }
} 