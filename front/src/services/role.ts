'use client';

import { request } from './api';
import { Role, RoleDTO } from '@/types/role';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';

/**
 * 角色管理服务
 */
const roleService = {
  /**
   * 获取角色列表
   */
  getRoleList: async (): Promise<Role[]> => {
    try {
      const response: AxiosResponse<ApiResponse<Role[]>> = await request.get<Role[]>('/roles');
      return response.data.data;
    } catch (error) {
      console.error('获取角色列表失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取角色详情
   */
  getRoleById: async (id: number): Promise<Role> => {
    try {
      const response: AxiosResponse<ApiResponse<Role>> = await request.get<Role>(`/roles/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取角色详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建角色
   */
  createRole: async (role: RoleDTO): Promise<Role> => {
    try {
      const response: AxiosResponse<ApiResponse<Role>> = await request.post<Role>('/roles', role);
      return response.data.data;
    } catch (error) {
      console.error('创建角色失败:', error);
      throw error;
    }
  },

  /**
   * 更新角色
   */
  updateRole: async (id: number, role: RoleDTO): Promise<Role> => {
    try {
      const response: AxiosResponse<ApiResponse<Role>> = await request.put<Role>(`/roles/${id}`, role);
      return response.data.data;
    } catch (error) {
      console.error(`更新角色失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除角色
   */
  deleteRole: async (id: number): Promise<void> => {
    try {
      await request.delete(`/roles/${id}`);
    } catch (error) {
      console.error(`删除角色失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 批量删除角色
   */
  batchDeleteRoles: async (ids: number[]): Promise<void> => {
    try {
      await request.delete('/roles/batch', { data: ids });
    } catch (error) {
      console.error('批量删除角色失败:', error);
      throw error;
    }
  },

  /**
   * 给角色分配权限
   */
  assignPermissions: async (roleId: number, permissionIds: number[]): Promise<Role> => {
    try {
      const response: AxiosResponse<ApiResponse<Role>> = await request.put<Role>(
        `/roles/${roleId}/permissions`, 
        permissionIds
      );
      return response.data.data;
    } catch (error) {
      console.error(`给角色分配权限失败, roleId: ${roleId}:`, error);
      throw error;
    }
  }
};

export default roleService; 