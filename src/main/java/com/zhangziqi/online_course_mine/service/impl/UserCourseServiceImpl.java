package com.zhangziqi.online_course_mine.service.impl;

import com.zhangziqi.online_course_mine.exception.BusinessException;
import com.zhangziqi.online_course_mine.exception.ResourceNotFoundException;
import com.zhangziqi.online_course_mine.model.entity.Course;
import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.entity.User;
import com.zhangziqi.online_course_mine.model.entity.Order;
import com.zhangziqi.online_course_mine.model.enums.UserCourseStatus;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.UserCourseVO;
import com.zhangziqi.online_course_mine.repository.CourseRepository;
import com.zhangziqi.online_course_mine.repository.UserCourseRepository;
import com.zhangziqi.online_course_mine.repository.UserRepository;
import com.zhangziqi.online_course_mine.repository.OrderRepository;
import com.zhangziqi.online_course_mine.service.UserCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCourseServiceImpl implements UserCourseService {

    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> getUserPurchasedCourses(Long userId) {
        List<UserCourse> userCourses = userCourseRepository.findByUser_Id(userId);
        
        return userCourses.stream()
                .map(userCourse -> CourseVO.fromEntity(userCourse.getCourse()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseVO> getUserPurchasedCourses(Long userId, Pageable pageable) {
        Page<UserCourse> userCoursePage = userCourseRepository.findByUser_Id(userId, pageable);
        
        List<CourseVO> courseVOs = userCoursePage.getContent().stream()
                .map(userCourse -> CourseVO.fromEntity(userCourse.getCourse()))
                .collect(Collectors.toList());
        
        return new PageImpl<>(courseVOs, pageable, userCoursePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserCourseVO getUserCourseRecord(Long userId, Long courseId) {
        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到学习记录，请先购买课程"));
        
        return UserCourseVO.fromEntity(userCourse);
    }

    @Override
    @Transactional
    public UserCourseVO updateLearningProgress(Long userId, Long courseId, Integer progress) {
        // 验证进度范围
        if (progress < 0 || progress > 100) {
            throw new BusinessException(400, "学习进度必须在0-100之间");
        }
        
        // 查询用户课程记录
        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到学习记录，请先购买课程"));
        
        // 只有当新进度大于原进度时才更新
        if (progress > userCourse.getProgress()) {
            userCourse.setProgress(progress);
            userCourse.setLastLearnAt(LocalDateTime.now());
            
            userCourseRepository.save(userCourse);
        }
        
        return UserCourseVO.fromEntity(userCourse);
    }

    @Override
    @Transactional
    public UserCourseVO recordLearningDuration(Long userId, Long courseId, Integer duration) {
        // 验证时长有效性
        if (duration <= 0) {
            throw new BusinessException(400, "学习时长必须大于0");
        }
        
        // 查询用户课程记录
        UserCourse userCourse = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到学习记录，请先购买课程"));
        
        // 累加学习时长
        userCourse.setLearnDuration(userCourse.getLearnDuration() + duration);
        userCourse.setLastLearnAt(LocalDateTime.now());
        
        userCourseRepository.save(userCourse);
        
        return UserCourseVO.fromEntity(userCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPurchasedCourse(Long userId, Long courseId) {
        // 只检查正常状态(0)的记录，忽略已退款(2)的记录
        return userCourseRepository.existsByUser_IdAndCourse_IdAndStatus(userId, courseId, UserCourseStatus.NORMAL.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCourseVO> getCourseStudents(Long courseId) {
        List<UserCourse> userCourses = userCourseRepository.findByCourse_Id(courseId);
        
        return userCourses.stream()
                .map(UserCourseVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserCourseVO> getCourseStudents(Long courseId, Pageable pageable) {
        Page<UserCourse> userCoursePage = userCourseRepository.findByCourse_Id(courseId, pageable);
        
        return userCoursePage.map(UserCourseVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCourseVO> getInstitutionStudents(Long institutionId) {
        List<UserCourse> userCourses = userCourseRepository.findByInstitutionId(institutionId);
        
        return userCourses.stream()
                .map(UserCourseVO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserCourseVO> getInstitutionStudents(Long institutionId, Pageable pageable) {
        Page<UserCourse> userCoursePage = userCourseRepository.findByInstitutionId(institutionId, pageable);
        
        return userCoursePage.map(UserCourseVO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseVO> getRecentLearnedCourses(Long userId, int limit) {
        // 查询用户最近学习的课程
        List<UserCourse> recentCourses = userCourseRepository.findRecentLearnedCourses(
                userId, PageRequest.of(0, limit));
        
        return recentCourses.stream()
                .map(userCourse -> CourseVO.fromEntity(userCourse.getCourse()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserCourse createUserCourseRelation(Long userId, Long courseId, Long orderId, boolean isPaid) {
        log.info("创建用户课程关系, 用户ID: {}, 课程ID: {}, 订单ID: {}, 是否已支付: {}", userId, courseId, orderId, isPaid);
        
        // 检查用户是否已购买该课程
        Optional<UserCourse> existingRelation = userCourseRepository.findByUser_IdAndCourse_Id(userId, courseId);
        if (existingRelation.isPresent()) {
            log.info("用户已购买该课程，无需重复创建关系");
            return existingRelation.get();
        }
        
        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + userId));
        
        // 查询课程
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
        
        // 查询订单（如果有）
        Order order = null;
        if (orderId != null) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("订单不存在，ID: " + orderId));
        }
        
        // 创建用户课程关系
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setPurchasedAt(LocalDateTime.now());
        userCourse.setOrder(order);
        userCourse.setProgress(0);
        userCourse.setStatusEnum(isPaid ? UserCourseStatus.NORMAL : UserCourseStatus.EXPIRED);
        userCourse.setLearnDuration(0);
        
        // 保存关系
        userCourseRepository.save(userCourse);
        
        // 更新课程学生数（使用乐观锁处理并发）
        try {
            course.incrementStudentCount();
            courseRepository.save(course);
        } catch (Exception e) {
            log.warn("更新课程学生数失败，将重试", e);
            // 重新获取课程并重试
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
            course.incrementStudentCount();
            courseRepository.save(course);
        }
        
        log.info("用户课程关系创建成功, ID: {}", userCourse.getId());
        return userCourse;
    }

    @Override
    @Transactional
    public UserCourse updateUserCourseRefunded(Long orderId) {
        log.info("更新用户课程关系为已退款, 订单ID: {}", orderId);
        
        // 查询订单关联的用户课程
        UserCourse userCourse = userCourseRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到与订单关联的课程记录，订单ID: " + orderId));
        
        // 更新状态为已退款
        userCourse.setStatusEnum(UserCourseStatus.REFUNDED);
        userCourseRepository.save(userCourse);
        
        // 更新课程学生数（使用乐观锁处理并发）
        final Long courseId = userCourse.getCourse().getId();
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException("课程不存在，ID: " + courseId));
                course.decrementStudentCount();
                courseRepository.save(course);
                break;
            } catch (Exception e) {
                retryCount++;
                if (retryCount == maxRetries) {
                    log.error("更新课程学生数失败，已重试{}次", maxRetries, e);
                    throw new BusinessException(500, "更新课程学生数失败");
                }
                log.warn("更新课程学生数失败，正在进行第{}次重试", retryCount);
            }
        }
        
        log.info("用户课程关系已更新为退款状态, ID: {}", userCourse.getId());
        return userCourse;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCourse> findByOrderId(Long orderId) {
        log.info("根据订单ID查询用户课程关系, 订单ID: {}", orderId);
        return userCourseRepository.findByOrder_Id(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserCourse> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable) {
        log.info("分页查询用户指定状态的课程关系, 用户ID: {}, 状态: {}, 分页: {}", userId, status, pageable);
        return userCourseRepository.findByUser_IdAndStatus(userId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserCourse> findByUserIdAndCourseIdAndStatus(Long userId, Long courseId, Integer status) {
        return userCourseRepository.findByUser_IdAndCourse_IdAndStatus(userId, courseId, status);
    }

    @Override
    @Transactional
    public UserCourse save(UserCourse userCourse) {
        return userCourseRepository.save(userCourse);
    }
} 