'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse } from '@/types/api';
import { DailyLearningStatVO, ActivityTypeStatVO } from './learning-service';
import {
  InstitutionLearningStatisticsVO,
  CourseStatisticsVO,
  ActiveUserVO
} from '@/types/institution-stats';
import {
  IncomeTrendVO,
  OrderStatusDistributionVO,
  CourseIncomeRankingVO
} from '@/types/order-stats';

/**
 * 管理员机构统计服务
 * 提供管理员查看机构统计数据相关API调用
 */
const adminInstitutionStatsService = {
  /**
   * 获取机构学习统计概览
   * @param institutionId 机构ID
   * @returns 机构学习统计数据
   */
  getStatisticsOverview: async (institutionId: number): Promise<InstitutionLearningStatisticsVO> => {
    try {
      console.log(`开始获取机构学习统计概览, 机构ID: ${institutionId}`);
      const response: AxiosResponse<ApiResponse<InstitutionLearningStatisticsVO>> =
        await request.get(`/admin/institutions/${institutionId}/learning-statistics/overview`);

      console.log('获取机构学习统计概览成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构学习统计概览失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构每日学习统计
   * @param institutionId 机构ID
   * @param startDate 开始日期（YYYY-MM-DD格式）
   * @param endDate 结束日期（YYYY-MM-DD格式）
   * @returns 每日学习统计列表
   */
  getDailyStatistics: async (
    institutionId: number,
    startDate?: string,
    endDate?: string
  ): Promise<DailyLearningStatVO[]> => {
    try {
      console.log(`开始获取机构每日学习统计, 机构ID: ${institutionId}, 开始日期: ${startDate}, 结束日期: ${endDate}`);
      const response: AxiosResponse<ApiResponse<DailyLearningStatVO[]>> =
        await request.get(`/admin/institutions/${institutionId}/learning-statistics/daily`, {
          params: { startDate, endDate }
        });

      console.log('获取机构每日学习统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构每日学习统计失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构活动类型统计
   * @param institutionId 机构ID
   * @returns 活动类型统计列表
   */
  getActivityTypeStatistics: async (institutionId: number): Promise<ActivityTypeStatVO[]> => {
    try {
      console.log(`开始获取机构活动类型统计, 机构ID: ${institutionId}`);
      const response: AxiosResponse<ApiResponse<ActivityTypeStatVO[]>> =
        await request.get(`/admin/institutions/${institutionId}/learning-statistics/activity-types`);

      console.log('获取机构活动类型统计成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构活动类型统计失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构收入趋势
   * @param institutionId 机构ID
   * @param timeRange 时间范围（7d, 30d, 90d）
   * @param groupBy 分组方式（day, week, month）
   * @returns 收入趋势数据列表
   */
  getIncomeTrend: async (
    institutionId: number,
    timeRange: string = '30d',
    groupBy: string = 'day'
  ): Promise<IncomeTrendVO[]> => {
    try {
      console.log(`开始获取机构收入趋势, 机构ID: ${institutionId}, 时间范围: ${timeRange}, 分组方式: ${groupBy}`);
      const response: AxiosResponse<ApiResponse<IncomeTrendVO[]>> =
        await request.get(`/admin/institutions/${institutionId}/income-trend`, {
          params: { timeRange, groupBy }
        });

      console.log('获取机构收入趋势成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构收入趋势失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构订单状态分布
   * @param institutionId 机构ID
   * @returns 订单状态分布数据列表
   */
  getOrderStatusDistribution: async (institutionId: number): Promise<OrderStatusDistributionVO[]> => {
    try {
      console.log(`开始获取机构订单状态分布, 机构ID: ${institutionId}`);
      const response: AxiosResponse<ApiResponse<OrderStatusDistributionVO[]>> =
        await request.get(`/admin/institutions/${institutionId}/order-status-distribution`);

      console.log('获取机构订单状态分布成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构订单状态分布失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  },

  /**
   * 获取机构课程收入排行
   * @param institutionId 机构ID
   * @param limit 返回数量限制
   * @returns 课程收入排行数据列表
   */
  getCourseIncomeRanking: async (
    institutionId: number,
    limit: number = 5
  ): Promise<CourseIncomeRankingVO[]> => {
    try {
      console.log(`开始获取机构课程收入排行, 机构ID: ${institutionId}, 限制: ${limit}`);
      const response: AxiosResponse<ApiResponse<CourseIncomeRankingVO[]>> =
        await request.get(`/admin/institutions/${institutionId}/course-income-ranking`, {
          params: { limit }
        });

      console.log('获取机构课程收入排行成功:', response.data);
      return response.data.data;
    } catch (error) {
      console.error(`获取机构课程收入排行失败, 机构ID: ${institutionId}:`, error);
      throw error;
    }
  }
};

export default adminInstitutionStatsService;
