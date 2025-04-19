'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse, Page } from '@/types/api';
import { DailyLearningStatVO, ActivityTypeStatVO } from './learning-service';
import {
  CourseStatisticsVO,
  StudentLearningVO,
  LearningDurationResponse
} from '@/types/institution-stats';
import { LearningHeatmapVO, LearningProgressTrendVO } from '@/types/learning-stats';

/**
 * 管理员学习统计服务接口
 */
export interface AdminLearningStatsService {
  /**
   * 获取平台学习时长统计
   * @returns 平台学习时长统计
   */
  getLearningDuration(): Promise<LearningDurationResponse>;

  /**
   * 获取平台每日学习统计
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  getDailyStatistics(startDate?: string, endDate?: string): Promise<DailyLearningStatVO[]>;

  /**
   * 获取平台活动类型统计
   * @returns 活动类型统计列表
   */
  getActivityTypeStatistics(): Promise<ActivityTypeStatVO[]>;

  /**
   * 获取所有课程学习统计
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  getAllCourseStatistics(page?: number, size?: number): Promise<Page<CourseStatisticsVO>>;

  /**
   * 获取机构课程学习统计
   * @param institutionId 机构ID
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  getInstitutionCourseStatistics(institutionId: number, page?: number, size?: number): Promise<Page<CourseStatisticsVO>>;

  /**
   * 获取课程学习统计概览
   * @param courseId 课程ID
   * @returns 课程学习统计数据
   */
  getCourseStatisticsOverview(courseId: number | string): Promise<CourseStatisticsVO>;

  /**
   * 获取课程每日学习统计
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  getCourseDailyStatistics(courseId: number | string, startDate?: string, endDate?: string): Promise<DailyLearningStatVO[]>;

  /**
   * 获取课程活动类型统计
   * @param courseId 课程ID
   * @returns 活动类型统计列表
   */
  getCourseActivityTypeStatistics(courseId: number | string): Promise<ActivityTypeStatVO[]>;

  /**
   * 获取课程学生学习统计
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页数量
   * @returns 学生学习统计分页
   */
  getCourseStudentStatistics(courseId: number | string, page?: number, size?: number): Promise<Page<StudentLearningVO>>;

  /**
   * 获取课程学习热力图数据
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 学习热力图数据
   */
  getCourseLearningHeatmap(courseId: number | string, startDate?: string, endDate?: string): Promise<LearningHeatmapVO>;

  /**
   * 获取课程学习进度趋势
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 学习进度趋势数据
   */
  getCourseLearningProgressTrend(courseId: number | string, startDate?: string, endDate?: string): Promise<LearningProgressTrendVO>;
}

/**
 * 管理员学习统计服务实现
 */
