'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { 
  IncomeTrendVO, 
  OrderStatusDistributionVO, 
  CourseIncomeRankingVO, 
  AdminCourseIncomeRankingVO, 
  PlatformIncomeStatsVO 
} from '@/types/order-stats';
import { AxiosResponse } from 'axios';

/**
 * 订单统计服务 - 处理订单统计相关API
 */
const orderStatsService = {
  /**
   * 获取机构收入趋势
   */
  getInstitutionIncomeTrend: async (timeRange: string = '30d', groupBy: string = 'day'): Promise<IncomeTrendVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<IncomeTrendVO[]>> = await request.get(
        `/orders/stats/institution/income-trend?timeRange=${timeRange}&groupBy=${groupBy}`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取机构收入趋势失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构订单状态分布
   */
  getInstitutionOrderStatusDistribution: async (): Promise<OrderStatusDistributionVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderStatusDistributionVO[]>> = await request.get(
        '/orders/stats/institution/status-distribution'
      );
      return response.data.data;
    } catch (error) {
      console.error('获取机构订单状态分布失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构课程收入排行
   */
  getInstitutionCourseIncomeRanking: async (limit: number = 10): Promise<CourseIncomeRankingVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<CourseIncomeRankingVO[]>> = await request.get(
        `/orders/stats/institution/course-income-ranking?limit=${limit}`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取机构课程收入排行失败:', error);
      throw error;
    }
  },

  /**
   * 获取平台收入趋势（管理员）
   */
  getPlatformIncomeTrend: async (timeRange: string = '30d', groupBy: string = 'day'): Promise<IncomeTrendVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<IncomeTrendVO[]>> = await request.get(
        `/admin/orders/stats/income-trend?timeRange=${timeRange}&groupBy=${groupBy}`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取平台收入趋势失败:', error);
      throw error;
    }
  },

  /**
   * 获取平台订单状态分布（管理员）
   */
  getPlatformOrderStatusDistribution: async (): Promise<OrderStatusDistributionVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<OrderStatusDistributionVO[]>> = await request.get(
        '/admin/orders/stats/status-distribution'
      );
      return response.data.data;
    } catch (error) {
      console.error('获取平台订单状态分布失败:', error);
      throw error;
    }
  },

  /**
   * 获取平台课程收入排行（管理员）
   */
  getPlatformCourseIncomeRanking: async (limit: number = 10): Promise<AdminCourseIncomeRankingVO[]> => {
    try {
      const response: AxiosResponse<ApiResponse<AdminCourseIncomeRankingVO[]>> = await request.get(
        `/admin/orders/stats/course-income-ranking?limit=${limit}`
      );
      return response.data.data;
    } catch (error) {
      console.error('获取平台课程收入排行失败:', error);
      throw error;
    }
  },

  /**
   * 获取平台收入统计（管理员）
   */
  getPlatformIncomeStats: async (startDate?: string, endDate?: string): Promise<PlatformIncomeStatsVO> => {
    try {
      let url = '/admin/orders/stats/income-stats';
      const params = [];
      
      if (startDate) {
        params.push(`startDate=${encodeURIComponent(startDate)}`);
      }
      
      if (endDate) {
        params.push(`endDate=${encodeURIComponent(endDate)}`);
      }
      
      if (params.length > 0) {
        url += `?${params.join('&')}`;
      }
      
      const response: AxiosResponse<ApiResponse<PlatformIncomeStatsVO>> = await request.get(url);
      return response.data.data;
    } catch (error) {
      console.error('获取平台收入统计失败:', error);
      throw error;
    }
  }
};

export default orderStatsService;
