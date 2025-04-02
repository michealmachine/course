'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import type { Result } from '@/types/api';
import { InstitutionQuotaStatsVO } from '@/types/quota';

/**
 * 管理员配额统计服务接口
 */
export interface AdminQuotaService {
  /**
   * 获取所有机构的配额统计信息
   * @returns 所有机构的配额统计信息
   */
  getAllInstitutionsQuotaStats(): Promise<Result<InstitutionQuotaStatsVO>>;
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
}

// 导出服务实例
const adminQuotaService = new AdminQuotaServiceImpl();
export default adminQuotaService; 