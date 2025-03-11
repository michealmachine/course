import { request } from './api';
import { InstitutionApplyRequest, InstitutionApplicationResponse } from '@/types/institution';

/**
 * 机构服务 - 处理机构申请和查询相关接口
 */
export const institutionService = {
  /**
   * 申请创建机构
   * @param data 申请数据
   * @returns 申请ID
   */
  async applyInstitution(data: InstitutionApplyRequest): Promise<string> {
    const response = await request.post<string>('/institutions/apply', data);
    return response.data.data;
  },
  
  /**
   * 查询申请状态
   * @param applicationId 申请ID
   * @param email 联系邮箱
   * @returns 申请详情
   */
  async getApplicationStatus(applicationId: string, email: string): Promise<InstitutionApplicationResponse> {
    const response = await request.get<InstitutionApplicationResponse>(
      `/institutions/application-status?applicationId=${applicationId}&email=${encodeURIComponent(email)}`
    );
    return response.data.data;
  }
};

export default institutionService; 