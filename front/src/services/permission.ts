'use client';

import { request } from './api';
import { Permission, PermissionDTO } from '@/types/permission';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';

/**
 * 权限管理服务
 */
const permissionService = {
  /**
   * 获取权限列表
   */
  getPermissionList: async (): Promise<Permission[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Permission[]>> = await request.get<Permission[]>('/permissions');
      return response.data.data;
    } catch (error) {
      console.error('获取权限列表失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取权限详情
   */
  getPermissionById: async (id: number): Promise<Permission> => {
    try {
      const response: AxiosResponse<ApiResponse<Permission>> = await request.get<Permission>(`/permissions/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取权限详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建权限
   */
  createPermission: async (permission: PermissionDTO): Promise<Permission> => {
    try {
      const response: AxiosResponse<ApiResponse<Permission>> = await request.post<Permission>('/permissions', permission);
      return response.data.data;
    } catch (error) {
      console.error('创建权限失败:', error);
      throw error;
    }
  },

  /**
   * 更新权限
   */
  updatePermission: async (id: number, permission: PermissionDTO): Promise<Permission> => {
    try {
      const response: AxiosResponse<ApiResponse<Permission>> = await request.put<Permission>(`/permissions/${id}`, permission);
      return response.data.data;
    } catch (error) {
      console.error(`更新权限失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除权限
   */
  deletePermission: async (id: number): Promise<void> => {
    try {
      await request.delete(`/permissions/${id}`);
    } catch (error) {
      console.error(`删除权限失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 批量删除权限
   */
  batchDeletePermissions: async (ids: number[]): Promise<void> => {
    try {
      await request.delete('/permissions/batch', { data: ids });
    } catch (error) {
      console.error('批量删除权限失败:', error);
      throw error;
    }
  }
};

export default permissionService; 