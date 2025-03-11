import { request } from './api';
import { InstitutionApplicationResponse, InstitutionResponse, Page, InstitutionApplicationQueryParams } from '@/types/institution';

/**
 * 机构审核服务 - 处理机构申请审核相关接口（管理员/审核员使用）
 */
export const reviewerInstitutionService = {
  /**
   * 分页查询机构申请列表
   * @param params 查询参数
   * @returns 申请列表分页数据
   */
  async getApplications(params: InstitutionApplicationQueryParams): Promise<Page<InstitutionApplicationResponse>> {
    const { page = 0, size = 10, status } = params;
    let url = `/reviewer/institutions/applications?page=${page}&size=${size}`;
    
    if (status !== undefined) {
      url += `&status=${status}`;
    }
    
    if (params.name) {
      url += `&name=${encodeURIComponent(params.name)}`;
    }
    
    if (params.applicationId) {
      url += `&applicationId=${encodeURIComponent(params.applicationId)}`;
    }
    
    if (params.contactEmail) {
      url += `&contactEmail=${encodeURIComponent(params.contactEmail)}`;
    }
    
    if (params.contactPerson) {
      url += `&contactPerson=${encodeURIComponent(params.contactPerson)}`;
    }
    
    const response = await request.get<Page<InstitutionApplicationResponse>>(url);
    return response.data.data;
  },
  
  /**
   * 获取申请详情
   * @param id 申请ID
   * @returns 申请详情
   */
  async getApplicationDetail(id: number): Promise<InstitutionApplicationResponse> {
    const response = await request.get<InstitutionApplicationResponse>(`/reviewer/institutions/applications/${id}`);
    return response.data.data;
  },
  
  /**
   * 审核通过
   * @param id 申请ID
   * @returns 创建的机构信息
   */
  async approveApplication(id: number): Promise<InstitutionResponse> {
    const response = await request.post<InstitutionResponse>(`/reviewer/institutions/applications/${id}/approve`);
    return response.data.data;
  },
  
  /**
   * 审核拒绝
   * @param id 申请ID
   * @param reason 拒绝原因
   */
  async rejectApplication(id: number, reason: string): Promise<void> {
    await request.post(`/reviewer/institutions/applications/${id}/reject?reason=${encodeURIComponent(reason)}`);
  }
};

export default reviewerInstitutionService; 