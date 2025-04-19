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
 * 管理员学习统计服务接口
 * 提供管理员查看全平台学习数据统计相关功能
 */
public interface AdminLearningStatisticsService {

    /**
     * 获取所有课程学习统计
     *
     * @param pageable 分页参数
     * @return 课程学习统计分页
     */
    Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> getAllCourseStatistics(Pageable pageable);

    /**
     * 获取特定机构的课程学习统计
     *
     * @param institutionId 机构ID
     * @param pageable 分页参数
     * @return 课程学习统计分页
     */
    Page<InstitutionLearningStatisticsVO.CourseStatisticsVO> getInstitutionCourseStatistics(Long institutionId, Pageable pageable);

    /**
     * 获取课程学习统计概览
     *
     * @param courseId 课程ID
     * @return 课程学习统计数据
     */
    InstitutionLearningStatisticsVO.CourseStatisticsVO getCourseLearningStatistics(Long courseId);

    /**
     * 获取课程每日学习统计
     *
     * @param courseId 课程ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日学习统计列表
     */
    List<DailyLearningStatVO> getCourseDailyLearningStats(Long courseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取课程活动类型统计
     *
     * @param courseId 课程ID
     * @return 活动类型统计列表
     */
    List<ActivityTypeStatVO> getCourseActivityTypeStats(Long courseId);

    /**
     * 获取课程学生学习统计
     *
     * @param courseId 课程ID
     * @param pageable 分页参数
     * @return 学生学习统计分页
     */
    Page<InstitutionLearningStatisticsVO.StudentLearningVO> getCourseStudentStatistics(Long courseId, Pageable pageable);

    /**
     * 获取课程学习热力图数据
     *
     * @param courseId 课程ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习热力图数据
     */
    LearningHeatmapVO getCourseLearningHeatmap(Long courseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取课程学习进度趋势
     *
     * @param courseId 课程ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习进度趋势数据
     */
    LearningProgressTrendVO getCourseLearningProgressTrend(Long courseId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取平台总学习时长
     *
     * @return 总学习时长（秒）
     */
    Long getTotalLearningDuration();

    /**
     * 获取平台今日学习时长
     *
     * @return 今日学习时长（秒）
     */
    Long getTodayLearningDuration();

    /**
     * 获取平台学习人数
     *
     * @return 学习人数
     */
    Long getTotalLearnerCount();

    /**
     * 获取平台活动类型统计
     *
     * @return 活动类型统计列表
     */
    List<ActivityTypeStatVO> getAllActivityTypeStats();

    /**
     * 获取平台每日学习统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日学习统计列表
     */
    List<DailyLearningStatVO> getAllDailyLearningStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取机构学习统计排行
     *
     * @param sortBy 排序字段(studentCount/courseCount/totalDuration/activityCount)
     * @param limit 数量限制
     * @return 机构学习统计排行列表
     */
    List<InstitutionLearningStatisticsVO.InstitutionStatisticsVO> getInstitutionRanking(String sortBy, Integer limit);

    /**
     * 获取课程学习统计排行
     *
     * @param sortBy 排序字段(learnerCount/totalDuration/activityCount/favoriteCount)
     * @param institutionId 机构ID（可选）
     * @param limit 数量限制
     * @return 课程学习统计排行列表
     */
    List<InstitutionLearningStatisticsVO.CourseStatisticsVO> getCourseRanking(String sortBy, Long institutionId, Integer limit);

    /**
     * 获取机构课程占比统计
     *
     * @return 机构课程占比统计
     */
    List<InstitutionLearningStatisticsVO.InstitutionCourseDistributionVO> getInstitutionCourseDistribution();

    /**
     * 获取特定用户在特定课程的学习热力图数据
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习热力图数据
     */
    LearningHeatmapVO getUserCourseLearningHeatmap(Long courseId, Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取特定用户在特定课程的学习进度趋势
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 学习进度趋势数据
     */
    LearningProgressTrendVO getUserCourseLearningProgressTrend(Long courseId, Long userId, LocalDate startDate, LocalDate endDate);
}
