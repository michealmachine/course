package com.zhangziqi.online_course_mine.repository;

import com.zhangziqi.online_course_mine.model.entity.LearningRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 学习记录数据访问接口
 */
@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {

    /**
     * 查找用户当前正在进行的学习活动（没有结束时间的记录）
     */
    Optional<LearningRecord> findByUser_IdAndActivityEndTimeIsNull(Long userId);

    /**
     * 根据用户ID查询所有学习记录
     */
    List<LearningRecord> findByUser_Id(Long userId);

    /**
     * 分页查询用户学习记录，按开始时间降序排序
     */
    Page<LearningRecord> findByUser_IdOrderByActivityStartTimeDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID和课程ID查询学习记录
     */
    List<LearningRecord> findByUser_IdAndCourse_Id(Long userId, Long courseId);

    /**
     * 分页查询用户特定课程的学习记录
     */
    Page<LearningRecord> findByUser_IdAndCourse_IdOrderByActivityStartTimeDesc(
            Long userId, Long courseId, Pageable pageable);

    /**
     * 根据用户ID和活动类型查询学习记录
     */
    List<LearningRecord> findByUser_IdAndActivityType(Long userId, String activityType);

    /**
     * 分页查询用户特定活动类型的学习记录
     */
    Page<LearningRecord> findByUser_IdAndActivityTypeOrderByActivityStartTimeDesc(
            Long userId, String activityType, Pageable pageable);

    /**
     * 获取用户在指定时间范围内的学习记录
     */
    List<LearningRecord> findByUser_IdAndActivityStartTimeBetween(
            Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户学习热力图数据
     * 按星期几和小时分组统计学习活动次数
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', lr.activityStartTime) as weekday, " +
           "HOUR(lr.activityStartTime) as hour, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "WHERE lr.user.id = :userId " +
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DAYOFWEEK', lr.activityStartTime), HOUR(lr.activityStartTime)")
    List<Object[]> findLearningHeatmapDataByUser(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取用户按日期分组的学习热力图数据
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') as date, " +
           "COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "WHERE lr.user.id = :userId " +
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d')")
    List<Object[]> findLearningHeatmapDataByUserGroupByDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取用户每日学习时长
     * 返回日期(yyyy-MM-dd格式)和总时长(秒)
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') as date, " +
           "SUM(lr.durationSeconds) as duration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') " +
           "ORDER BY date DESC")
    List<Object[]> findDailyLearningDurationByUserId(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取用户按活动类型分组的学习时长
     */
    @Query("SELECT lr.activityType, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.activityType")
    List<Object[]> findLearningDurationByActivityType(@Param("userId") Long userId);

    /**
     * 获取用户按课程分组的学习时长
     */
    @Query("SELECT lr.course.id, lr.course.title, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.course.id, lr.course.title " +
           "ORDER BY totalDuration DESC")
    List<Object[]> findLearningDurationByCourse(@Param("userId") Long userId);

    /**
     * 获取用户最近一次特定类型的学习活动
     */
    @Query("SELECT lr FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityType = :activityType " +
           "ORDER BY lr.activityStartTime DESC")
    List<LearningRecord> findLatestActivityByType(
            @Param("userId") Long userId,
            @Param("activityType") String activityType,
            Pageable pageable);

    /**
     * 获取用户今日总学习时长
     */
    @Query("SELECT SUM(lr.durationSeconds) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') = FUNCTION('DATE_FORMAT', CURRENT_DATE, '%Y-%m-%d') " +
           "AND lr.durationSeconds IS NOT NULL")
    Long findTodayLearningDuration(@Param("userId") Long userId);

    /**
     * 根据机构ID查询学习记录
     * 通过课程关联的机构进行查询
     */
    @Query("SELECT lr FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0") // 0 = NORMAL，只计算正常状态的课程
    List<LearningRecord> findByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 分页查询机构的学习记录
     */
    @Query("SELECT lr FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0") // 0 = NORMAL，只计算正常状态的课程
    Page<LearningRecord> findByInstitutionId(@Param("institutionId") Long institutionId, Pageable pageable);

    /**
     * 获取机构在指定时间范围内的学习记录
     */
    @Query("SELECT lr FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate")
    List<LearningRecord> findByInstitutionIdAndTimeRange(
            @Param("institutionId") Long institutionId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取机构每日学习统计数据
     * 返回日期、总时长和活动次数
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') as date, " +
           "SUM(lr.durationSeconds) as duration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') " +
           "ORDER BY date DESC")
    List<Object[]> findDailyLearningStatsByInstitutionId(
            @Param("institutionId") Long institutionId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取机构按课程分组的学习统计
     * 返回课程ID、课程标题、总时长和活动次数
     */
    @Query("SELECT lr.course.id, lr.course.title, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.course.id, lr.course.title " +
           "ORDER BY totalDuration DESC")
    List<Object[]> findLearningStatsByCourseForInstitution(@Param("institutionId") Long institutionId);

    /**
     * 获取机构按活动类型分组的学习统计
     */
    @Query("SELECT lr.activityType, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.activityType")
    List<Object[]> findLearningStatsByActivityTypeForInstitution(@Param("institutionId") Long institutionId);

    /**
     * 获取机构总学习时长
     */
    @Query("SELECT SUM(lr.durationSeconds) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL")
    Long findTotalLearningDurationByInstitution(@Param("institutionId") Long institutionId);

    /**
     * 获取机构今日总学习时长
     */
    @Query("SELECT SUM(lr.durationSeconds) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') = FUNCTION('DATE_FORMAT', CURRENT_DATE, '%Y-%m-%d') " +
           "AND lr.durationSeconds IS NOT NULL")
    Long findTodayLearningDurationByInstitution(@Param("institutionId") Long institutionId);

    /**
     * 获取机构用户学习人数
     * 返回在该机构课程中有学习记录的不同用户数量
     */
    @Query("SELECT COUNT(DISTINCT lr.user.id) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0") // 0 = NORMAL，只计算正常状态的课程
    Long countUniqueUsersByInstitution(@Param("institutionId") Long institutionId);

    /**
     * 获取机构中最活跃的用户
     * 返回用户ID、用户名、总学习时长和活动次数
     */
    @Query("SELECT lr.user.id, lr.user.username, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.institution.id = :institutionId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.user.id, lr.user.username " +
           "ORDER BY totalDuration DESC")
    List<Object[]> findMostActiveUsersByInstitution(
            @Param("institutionId") Long institutionId,
            Pageable pageable);

    /**
     * 获取课程每日学习统计
     * 返回日期、总时长和活动次数
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') as date, " +
           "SUM(lr.durationSeconds) as duration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') " +
           "ORDER BY date DESC")
    List<Object[]> findDailyLearningStatsByCourseId(
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取课程按活动类型分组的学习统计
     */
    @Query("SELECT lr.activityType, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.activityType")
    List<Object[]> findLearningStatsByActivityTypeForCourse(@Param("courseId") Long courseId);

    /**
     * 获取课程总学习时长
     */
    @Query("SELECT SUM(lr.durationSeconds) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL")
    Long findTotalLearningDurationByCourse(@Param("courseId") Long courseId);

    /**
     * 获取课程今日总学习时长
     */
    @Query("SELECT SUM(lr.durationSeconds) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND FUNCTION('DATE_FORMAT', lr.activityStartTime, '%Y-%m-%d') = FUNCTION('DATE_FORMAT', CURRENT_DATE, '%Y-%m-%d') " +
           "AND lr.durationSeconds IS NOT NULL")
    Long findTodayLearningDurationByCourse(@Param("courseId") Long courseId);

    /**
     * 获取课程学习人数
     * 返回在该课程中有学习记录的不同用户数量
     */
    @Query("SELECT COUNT(DISTINCT lr.user.id) FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0") // 0 = NORMAL，只计算正常状态的课程
    Long countUniqueUsersByCourse(@Param("courseId") Long courseId);

    /**
     * 获取课程中学生的学习统计
     * 返回用户ID、用户名、总学习时长和活动次数
     */
    @Query("SELECT lr.user.id, lr.user.username, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count, " +
           "MAX(lr.activityStartTime) as lastLearnTime " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.user.id, lr.user.username " +
           "ORDER BY totalDuration DESC")
    List<Object[]> findStudentLearningStatsByCourse(
            @Param("courseId") Long courseId,
            Pageable pageable);

    /**
     * 获取课程在指定时间范围内的学习记录
     */
    @Query("SELECT lr FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate")
    List<LearningRecord> findByCourseIdAndTimeRange(
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取用户在特定章节的总学习时长（秒）
     */
    @Query("SELECT SUM(lr.durationSeconds) FROM LearningRecord lr " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND lr.chapter.id = :chapterId " +
           "AND lr.durationSeconds IS NOT NULL")
    Integer getChapterLearningDuration(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId);

    /**
     * 检查特定小节是否有完成记录
     */
    boolean existsByUser_IdAndCourse_IdAndSection_IdAndActivityType(
            Long userId, Long courseId, Long sectionId, String activityType);

    /**
     * 计算用户在课程中已完成的不同小节数量
     */
    @Query("SELECT COUNT(DISTINCT lr.section.id) FROM LearningRecord lr " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND lr.activityType = :activityType")
    long countDistinctSectionIdByUser_IdAndCourse_IdAndActivityType(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("activityType") String activityType);

    /**
     * 获取课程学习活动热力图数据
     * 按星期几和小时分组统计学习活动次数
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', lr.activityStartTime) as weekday, " +
           "HOUR(lr.activityStartTime) as hour, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DAYOFWEEK', lr.activityStartTime), HOUR(lr.activityStartTime)")
    List<Object[]> findLearningHeatmapDataByCourse(
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取特定用户在特定课程的学习热力图数据
     * 按星期几和小时分组统计学习活动次数
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', lr.activityStartTime) as weekday, " +
           "HOUR(lr.activityStartTime) as hour, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.activityStartTime BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DAYOFWEEK', lr.activityStartTime), HOUR(lr.activityStartTime)")
    List<Object[]> findLearningHeatmapDataByUserAndCourse(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 获取特定用户在特定课程的活动类型统计
     */
    @Query("SELECT lr.activityType, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "AND lr.durationSeconds IS NOT NULL " +
           "GROUP BY lr.activityType")
    List<Object[]> findLearningStatsByActivityTypeForUserAndCourse(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId);

    /**
     * 获取特定用户在特定课程的基本信息
     * 返回用户名、总学习时长、活动次数和最后学习时间
     */
    @Query("SELECT lr.user.username, SUM(lr.durationSeconds) as totalDuration, COUNT(lr.id) as count, " +
           "MAX(lr.activityStartTime) as lastLearnTime " +
           "FROM LearningRecord lr " +
           "JOIN UserCourse uc ON lr.user.id = uc.user.id AND lr.course.id = uc.course.id " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND uc.status = 0 " + // 0 = NORMAL，只计算正常状态的课程
           "GROUP BY lr.user.username")
    Object[] findUserBasicInfoByUserIdAndCourseId(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId);

    /**
     * 获取用户在课程中的完成情况
     * 返回完成的章节数和小节数
     */
    @Query("SELECT COUNT(DISTINCT lr.chapter.id) as completedChapters, " +
           "COUNT(DISTINCT lr.section.id) as completedSections " +
           "FROM LearningRecord lr " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND lr.activityType = 'SECTION_COMPLETE'")
    Object[] findUserCourseCompletionInfo(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId);

    /**
     * 获取用户在课程中的测验情况
     * 返回完成的测验数和正确率
     */
    @Query("SELECT COUNT(lr.id) as completedQuizzes, " +
           "AVG(CASE WHEN lr.contextData LIKE '%\"isCorrect\":true%' THEN 1 ELSE 0 END) as accuracy " +
           "FROM LearningRecord lr " +
           "WHERE lr.user.id = :userId " +
           "AND lr.course.id = :courseId " +
           "AND lr.activityType = 'QUIZ_ATTEMPT'")
    Object[] findUserCourseQuizInfo(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId);

    /**
     * 获取机构统计数据
     * 返回机构ID、机构名称、Logo、学生数量、课程数量、总学习时长和活动次数
     */
    @Query("SELECT i.id, i.name, i.logo, " +
           "COUNT(DISTINCT lr.user.id) as studentCount, " +
           "COUNT(DISTINCT lr.course.id) as courseCount, " +
           "SUM(lr.durationSeconds) as totalDuration, " +
           "COUNT(lr.id) as activityCount " +
           "FROM Institution i " +
           "JOIN Course c ON c.institution.id = i.id " +
           "JOIN LearningRecord lr ON lr.course.id = c.id " +
           "GROUP BY i.id, i.name, i.logo")
    List<Object[]> findInstitutionStatistics();

    /**
     * 获取机构课程分布数据
     * 返回机构ID、机构名称、Logo和课程数量
     */
    @Query("SELECT i.id, i.name, i.logo, COUNT(c.id) as courseCount " +
           "FROM Institution i " +
           "JOIN Course c ON c.institution.id = i.id " +
           "GROUP BY i.id, i.name, i.logo")
    List<Object[]> findInstitutionCourseDistribution();
}