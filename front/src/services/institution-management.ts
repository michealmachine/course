'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse } from '@/types/api';

/**
 * 机构信息接口，对应后端InstitutionVO
 */
export interface InstitutionInfo {
  id: number;
  name: string;
  logo?: string;
  description?: string;
  status: number;
  contactPerson: string;
  contactPhone?: string;
  contactEmail: string;
  address?: string;
  registerCode?: string;  // 机构注册码
  createdAt: string;
  updatedAt: string;
}

/**
 * 机构更新请求，对应后端InstitutionUpdateDTO
 */
export interface InstitutionUpdateRequest {
  name: string;
  description?: string;
  contactPerson: string;
  contactPhone?: string;
  address?: string;
}

/**
 * 机构管理服务 - 处理机构信息管理相关接口
 */
const institutionManagementService = {
  /**
   * 获取机构详情
   * @returns 机构详情信息
   */
  getInstitutionDetail: async (): Promise<InstitutionInfo> => {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionInfo>> = 
        await request.get<InstitutionInfo>('/v1/institution-management/detail');
      return response.data.data;
    } catch (error) {
      console.error('获取机构详情失败:', error);
      throw error;
    }
  },

  /**
   * 更新机构信息
   * @param data 更新数据
   * @returns 更新后的机构信息
   */
  updateInstitution: async (data: InstitutionUpdateRequest): Promise<InstitutionInfo> => {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionInfo>> = 
        await request.put<InstitutionInfo>('/v1/institution-management/update', data);
      return response.data.data;
    } catch (error) {
      console.error('更新机构信息失败:', error);
      throw error;
    }
  },

  /**
   * 上传机构Logo
   * @param file Logo文件
   * @returns 含Logo URL的结果
   */
  uploadLogo: async (file: File): Promise<string> => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response: AxiosResponse<ApiResponse<{logoUrl: string}>> = 
        await request.post<{logoUrl: string}>('/v1/institution-management/logo', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });
      return response.data.data.logoUrl;
    } catch (error) {
      console.error('上传机构Logo失败:', error);
      throw error;
    }
  },

  /**
   * 重置机构注册码
   * @returns 新的注册码
   */
  resetRegisterCode: async (): Promise<string> => {
    try {
      const response: AxiosResponse<ApiResponse<{registerCode: string}>> = 
        await request.post<{registerCode: string}>('/v1/institution-management/reset-register-code');
      return response.data.data.registerCode;
    } catch (error) {
      console.error('重置机构注册码失败:', error);
      throw error;
    }
  }
};

export default institutionManagementService; 