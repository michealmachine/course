import { http } from '@/lib/http';
import type { Result, QuotaInfoVO } from '@/types/api';

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
}

class StorageServiceImpl implements StorageService {
  async getQuotaInfo(institutionId: number): Promise<Result<QuotaInfoVO>> {
    return http.get(`/api/storage/quota/${institutionId}`);
  }

  async getAllQuotas(institutionId: number): Promise<Result<QuotaInfoVO[]>> {
    return http.get(`/api/storage/quota/${institutionId}/details`);
  }

  async getCurrentQuotas(): Promise<Result<QuotaInfoVO[]>> {
    return http.get('/api/media/quota');
  }
}

export const storageService = new StorageServiceImpl(); 