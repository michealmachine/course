package com.zhangziqi.online_course_mine.security;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.security.jwt.JwtTokenProvider.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 安全工具类
 * 用于从安全上下文中获取当前用户信息
 */
@Slf4j
public class SecurityUtil {

    /**
     * 获取当前认证对象
     *
     * @return 认证对象
     */
    public static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new BusinessException(401, "用户未认证");
        }
        
        return authentication;
    }

    /**
     * 获取当前认证用户
     *
     * @return 用户主体对象
     */
    public static Object getCurrentUser() {
        return getAuthentication().getPrincipal();
    }
    
    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        
        // 从JWT token中获取用户ID
        if (authentication instanceof JwtAuthenticationToken) {
            Claims claims = ((JwtAuthenticationToken) authentication).getClaims();
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                }
            }
        }
        
        // 如果无法获取用户ID，抛出异常
        throw new BusinessException(401, "无法获取当前用户ID");
    }
    
    /**
     * 获取当前用户的用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        // 从Authentication获取用户名
        return getAuthentication().getName();
    }
    
    /**
     * 获取当前用户的机构ID
     *
     * @return 机构ID
     */
    public static Long getCurrentInstitutionId() {
        // 获取认证对象
        Authentication authentication = getAuthentication();
        
        // 从JWT token中获取机构ID
        if (authentication instanceof JwtAuthenticationToken) {
            Long institutionId = ((JwtAuthenticationToken) authentication).getInstitutionId();
            if (institutionId != null) {
                return institutionId;
            }
        }
        
        // 判断是否是机构用户角色
        if (hasRole("INSTITUTION")) {
            // 如果是机构用户，但没有找到机构ID，返回默认值0
            log.warn("无法从认证对象获取机构ID，但用户具有INSTITUTION角色，使用默认值0");
            return 0L;
        }
        
        // 不是机构用户，没有机构ID
        return null;
    }
    
    /**
     * 获取当前用户的权限列表
     *
     * @return 权限集合
     */
    public static Set<String> getCurrentUserAuthorities() {
        Authentication authentication = getAuthentication();
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
    
    /**
     * 检查当前用户是否有指定角色
     *
     * @param role 角色名称
     * @return 是否有该角色
     */
    public static boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUserAuthorities().contains(roleWithPrefix);
    }
    
    /**
     * 检查当前用户是否有指定权限
     *
     * @param permission 权限编码
     * @return 是否有该权限
     */
    public static boolean hasPermission(String permission) {
        return getCurrentUserAuthorities().contains(permission);
    }
    
    /**
     * 检查当前用户是否是超级管理员
     *
     * @return 是否是超级管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * 检查当前用户是否是机构管理员
     *
     * @return 是否是机构管理员
     */
    public static boolean isInstitutionAdmin() {
        return hasRole("INSTITUTION_ADMIN");
    }
    
    /**
     * 检查当前用户是否是机构教师
     *
     * @return 是否是机构教师
     */
    public static boolean isInstitutionTeacher() {
        return hasRole("INSTITUTION_TEACHER");
    }
    
    /**
     * 检查当前用户是否属于指定机构
     *
     * @param institutionId 机构ID
     * @return 是否属于该机构
     */
    public static boolean belongsToInstitution(Long institutionId) {
        Long currentInstitutionId = getCurrentInstitutionId();
        return currentInstitutionId != null && currentInstitutionId.equals(institutionId);
    }
} 