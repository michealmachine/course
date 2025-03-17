package com.zhangziqi.online_course_mine.controller;

import com.zhangziqi.online_course_mine.model.vo.Result;
import com.zhangziqi.online_course_mine.model.vo.UserFavoriteVO;
import com.zhangziqi.online_course_mine.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 用户收藏课程控制器
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;
    
    /**
     * 收藏课程
     */
    @PostMapping("/{courseId}")
    public Result<Void> addFavorite(@PathVariable Long courseId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        boolean success = userFavoriteService.addFavorite(userId, courseId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("收藏失败");
        }
    }
    
    /**
     * 取消收藏
     */
    @DeleteMapping("/{courseId}")
    public Result<Void> removeFavorite(@PathVariable Long courseId,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        boolean success = userFavoriteService.removeFavorite(userId, courseId);
        
        if (success) {
            return Result.success();
        } else {
            return Result.fail("取消收藏失败");
        }
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/check/{courseId}")
    public Result<Boolean> checkFavorite(@PathVariable Long courseId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        boolean isFavorite = userFavoriteService.isFavorite(userId, courseId);
        
        return Result.success(isFavorite);
    }
    
    /**
     * 获取收藏列表
     */
    @GetMapping
    public Result<Page<UserFavoriteVO>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = Long.valueOf(userDetails.getUsername());
        
        PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "favoriteTime"));
        
        Page<UserFavoriteVO> favorites = userFavoriteService.getUserFavorites(userId, pageRequest);
        
        return Result.success(favorites);
    }
    
    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public Result<Long> getFavoriteCount(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.valueOf(userDetails.getUsername());
        long count = userFavoriteService.countUserFavorites(userId);
        
        return Result.success(count);
    }
} 