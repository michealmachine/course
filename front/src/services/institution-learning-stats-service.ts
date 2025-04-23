'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse, Page } from '@/types/api';
import { DailyLearningStatVO, ActivityTypeStatVO } from './learning-service';
import {
  InstitutionLearningStatisticsVO,
  CourseStatisticsVO,
  ActiveUserVO,
  StudentLearningVO,
  LearningDurationResponse
} from '@/types/institution-stats';
import { LearningHeatmapVO, LearningProgressTrendVO } from '@/types/learning-stats';

/**
 * 机构学习统计服务
 * 提供机构学习数据统计相关API调用
 */
const institutionLearningStatsService = {
  /**
   * 获取机构学习统计概览
   * @returns 机构学习统计数据
   */
  getStatisticsOverview: async (): Promise<InstitutionLearningStatisticsVO> => {
    try {
      console.log('开始获取机构学习统计概览');
      const response: AxiosResponse<ApiResponse<InstitutionLearningStatisticsVO>> =
        await request.get('/institutions/learning-statistics/overview');

      console.log('获取机构学习统计概览成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取机构学习统计概览失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构每日学习统计
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  getDailyStatistics: async (
    startDate?: string,
    endDate?: string
  ): Promise<DailyLearningStatVO[]> => {
    try {
      console.log(`开始获取机构每日学习统计, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> =
        await request.get('/institutions/learning-statistics/daily', {
          params: { startDate, endDate }
        });

      console.log('获取机构每日学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取机构每日学习统计失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构活动类型统计
   * @returns 活动类型统计列表
   */
  getActivityTypeStatistics: async (): Promise<ActivityTypeStatVO[]> => {
    try {
      console.log('开始获取机构活动类型统计');
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> =
        await request.get('/institutions/learning-statistics/activity-types');

      console.log('获取机构活动类型统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取机构活动类型统计失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构课程学习统计
   * @param page 页码
   * @param size 每页数量
   * @returns 课程学习统计分页
   */
  getCourseStatistics: async (
    page: number = 0,
    size: number = 10
  ): Promise<Page<CourseStatisticsVO>> => {
    try {
      console.log(`开始获取机构课程学习统计, 页码: ${page}, 每页数量: ${size}`);
      const response: AxiosResponse<ApiResponse<Page<CourseStatisticsVO>>> =
        await request.get('/institutions/learning-statistics/courses', {
          params: { page, size }
        });

      console.log('获取机构课程学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取机构课程学习统计失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构最活跃用户
   * @param limit 用户数量限制
   * @returns 活跃用户统计列表
   */
  getMostActiveUsers: async (limit: number = 10): Promise<ActiveUserVO[]> => {
    try {
      console.log(`开始获取机构最活跃用户, 限制: ${limit}`);
      const response: AxiosResponse<ApiResponse<ActiveUserVO[]>> =
        await request.get('/institutions/learning-statistics/active-users', {
          params: { limit }
        });

      console.log('获取机构最活跃用户成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取机构最活跃用户失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构学习时长统计
   * @returns 机构学习时长统计
   */
  getLearningDuration: async (): Promise<LearningDurationResponse> => {
    try {
      console.log('开始获取机构学习时长统计');
      const response: AxiosResponse<ApiResponse<LearningDurationResponse>> =
        await request.get('/institutions/learning-statistics/duration');

      console.log('获取机构学习时长统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error('获取机构学习时长统计失败:', error);
      throw error;
    }
  },

  /**
   * 获取课程学习统计概览
   * @param courseId 课程ID
   * @returns 课程学习统计数据
   */
  getCourseStatisticsOverview: async (courseId: string | number): Promise<CourseStatisticsVO> => {
    try {
      console.log(`开始获取课程学习统计概览, 课程ID: ${courseId}`);
      const response: AxiosResponse<ApiResponse<CourseStatisticsVO>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/overview`);

      console.log('获取课程学习统计概览成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习统计概览失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程每日学习统计
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  getCourseDailyStatistics: async (
    courseId: string | number,
    startDate?: string,
    endDate?: string
  ): Promise<DailyLearningStatVO[]> => {
    try {
      console.log(`开始获取课程每日学习统计, 课程ID: ${courseId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/daily`, {
          params: { startDate, endDate }
        });

      console.log('获取课程每日学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程每日学习统计失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程活动类型统计
   * @param courseId 课程ID
   * @returns 活动类型统计列表
   */
  getCourseActivityTypeStatistics: async (courseId: string | number): Promise<ActivityTypeStatVO[]> => {
    try {
      console.log(`开始获取课程活动类型统计, 课程ID: ${courseId}`);
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/activity-types`);

      console.log('获取课程活动类型统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程活动类型统计失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程学生学习统计
   * @param courseId 课程ID
   * @param page 页码
   * @param size 每页数量
   * @returns 学生学习统计分页
   */
  getCourseStudentStatistics: async (
    courseId: string | number,
    page: number = 0,
    size: number = 10
  ): Promise<Page<StudentLearningVO>> => {
    try {
      console.log(`开始获取课程学生学习统计, 课程ID: ${courseId}, 页码: ${page}, 每页数量: ${size}`);
      const response: AxiosResponse<ApiResponse<Page<StudentLearningVO>>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/students`, {
          params: { page, size }
        });

      console.log('获取课程学生学习统计成功:', response.data);

      // 处理空数据情况
      if (!response.data.data || !response.data.data.content) {
        console.warn('服务器返回的学生学习统计数据为空');
        return {
          content: [],
          totalElements: 0,
          totalPages: 1,
          size: size,
          number: page,
          numberOfElements: 0,
          empty: true,
          first: true,
          last: true
        };
      }

      return response.data.data;
    } catch (error) {
      console.error(`获取课程学生学习统计失败, 课程ID: ${courseId}:`, error);
      // 返回空分页对象而不是抛出异常
      return {
        content: [],
        totalElements: 0,
        totalPages: 1,
        size: size,
        number: page,
        numberOfElements: 0,
        empty: true,
        first: true,
        last: true
      };
    }
  },

  /**
   * 获取课程学习时长统计
   * @param courseId 课程ID
   * @returns 课程学习时长统计
   */
  getCourseLearningDuration: async (courseId: string | number): Promise<LearningDurationResponse> => {
    try {
      console.log(`开始获取课程学习时长统计, 课程ID: ${courseId}`);
      const response: AxiosResponse<ApiResponse<LearningDurationResponse>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/duration`);

      console.log('获取课程学习时长统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习时长统计失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程学习热力图数据
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 学习热力图数据
   */
  getCourseLearningHeatmap: async (
    courseId: string | number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningHeatmapVO> => {
    try {
      console.log(`开始获取课程学习热力图数据, 课程ID: ${courseId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<LearningHeatmapVO>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/heatmap`, {
          params: { startDate, endDate }
        });

      console.log('获取课程学习热力图数据成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习热力图数据失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  },

  /**
   * 获取课程学习进度趋势
   * @param courseId 课程ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 学习进度趋势数据
   */
  getCourseLearningProgressTrend: async (
    courseId: string | number,
    startDate?: string,
    endDate?: string
  ): Promise<LearningProgressTrendVO> => {
    try {
      console.log(`开始获取课程学习进度趋势, 课程ID: ${courseId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<LearningProgressTrendVO>> =
        await request.get(`/institutions/learning-statistics/courses/${courseId}/progress-trend`, {
          params: { startDate, endDate }
        });

      console.log('获取课程学习进度趋势成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取课程学习进度趋势失败, 课程ID: ${courseId}:`, error);
      throw error;
    }
  }
};

export default institutionLearningStatsService;