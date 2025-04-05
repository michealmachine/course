'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import type { Result } from '@/types/api';
import { InstitutionQuotaStatsVO } from '@/types/quota';
import { MediaTypeDistributionVO } from '@/types/media';

/**
 * 管理员配额统计服务接口
 */
export interface AdminQuotaService {
  /**
   * 获取所有机构的配额统计信息
   * @returns 所有机构的配额统计信息
   */
  getAllInstitutionsQuotaStats(): Promise<Result<InstitutionQuotaStatsVO>>;

  /**
   * 获取媒体类型分布统计
   * @param institutionId 机构ID（可选，不提供则统计所有机构）
   * @returns 媒体类型分布统计
   */
  getMediaTypeDistribution(institutionId?: number): Promise<Result<MediaTypeDistributionVO>>;

  /**
   * 获取各机构的媒体存储占用统计
   * @returns 各机构的媒体存储占用（机构名称 -> 占用字节数）
   */
  getInstitutionStorageUsage(): Promise<Result<Record<string, number>>>;
}

/**
 * 管理员配额统计服务实现
 */
class AdminQuotaServiceImpl implements AdminQuotaService {
  /**
   * 获取所有机构的配额统计信息
   * @returns 所有机构的配额统计信息
   */
  async getAllInstitutionsQuotaStats(): Promise<Result<InstitutionQuotaStatsVO>> {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionQuotaStatsVO>> = await request.get('/admin/quota/stats');
      return response.data as Result<InstitutionQuotaStatsVO>;
    } catch (error) {
      console.error('获取所有机构配额统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取媒体类型分布统计
   * @param institutionId 机构ID（可选，不提供则统计所有机构）
   * @returns 媒体类型分布统计
   */
  async getMediaTypeDistribution(institutionId?: number): Promise<Result<MediaTypeDistributionVO>> {
    try {
      const params = institutionId ? { institutionId } : undefined;
      const response: AxiosResponse<ApiResponse<MediaTypeDistributionVO>> = 
        await request.get('/admin/media/stats/type-distribution', { params });
      return response.data as Result<MediaTypeDistributionVO>;
    } catch (error) {
      console.error('获取媒体类型分布统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取各机构的媒体存储占用统计
   * @returns 各机构的媒体存储占用（机构名称 -> 占用字节数）
   */
  async getInstitutionStorageUsage(): Promise<Result<Record<string, number>>> {
    try {
      const response: AxiosResponse<ApiResponse<Record<string, number>>> = 
        await request.get('/admin/media/stats/institution-usage');
      return response.data as Result<Record<string, number>>;
    } catch (error) {
      console.error('获取各机构的媒体存储占用统计失败:', error);
      throw error;
    }
  }
}

// 导出服务实例
const adminQuotaService = new AdminQuotaServiceImpl();
export default adminQuotaService; 