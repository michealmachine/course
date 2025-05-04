package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.config.CacheConfig;
import com.zhangziqi.online_course_mine.model.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 缓存管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/cache")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCacheController {

    private final CacheManager cacheManager;

    /**
     * 清除所有缓存
     *
     * @return 清除的缓存名称列表
     */
    @PostMapping("/clear-all")
    @ResponseStatus(HttpStatus.OK)
    public Result<List<String>> clearAllCaches() {
        log.info("管理员请求清除所有缓存");
        
        List<String> clearedCaches = new ArrayList<>();
        
        // 清除所有已知的缓存
        clearCache(CacheConfig.USER_CACHE, clearedCaches);
        clearCache(CacheConfig.PERMISSION_CACHE, clearedCaches);
        clearCache(CacheConfig.ROLE_CACHE, clearedCaches);
        clearCache(CacheConfig.QUOTA_STATS_CACHE, clearedCaches);
        clearCache(CacheConfig.MEDIA_ACTIVITY_CACHE, clearedCaches);
        clearCache(CacheConfig.MEDIA_STATS_CACHE, clearedCaches);
        clearCache(CacheConfig.USER_STATS_CACHE, clearedCaches);
        clearCache(CacheConfig.INSTITUTION_STATS_CACHE, clearedCaches);
        clearCache(CacheConfig.COURSE_STATS_CACHE, clearedCaches);
        clearCache(CacheConfig.ADMIN_STATS_CACHE, clearedCaches);
        
        log.info("成功清除 {} 个缓存", clearedCaches.size());
        return Result.success(clearedCaches);
    }
    
    /**
     * 清除指定名称的缓存
     *
     * @param cacheName 缓存名称
     * @param clearedCaches 已清除的缓存名称列表
     */
    private void clearCache(String cacheName, List<String> clearedCaches) {
        try {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).clear();
                clearedCaches.add(cacheName);
                log.info("已清除缓存: {}", cacheName);
            }
        } catch (Exception e) {
            log.error("清除缓存 {} 时发生错误: {}", cacheName, e.getMessage());
        }
    }
}
