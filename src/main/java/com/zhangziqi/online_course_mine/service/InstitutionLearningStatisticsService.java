package com.zhangziqi.online_course_mine.service;

import com.zhangziqi.online_course_mine.model.vo.ActivityTypeStatVO;
import com.zhangziqi.online_course_mine.model.vo.DailyLearningStatVO;
import com.zhangziqi.online_course_mine.model.vo.InstitutionLearningStatisticsVO;
import com.zhangziqi.online_course_mine.model.vo.LearningHeatmapVO;
import com.zhangziqi.online_course_mine.model.vo.LearningProgressTrendVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 机构学习统计服务接口
 * 提供机构学习数据统计相关功能
 */
public interface InstitutionLearningStatisticsService {

    /**
     * 获取机构学习统计
     *
     * @param institutionId 机构ID
     * @return 机构学习统计数据
     */
    InstitutionLearningStatisticsVO getInstitutionLearningStatistics(Long institutionId);

    /**
     * 获取机构每日学习统计
     *
     * @param institutionId 机构ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日学习统计列表
     */
    List<DailyLearningStatVO> getInstitutionDailyLearningStats(Long institutionId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取机构活动类型统计
     *
     * @param institutionId 机构ID
     * @return 活动类型统计列表
     */
    List<ActivityTypeStatVO> getInstitutionActivityTypeStats(Long institutionId);

    /**
     * 获取机构课程学习统计
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 课程学习统计分页
     */
    Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> getInstitutionCourseStatistics(
            Long institutionId, Pageable pageable);

    /**
     * 获取机构最活跃用户统计
     *
     * @param institutionId 机构ID
     * @param limit 用户数量限制
     * @return 活跃用户统计列表
     */
    List<InstitutionLearningStatisticsVO.ActiveUserVO> getMostActiveUsers(Long institutionId, int limit);

    /**
     * 获取机构今日学习时长
     *
     * @param institutionId 机构ID
     * @return 今日学习时长（秒）
     */
    Long getInstitutionTodayLearningDuration(Long institutionId);

    /**
     * 获取机构总学习时长
     *
     * @param institutionId 机构ID
     * @return 总学习时长（秒）
     */
    Number getInstitutionTotalLearningDuration(Long institutionId);

    /**
     * 获取机构学习人数
     *
     * @param institutionId 机构ID
     * @return 学习人数
     */
    Number getInstitutionLearnerCount(Long institutionId);

    /**
     * 获取课程学习统计概览
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @return 课程学习统计数据
     */
    InstitutionLearningStatisticsVO.CourseStatisticsVO getCourseLearningStatistics(Long institutionId, Long courseId);

    /**
     * 获取课程每日学习统计
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日学习统计列表
     */
    List<DailyLearningStatVO> getCourseDailyLearningStats(Long institutionId, Long courseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取课程活动类型统计
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @return 活动类型统计列表
     */
    List<ActivityTypeStatVO> getCourseActivityTypeStats(Long institutionId, Long courseId);

    /**
     * 获取课程学生学习统计
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 学生学习统计分页
     */
    Page<InstitutionLearningStatisticsVO.StudentLearningVO> getCourseStudentStatistics(
            Long institutionId, Long courseId, Pageable pageable);

    /**
     * 获取课程今日学习时长
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @return 今日学习时长（秒）
     */
    Long getCourseTodayLearningDuration(Long institutionId, Long courseId);

    /**
     * 获取课程总学习时长
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @return 总学习时长（秒）
     */
    Long getCourseTotalLearningDuration(Long institutionId, Long courseId);

    /**
     * 获取课程学习人数
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @return 学习人数
     */
    Long getCourseLearnerCount(Long institutionId, Long courseId);

    /**
     * 获取课程学习热力图数据
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习热力图数据
     */
    LearningHeatmapVO getCourseLearningHeatmap(Long institutionId, Long courseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取课程学习进度趋势
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习进度趋势数据
     */
    LearningProgressTrendVO getCourseLearningProgressTrend(Long institutionId, Long courseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取特定用户在特定课程的学习热力图数据
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习热力图数据
     */
    LearningHeatmapVO getUserCourseLearningHeatmap(Long institutionId, Long courseId, Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取特定用户在特定课程的学习进度趋势
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习进度趋势数据
     */
    LearningProgressTrendVO getUserCourseLearningProgressTrend(Long institutionId, Long courseId, Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取特定用户在特定课程的活动类型统计
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return 活动类型统计列表
     */
    List<ActivityTypeStatVO> getUserCourseActivityTypeStats(Long institutionId, Long courseId, Long userId);

    /**
     * 获取特定用户在特定课程的详细学习统计
     *
     * @param institutionId 机构ID
     * @param courseId 课程ID
     * @param userId 用户ID
     * @return 用户课程学习统计详情
     */
    InstitutionLearningStatisticsVO.StudentLearningDetailVO getUserCourseLearningDetail(Long institutionId, Long courseId, Long userId);
}