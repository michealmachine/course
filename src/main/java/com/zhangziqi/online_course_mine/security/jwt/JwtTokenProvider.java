package com.zhangziqi.online_course_mine.security.jwt;

import com.zhangziqi.online_course_mine.config.security.JwtConfig;
import com.zhangziqi.online_course_mine.model.dto.JwtTokenDTO;
import com.zhangziqi.online_course_mine.model.entity.Role;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT令牌提供者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    /**
     * 获取密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    /**
     * 创建令牌
     *
     * @param authentication 认证信息
     * @return JWT令牌
     */
    public JwtTokenDTO createToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // 只保留角色信息，不包含具体权限
        String roles = authorities.stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        log.debug("为用户 {} 创建令牌，角色: {}", username, roles);
        
        long now = System.currentTimeMillis();
        
        // 创建访问令牌
        String accessToken = Jwts.builder()
                .subject(username)
                .claim("auth", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtConfig.getAccessTokenExpiration()))
                .signWith(getSigningKey())
                .compact();

        // 创建刷新令牌，同样包含角色信息
        String refreshToken = Jwts.builder()
                .subject(username)
                .claim("auth", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtConfig.getRefreshTokenExpiration()))
                .signWith(getSigningKey())
                .compact();

        return JwtTokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getAccessTokenExpiration())
                .build();
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return JWT令牌
     */
    public JwtTokenDTO refreshToken(String refreshToken) {
        // 验证token并获取信息
        Claims claims = parseToken(refreshToken);
        String username = claims.getSubject();
        String roles = claims.get("auth", String.class);

        log.debug("刷新令牌，用户: {}, 角色: {}", username, roles);

        long now = System.currentTimeMillis();
        
        // 使用refresh token中的信息创建新的access token
        String accessToken = Jwts.builder()
                .subject(username)
                .claim("auth", roles)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtConfig.getAccessTokenExpiration()))
                .signWith(getSigningKey())
                .compact();

        return JwtTokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getAccessTokenExpiration())
                .build();
    }

    /**
     * 从令牌中获取用户信息
     *
     * @param token 令牌
     * @return 认证信息
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseToken(token);

        String auth = claims.get("auth", String.class);
        Collection<? extends GrantedAuthority> authorities = auth != null ?
                Arrays.stream(auth.split(","))
                        .filter(role -> !role.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()) :
                java.util.Collections.emptyList();

        // 添加日志
        log.debug("从token中提取角色信息: {}", authorities);

        UserDetails principal = org.springframework.security.core.userdetails.User.builder()
                .username(claims.getSubject())
                .password("")
                .authorities(authorities)
                .build();
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 解析令牌
     *
     * @param token 令牌
     * @return 声明
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("无效的JWT签名: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌为空: {}", e.getMessage());
        }
        return false;
    }
} 