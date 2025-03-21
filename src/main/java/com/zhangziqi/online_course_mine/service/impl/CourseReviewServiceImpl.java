package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.dto.review.ReviewCreateDTO;
import com.zhangziqi.online_course_mine.model.dto.review.ReviewQueryDTO;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.CourseReview;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.vo.CourseReviewSectionVO;
import com.zhangziqi.online_course_mine.model.vo.ReviewStatsVO;
import com.zhangziqi.online_course_mine.model.vo.ReviewVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.CourseReviewRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.service.CourseReviewService;
import com.zhangziqi.online_course_mine.service.CourseService;
import com.zhangziqi.online_course_mine.service.UserCourseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课程评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseReviewServiceImpl implements CourseReviewService {
    
    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final UserCourseService userCourseService;
    
    @Override
    @Transactional
    public ReviewVO createReview(ReviewCreateDTO dto, Long userId) {
        // 检查课程是否存在
        Course course = courseRepository.findById(dto.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + dto.getCourseId()));
        
        // 检查用户是否评价过该课程
        if (reviewRepository.existsByUserIdAndCourseId(userId, dto.getCourseId())) {
            throw new BusinessException(400, "您已经评价过该课程");
        }
        
        // 检查用户是否购买了该课程
        if (!userCourseService.hasPurchasedCourse(userId, dto.getCourseId())) {
            throw new BusinessException(403, "您尚未购买该课程，不能评价");
        }
        
        // 创建评论实体
        CourseReview review = CourseReview.builder()
            .course(course)
            .userId(userId)
            .rating(dto.getRating())
            .content(dto.getContent())
            .likeCount(0)
            .build();
        
        // 保存评论
        CourseReview savedReview = reviewRepository.save(review);
        
        // 更新课程评分
        courseService.updateCourseRating(dto.getCourseId(), dto.getRating());
        
        // 获取用户信息
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 返回评论VO
        return ReviewVO.fromEntity(savedReview, user.getUsername(), user.getAvatar());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseReviewSectionVO getCourseReviewSection(Long courseId, Integer page, Integer size, String orderBy) {
        // 检查课程是否存在
        courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 默认页面参数
        if (page == null) page = 0;
        if (size == null) size = 10;
        if (orderBy == null) orderBy = "newest";
        
        // 构建排序
        Sort sort;
        if ("highest_rating".equals(orderBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rating");
        } else if ("lowest_rating".equals(orderBy)) {
            sort = Sort.by(Sort.Direction.ASC, "rating");
        } else {
            // 默认按最新排序
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        // 创建分页请求
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        // 查询评论
        Page<CourseReview> reviewPage = reviewRepository.findByCourseId(courseId, pageRequest);
        
        // 获取所有评论中的用户ID
        Set<Long> userIds = reviewPage.getContent().stream()
            .map(CourseReview::getUserId)
            .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        // 转换评论列表
        List<ReviewVO> reviewVOs = reviewPage.getContent().stream()
            .map(review -> {
                User user = userMap.get(review.getUserId());
                if (user != null) {
                    return ReviewVO.fromEntity(review, user.getUsername(), user.getAvatar());
                } else {
                    return ReviewVO.fromEntity(review, "未知用户", null);
                }
            })
            .collect(Collectors.toList());
        
        // 获取评分统计
        ReviewStatsVO stats = getReviewStats(courseId);
        
        // 构建评论区VO
        return CourseReviewSectionVO.builder()
            .courseId(courseId)
            .stats(stats)
            .reviews(reviewVOs)
            .totalReviews((int) reviewPage.getTotalElements())
            .currentPage(page)
            .totalPages(reviewPage.getTotalPages())
            .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewVO> getReviewsByCourse(Long courseId, ReviewQueryDTO queryDTO, Pageable pageable) {
        // 构建查询条件
        Specification<CourseReview> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 课程ID条件
            predicates.add(cb.equal(root.get("course").get("id"), courseId));
            
            // 评分筛选条件
            if (queryDTO != null && queryDTO.getRatingFilter() != null) {
                predicates.add(cb.equal(root.get("rating"), queryDTO.getRatingFilter()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<CourseReview> reviewPage = reviewRepository.findAll(spec, pageable);
        
        // 获取所有评论中的用户ID
        Set<Long> userIds = reviewPage.getContent().stream()
            .map(CourseReview::getUserId)
            .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(User::getId, user -> user));
        
        // 转换为ReviewVO列表
        List<ReviewVO> reviewVOs = reviewPage.getContent().stream()
            .map(review -> {
                User user = userMap.get(review.getUserId());
                if (user != null) {
                    return ReviewVO.fromEntity(review, user.getUsername(), user.getAvatar());
                } else {
                    return ReviewVO.fromEntity(review, "未知用户", null);
                }
            })
            .collect(Collectors.toList());
        
        // 创建新的Page对象
        return new PageImpl<>(reviewVOs, pageable, reviewPage.getTotalElements());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewStatsVO getReviewStats(Long courseId) {
        // 检查课程是否存在
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 获取评分分布
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        
        // 查询每个评分的数量
        for (int rating = 1; rating <= 5; rating++) {
            long count = reviewRepository.countByCourseIdAndRating(courseId, rating);
            ratingDistribution.put(rating, (int) count);
        }
        
        // 构建并返回评分统计VO
        return ReviewStatsVO.builder()
            .courseId(courseId)
            .averageRating(course.getAverageRating())
            .ratingCount(course.getRatingCount())
            .ratingDistribution(ratingDistribution)
            .build();
    }
    
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 查找评论
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("评论不存在，ID: " + reviewId));
        
        // 检查是否是评论的作者或管理员
        if (!review.getUserId().equals(userId)) {
            // TODO: 后续可以添加管理员权限检查
            throw new BusinessException(403, "没有权限删除此评论");
        }
        
        // 删除评论
        reviewRepository.delete(review);
        
        // TODO: 更新课程评分 - 需要在CourseService中添加removeRating方法
    }
    
    @Override
    @Transactional(readOnly = true)
    public ReviewVO getUserReviewOnCourse(Long userId, Long courseId) {
        // 查询用户在课程上的评论
        Optional<CourseReview> reviewOpt = reviewRepository.findByUserIdAndCourseId(userId, courseId);
        
        if (reviewOpt.isEmpty()) {
            return null;
        }
        
        CourseReview review = reviewOpt.get();
        
        // 获取用户信息
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 返回评论VO
        return ReviewVO.fromEntity(review, user.getUsername(), user.getAvatar());
    }
    
    @Override
    @Transactional
    public ReviewVO updateReview(Long reviewId, ReviewCreateDTO dto, Long userId) {
        // 查找评论
        CourseReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("评论不存在，ID: " + reviewId));
        
        // 检查是否是评论的作者
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此评论");
        }
        
        // 验证评分范围
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }
        
        // 保存旧评分用于更新课程评分
        Integer oldRating = review.getRating();
        
        // 更新评论
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        
        // 保存更新
        CourseReview updatedReview = reviewRepository.save(review);
        
        // 更新课程评分（如果评分改变）
        if (!oldRating.equals(dto.getRating())) {
            courseService.updateCourseRating(review.getCourseId(), oldRating, dto.getRating());
        }
        
        // 获取用户信息
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 返回更新的评论VO
        return ReviewVO.fromEntity(updatedReview, user.getUsername(), user.getAvatar());
    }
} 