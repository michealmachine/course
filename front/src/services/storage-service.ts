'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import type { Result, QuotaInfoVO } from '@/types/api';
import { QuotaStatsVO, InstitutionQuotaStatsVO } from '@/types/quota';

export interface StorageService {
  /**
   * 获取机构的存储配额信息
   * @param institutionId 机构ID
   */
  getQuotaInfo(institutionId: number): Promise<Result<QuotaInfoVO>>;

  /**
   * 获取机构的详细配额信息
   * @param institutionId 机构ID
   */
  getAllQuotas(institutionId: number): Promise<Result<QuotaInfoVO[]>>;

  /**
   * 获取当前机构的存储配额使用情况
   */
  getCurrentQuotas(): Promise<Result<QuotaInfoVO[]>>;

  /**
   * 获取单个机构的配额统计信息
   * @param institutionId 机构ID
   */
  getQuotaStats(institutionId: number): Promise<Result<QuotaStatsVO>>;

  /**
   * 获取所有机构的配额统计信息 (管理员专用)
   */
  getAllInstitutionsQuotaStats(): Promise<Result<InstitutionQuotaStatsVO>>;
}

class StorageServiceImpl implements StorageService {
  async getQuotaInfo(institutionId: number): Promise<Result<QuotaInfoVO>> {
    const response: AxiosResponse<ApiResponse<QuotaInfoVO>> = await request.get(`/storage/quota/${institutionId}`);
    return response.data as Result<QuotaInfoVO>;
  }

  async getAllQuotas(institutionId: number): Promise<Result<QuotaInfoVO[]>> {
    const response: AxiosResponse<ApiResponse<QuotaInfoVO[]>> = await request.get(`/storage/quota/${institutionId}/details`);
    return response.data as Result<QuotaInfoVO[]>;
  }

  async getCurrentQuotas(): Promise<Result<QuotaInfoVO[]>> {
    const response: AxiosResponse<ApiResponse<QuotaInfoVO[]>> = await request.get('/media/quota');
    return response.data as Result<QuotaInfoVO[]>;
  }

  async getQuotaStats(institutionId: number): Promise<Result<QuotaStatsVO>> {
    const response: AxiosResponse<ApiResponse<QuotaStatsVO>> = await request.get(`/storage/quota/${institutionId}/stats`);
    return response.data as Result<QuotaStatsVO>;
  }

  async getAllInstitutionsQuotaStats(): Promise<Result<InstitutionQuotaStatsVO>> {
    const response: AxiosResponse<ApiResponse<InstitutionQuotaStatsVO>> = await request.get('/admin/quota/stats');
    return response.data as Result<InstitutionQuotaStatsVO>;
  }
}

export const storageService = new StorageServiceImpl(); 