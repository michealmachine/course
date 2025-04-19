package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.UserCourse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户课程关联数据访问接口
 */
@Repository
public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {

    /**
     * 根据用户ID和课程ID查询关联记录
     */
    Optional<UserCourse> findByUser_IdAndCourse_Id(Long userId, Long courseId);

    /**
     * 根据订单ID查询关联记录
     */
    Optional<UserCourse> findByOrder_Id(Long orderId);

    /**
     * 根据用户ID查询所有已购课程
     */
    List<UserCourse> findByUser_Id(Long userId);

    /**
     * 分页查询用户已购课程
     */
    Page<UserCourse> findByUser_Id(Long userId, Pageable pageable);

    /**
     * 根据课程ID查询所有购买记录
     */
    List<UserCourse> findByCourse_Id(Long courseId);

    /**
     * 分页查询课程购买记录
     */
    Page<UserCourse> findByCourse_Id(Long courseId, Pageable pageable);

    /**
     * 根据状态查询记录
     */
    List<UserCourse> findByStatus(Integer status);

    /**
     * 分页查询状态记录
     */
    Page<UserCourse> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据用户ID和状态查询记录
     */
    List<UserCourse> findByUser_IdAndStatus(Long userId, Integer status);

    /**
     * 分页查询用户状态记录
     */
    Page<UserCourse> findByUser_IdAndStatus(Long userId, Integer status, Pageable pageable);

    /**
     * 根据课程ID和状态查询记录
     */
    List<UserCourse> findByCourse_IdAndStatus(Long courseId, Integer status);

    /**
     * 分页查询课程状态记录
     */
    Page<UserCourse> findByCourse_IdAndStatus(Long courseId, Integer status, Pageable pageable);

    /**
     * 查询用户学习进度大于等于指定值的课程
     */
    List<UserCourse> findByUser_IdAndProgressGreaterThanEqual(Long userId, Integer progress);

    /**
     * 查询用户最近学习的课程
     */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.user.id = :userId ORDER BY uc.lastLearnAt DESC")
    List<UserCourse> findRecentLearnedCourses(@Param("userId") Long userId, Pageable pageable);

    /**
     * 统计用户已购课程数
     */
    long countByUser_Id(Long userId);

    /**
     * 统计课程购买数
     */
    long countByCourse_Id(Long courseId);

    /**
     * 统计状态记录数
     */
    long countByStatus(Integer status);

    /**
     * 判断用户是否购买过课程
     */
    boolean existsByUser_IdAndCourse_Id(Long userId, Long courseId);

    /**
     * 查询用户课程学习进度
     */
    @Query("SELECT uc.progress FROM UserCourse uc WHERE uc.user.id = :userId AND uc.course.id = :courseId")
    Integer findProgressByUserIdAndCourseId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * 查询机构下所有课程的购买记录
     */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.course.institution.id = :institutionId")
    List<UserCourse> findByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 分页查询机构课程购买记录
     */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.course.institution.id = :institutionId")
    Page<UserCourse> findByInstitutionId(@Param("institutionId") Long institutionId, Pageable pageable);

    /**
     * 根据用户ID、课程ID和状态查询用户课程记录
     */
    Optional<UserCourse> findByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, Integer status);

    /**
     * 检查用户是否购买了指定课程（指定状态）
     */
    boolean existsByUser_IdAndCourse_IdAndStatus(Long userId, Long courseId, Integer status);

    /**
     * 分页查询用户的已购课程
     */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.user.id = :userId AND uc.status = :status")
    Page<UserCourse> findPurchasedCourses(@Param("userId") Long userId, @Param("status") Integer status, Pageable pageable);

    /**
     * 查询用户已购课程列表
     */
    @Query("SELECT uc FROM UserCourse uc WHERE uc.user.id = :userId AND uc.status = :status")
    List<UserCourse> findPurchasedCourses(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 统计课程学习人数
     * 返回课程ID和学习人数
     */
    @Query("SELECT uc.course.id, COUNT(DISTINCT uc.user.id) FROM UserCourse uc " +
           "WHERE uc.course.id = :courseId " +
           "GROUP BY uc.course.id")
    List<Object[]> countLearnersByCourseId(@Param("courseId") Long courseId);

    /**
     * 统计特定进度的用户数量
     * 例如查询完成人数（进度=100）
     */
    @Query("SELECT COUNT(uc) FROM UserCourse uc " +
           "WHERE uc.course.id = :courseId AND uc.progress = :progress")
    Long countByProgress(@Param("courseId") Long courseId, @Param("progress") Integer progress);

    /**
     * 计算课程平均进度
     */
    @Query("SELECT AVG(uc.progress) FROM UserCourse uc WHERE uc.course.id = :courseId")
    Double getAverageProgressByCourseId(@Param("courseId") Long courseId);

    /**
     * 统计机构下所有课程的平均进度
     */
    @Query("SELECT AVG(uc.progress) FROM UserCourse uc WHERE uc.course.institution.id = :institutionId")
    Double getAverageProgressByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 统计机构下所有课程的完成人数
     * 完成定义为进度=100
     */
    @Query("SELECT COUNT(DISTINCT uc.user.id) FROM UserCourse uc " +
           "WHERE uc.course.institution.id = :institutionId AND uc.progress = 100")
    Long countCompletedLearnersByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 获取课程每日平均学习进度
     * 返回日期、平均进度和活跃用户数
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', uc.lastLearnAt, '%Y-%m-%d') as date, " +
           "AVG(uc.progress) as avgProgress, COUNT(DISTINCT uc.user.id) as userCount " +
           "FROM UserCourse uc " +
           "WHERE uc.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND uc.lastLearnAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', uc.lastLearnAt, '%Y-%m-%d') " +
           "ORDER BY date ASC")
    List<Object[]> findDailyProgressTrendByCourse(
            @Param("courseId") Long courseId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 获取特定用户在特定课程的每日学习进度
     * 返回日期、进度和活动次数
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') as date, " +
           "uc.progress as progress, COUNT(lr.id) as activityCount " +
           "FROM UserCourse uc " +
           "JOIN LearningRecord lr ON uc.user.id = lr.user.id AND uc.course.id = lr.course.id " +
           "WHERE uc.user.id = :userId " +
           "AND uc.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d'), uc.progress " +
           "ORDER BY date ASC")
    List<Object[]> findDailyProgressTrendByUserAndCourse(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}