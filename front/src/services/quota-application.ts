'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import { 
  QuotaApplicationVO, 
  QuotaApplicationStatus, 
  QuotaType,
  QuotaApplicationPage,
  QuotaApplicationQueryParams
} from '@/types/quota';

/**
 * 配额申请服务 - 处理存储配额申请相关接口
 */
const quotaApplicationService = {
  /**
   * 获取配额申请列表（管理员用）
   * @param params 查询参数
   * @returns 分页申请列表
   */
  getAllApplications: async (params: QuotaApplicationQueryParams): Promise<QuotaApplicationPage> => {
    try {
      const { status, pageNum = 1, pageSize = 10 } = params;
      let url = `/v1/quota-applications/admin?pageNum=${pageNum}&pageSize=${pageSize}`;
      
      if (status !== undefined) {
        url += `&status=${status}`;
      }
      
      const response: AxiosResponse<ApiResponse<QuotaApplicationPage>> = 
        await request.get<QuotaApplicationPage>(url);
      return response.data.data;
    } catch (error) {
      console.error('获取配额申请列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取配额申请详情
   * @param id 申请ID
   * @returns 申请详情
   */
  getApplicationDetail: async (id: number): Promise<QuotaApplicationVO> => {
    try {
      const response: AxiosResponse<ApiResponse<QuotaApplicationVO>> = 
        await request.get<QuotaApplicationVO>(`/v1/quota-applications/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取申请详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 批准配额申请
   * @param id 申请ID
   */
  approveApplication: async (id: number): Promise<void> => {
    try {
      await request.post(`/v1/quota-applications/approve/${id}`);
    } catch (error) {
      console.error(`批准申请失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 拒绝配额申请
   * @param id 申请ID
   * @param reason 拒绝原因
   */
  rejectApplication: async (id: number, reason: string): Promise<void> => {
    try {
      await request.post(`/v1/quota-applications/${id}/reject?reason=${encodeURIComponent(reason)}`);
    } catch (error) {
      console.error(`拒绝申请失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建配额申请（机构用户用）
   * @param data 申请数据
   */
  createApplication: async (data: {
    quotaType: QuotaType;
    requestedBytes: number;
    reason: string;
  }): Promise<void> => {
    try {
      await request.post('/v1/quota-applications/apply', data);
    } catch (error) {
      console.error('创建配额申请失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构用户自己的申请列表
   * @param params 查询参数
   * @returns 分页申请列表
   */
  getMyApplications: async (params: {
    pageNum: number;
    pageSize: number;
  }): Promise<QuotaApplicationPage> => {
    try {
      const { pageNum = 1, pageSize = 10 } = params;
      const response: AxiosResponse<ApiResponse<QuotaApplicationPage>> = 
        await request.get<QuotaApplicationPage>(
          `/v1/quota-applications/user?pageNum=${pageNum}&pageSize=${pageSize}`
        );
      return response.data.data;
    } catch (error) {
      console.error('获取我的申请列表失败:', error);
      throw error;
    }
  },
};

export default quotaApplicationService; 