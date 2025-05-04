'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';

/**
 * 管理员服务 - 处理管理员相关API
 */
const adminService = {
  /**
   * 手动触发学习记录聚合
   * 将Redis中的学习记录同步到数据库
   */
  triggerLearningRecordAggregation: async (): Promise<void> => {
    try {
      const response: AxiosResponse<ApiResponse<void>> =
        await request.post('/admin/learning-records/aggregate');
      return response.data.data;
    } catch (error) {
      console.error('手动触发学习记录聚合失败:', error);
      throw error;
    }
  },

  /**
   * 清除统计缓存
   */
  clearStatisticsCache: async (): Promise<void> => {
    try {
      const response: AxiosResponse<ApiResponse<void>> =
        await request.post('/admin/learning-statistics/cache/clear');
      return response.data.data;
    } catch (error) {
      console.error('清除统计缓存失败:', error);
      throw error;
    }
  }
};

export default adminService;
