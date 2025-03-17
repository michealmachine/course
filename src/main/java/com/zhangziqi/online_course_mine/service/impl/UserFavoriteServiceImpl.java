package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.entity.UserFavorite;
import com.zhangziqi.online_course_mine.model.vo.UserFavoriteVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.UserFavoriteRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 用户收藏课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final UserFavoriteRepository userFavoriteRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long courseId) {
        // 检查是否已收藏
        if (isFavorite(userId, courseId)) {
            return true;
        }
        
        // 检查用户和课程是否存在
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        
        if (userOpt.isEmpty() || courseOpt.isEmpty()) {
            log.warn("添加收藏失败，用户ID {} 或课程ID {} 不存在", userId, courseId);
            return false;
        }
        
        // 创建收藏记录
        UserFavorite favorite = UserFavorite.builder()
                .user(userOpt.get())
                .course(courseOpt.get())
                .build();
        
        userFavoriteRepository.save(favorite);
        log.info("用户 {} 收藏课程 {} 成功", userId, courseId);
        return true;
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long courseId) {
        long count = userFavoriteRepository.deleteByUserAndCourseIds(userId, courseId);
        boolean success = count > 0;
        if (success) {
            log.info("用户 {} 取消收藏课程 {} 成功", userId, courseId);
        } else {
            log.warn("用户 {} 取消收藏课程 {} 失败，可能没有收藏记录", userId, courseId);
        }
        return success;
    }

    @Override
    public boolean isFavorite(Long userId, Long courseId) {
        return userFavoriteRepository.findByUser_IdAndCourse_Id(userId, courseId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserFavoriteVO> getUserFavorites(Long userId, Pageable pageable) {
        Page<UserFavorite> favorites = userFavoriteRepository.findByUser_Id(userId, pageable);
        
        return favorites.map(this::convertToVO);
    }

    @Override
    public long countUserFavorites(Long userId) {
        return userFavoriteRepository.findByUser_Id(userId).size();
    }

    @Override
    public long countCourseFavorites(Long courseId) {
        return userFavoriteRepository.countByCourse_Id(courseId);
    }
    
    /**
     * 将实体转换为VO
     */
    private UserFavoriteVO convertToVO(UserFavorite favorite) {
        Course course = favorite.getCourse();
        String price = "免费";
        if (course.getPaymentType() != null && course.getPaymentType() == 1) { // 付费课程
            BigDecimal coursePrice = course.getDiscountPrice() != null ? course.getDiscountPrice() : course.getPrice();
            if (coursePrice != null) {
                price = "￥" + coursePrice.toString();
            }
        }
        
        String categoryName = "";
        if (course.getCategory() != null) {
            categoryName = course.getCategory().getName();
        }
        
        String institutionName = "";
        if (course.getInstitution() != null) {
            institutionName = course.getInstitution().getName();
        }
        
        return UserFavoriteVO.builder()
                .id(favorite.getId())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseCoverImage(course.getCoverImage())
                .coursePrice(price)
                .categoryName(categoryName)
                .institutionName(institutionName)
                .favoriteTime(favorite.getFavoriteTime())
                .build();
    }
} 