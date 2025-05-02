import { AxiosResponse } from 'axios';
import request from './api';
import { ApiResponse } from '@/types/api';

/**
 * 管理员缓存服务
 */
class AdminCacheService {
  /**
   * 清除所有缓存
   * @returns 清除的缓存名称列表
   */
  async clearAllCaches(): Promise<string[]> {
    try {
      const response: AxiosResponse<ApiResponse<string[]>> = 
        await request.post('/admin/cache/clear-all');
      return response.data.data;
    } catch (error) {
      console.error('清除缓存失败:', error);
      throw error;
    }
  }
}

export default new AdminCacheService();
