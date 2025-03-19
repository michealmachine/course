package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import com.zhangziqi.online_course_mine.model.vo.CourseVO;
import com.zhangziqi.online_course_mine.model.vo.UserCourseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户课程服务接口
 */
public interface UserCourseService {

    /**
     * 获取用户的已购课程
     *
     * @param userId 用户ID
     * @return 课程VO列表
     */
    List<CourseVO> getUserPurchasedCourses(Long userId);

    /**
     * 分页获取用户的已购课程
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页课程VO
     */
    Page<CourseVO> getUserPurchasedCourses(Long userId, Pageable pageable);

    /**
     * 获取用户的课程学习记录
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 用户课程VO
     */
    UserCourseVO getUserCourseRecord(Long userId, Long courseId);

    /**
     * 更新用户的课程学习进度
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param progress 进度百分比（0-100）
     * @return 更新后的用户课程VO
     */
    UserCourseVO updateLearningProgress(Long userId, Long courseId, Integer progress);

    /**
     * 记录用户的学习时长
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param duration 学习时长（秒）
     * @return 更新后的用户课程VO
     */
    UserCourseVO recordLearningDuration(Long userId, Long courseId, Integer duration);

    /**
     * 检查用户是否已购买课程
     *
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否已购买
     */
    boolean hasPurchasedCourse(Long userId, Long courseId);

    /**
     * 获取课程的所有学习用户
     *
     * @param courseId 课程ID
     * @return 用户课程VO列表
     */
    List<UserCourseVO> getCourseStudents(Long courseId);

    /**
     * 分页获取课程的学习用户
     *
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 分页用户课程VO
     */
    Page<UserCourseVO> getCourseStudents(Long courseId, Pageable pageable);

    /**
     * 获取机构的所有学习用户
     *
     * @param institutionId 机构ID
     * @return 用户课程VO列表
     */
    List<UserCourseVO> getInstitutionStudents(Long institutionId);

    /**
     * 分页获取机构的学习用户
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 分页用户课程VO
     */
    Page<UserCourseVO> getInstitutionStudents(Long institutionId, Pageable pageable);

    /**
     * 获取用户最近学习的课程
     *
     * @param userId 用户ID
     * @param limit 数量限制
     * @return 课程VO列表
     */
    List<CourseVO> getRecentLearnedCourses(Long userId, int limit);

    /**
     * 创建用户课程关系(购买课程)
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     * @param orderId 订单ID
     * @param isPaid 是否已支付
     * @return 创建的用户课程关系
     */
    UserCourse createUserCourseRelation(Long userId, Long courseId, Long orderId, boolean isPaid);
    
    /**
     * 更新用户课程关系状态为已退款
     * 
     * @param orderId 订单ID
     * @return 更新后的用户课程关系
     */
    UserCourse updateUserCourseRefunded(Long orderId);
    
    /**
     * 根据订单ID查找用户课程关系
     * 
     * @param orderId 订单ID
     * @return 用户课程关系(可能为空)
     */
    Optional<UserCourse> findByOrderId(Long orderId);
    
    /**
     * 分页查询用户指定状态的课程关系
     *
     * @param userId 用户ID
     * @param status 状态值
     * @param pageable 分页参数
     * @return 分页用户课程关系
     */
    Page<UserCourse> findByUserIdAndStatus(Long userId, Integer status, Pageable pageable);
} 