class AdminLearningStatsServiceImpl implements AdminLearningStatsService {
  /**
   * 获取平台学习时长统计
   * @returns 平台学习时长统计
   */
  async getLearningDuration(): Promise<LearningDurationResponse> {
    try {
      console.log('开始获取平台学习时长统计');
      const response: AxiosResponse<ApiResponse<LearningDurationResponse>> =
        await request.get('/admin/learning-statistics/duration');

      console.log('获取平台学习时长统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取平台学习时长统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取平台每日学习统计
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  async getDailyStatistics(startDate?: string, endDate?: string): Promise<DailyLearningStatVO[]> {
    try {
      console.log(`开始获取平台每日学习统计, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> =
        await request.get('/admin/learning-statistics/daily', {
          params: { startDate, endDate }
        });

      console.log('获取平台每日学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取平台每日学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取平台活动类型统计
   * @returns 活动类型统计列表
   */
  async getActivityTypeStatistics(): Promise<ActivityTypeStatVO[]> {
    try {
      console.log('开始获取平台活动类型统计');
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> =
        await request.get('/admin/learning-statistics/activity-types');

      console.log('获取平台活动类型统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取平台活动类型统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取所有课程学习统计
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  async getAllCourseStatistics(page: number = 0, size: number = 10): Promise<Page<CourseStatisticsVO>> {
    try {
      console.log(`开始获取所有课程学习统计, 页码: ${page}, 每页数量: ${size}`);
      const response: AxiosResponse<ApiResponse<Page<CourseStatisticsVO>>> =
        await request.get('/admin/learning-statistics/courses', {
          params: { page, size }
        });

      console.log('获取所有课程学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取所有课程学习统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取机构课程学习统计
   * @param institutionId 机构ID
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  async getInstitutionCourseStatistics(
    institutionId: number,
    page: number = 0,
    size: number = 10
  ): Promise<Page<CourseStatisticsVO>> {
    try {
      console.log(`开始获取机构课程学习统计, 机构ID: ${institutionId}, 页码: ${page}, 每页数量: ${size}`);
      const response: AxiosResponse<ApiResponse<Page<CourseStatisticsVO>>> =
        await request.get(`/admin/learning-statistics/institutions/${institutionId}/courses`, {
          params: { page, size }
        });

      console.log('获取机构课程学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构课程学习统计失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  }

  /**
   * 获取课程学习统计概览
   * @param courseId 课程ID
   * @returns 课程学习统计数据
   */
  async getCourseStatisticsOverview(courseId: string | number): Promise<CourseStatisticsVO> {
    try {
      console.log(`开始获取课程学习统计概览, 课程ID: ${courseId}`);
      const response: AxiosResponse<ApiResponse<CourseStatisticsVO>> =
        await request.get(`/admin/learning-statistics/courses/${courseId}/overview`);

      console.log('获取课程学习统计概览成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习统计概览失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }

  /**
   * 获取课程每日学习统计
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  async getCourseDailyStatistics(
    courseId: string | number,
    startDate?: string,
    endDate?: string
  ): Promise<DailyLearningStatVO[]> {
    try {
      console.log(`开始获取课程每日学习统计, 课程ID: ${courseId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> =
        await request.get(`/admin/learning-statistics/courses/${courseId}/daily`, {
          params: { startDate, endDate }
        });

      console.log('获取课程每日学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程每日学习统计失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }

  /**
   * 获取课程活动类型统计
   * @param courseId 课程ID
   * @returns 活动类型统计列表
   */
  async getCourseActivityTypeStatistics(courseId: string | number): Promise<ActivityTypeStatVO[]> {
    try {
      console.log(`开始获取课程活动类型统计, 课程ID: ${courseId}`);
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> =
        await request.get(`/admin/learning-statistics/courses/${courseId}/activity-types`);

      console.log('获取课程活动类型统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程活动类型统计失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }

  /**
   * 获取课程学生学习统计
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页数量
   * @returns 学生学习统计分页
   */
  async getCourseStudentStatistics(
    courseId: string | number,
    page: number = 0,
    size: number = 10
  ): Promise<Page<StudentLearningVO>> {
    try {
      console.log(`开始获取课程学生学习统计, 课程ID: ${courseId}, 页码: ${page}, 每页数量: ${size}`);
      const response: AxiosResponse<ApiResponse<Page<StudentLearningVO>>> =
        await request.get(`/admin/learning-statistics/courses/${courseId}/students`, {
          params: { page, size }
        });

      console.log('获取课程学生学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学生学习统计失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }

  /**
   * 获取课程学习热力图数据
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 学习热力图数据
   */
  async getCourseLearningHeatmap(
    courseId: string | number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningHeatmapVO> {
    try {
      console.log(`开始获取课程学习热力图数据, 课程ID: ${courseId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<LearningHeatmapVO>> =
        await request.get(`/admin/learning-statistics/courses/${courseId}/heatmap`, {
          params: { startDate, endDate }
        });

      console.log('获取课程学习热力图数据成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习热力图数据失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }

  /**
   * 获取课程学习进度趋势
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 学习进度趋势数据
   */
  async getCourseLearningProgressTrend(
    courseId: string | number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningProgressTrendVO> {
    try {
      console.log(`开始获取课程学习进度趋势, 课程ID: ${courseId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<LearningProgressTrendVO>> =
        await request.get(`/admin/learning-statistics/courses/${courseId}/progress-trend`, {
          params: { startDate, endDate }
        });

      console.log('获取课程学习进度趋势成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习进度趋势失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }
}

// 导出服务实例
const adminLearningStatsService = new AdminLearningStatsServiceImpl();
export default adminLearningStatsService;
