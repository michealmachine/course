'use client';

import { request } from './api';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';
import type { Result } from '@/types/api';
import {
  UserStatsVO,
  UserRoleDistributionVO,
  UserGrowthStatsVO,
  UserStatusStatsVO,
  UserActivityStatsVO
} from '@/types/user-stats';

/**
 * 管理员用户统计服务接口
 */
export interface AdminUserStatsService {
  /**
   * 获取用户统计综合数据
   * @returns 用户统计综合数据
   */
  getUserStats(): Promise<UserStatsVO>;

  /**
   * 获取用户角色分布统计
   * @returns 用户角色分布统计
   */
  getUserRoleDistribution(): Promise<UserRoleDistributionVO>;

  /**
   * 获取用户增长统计
   * @returns 用户增长统计
   */
  getUserGrowthStats(): Promise<UserGrowthStatsVO>;

  /**
   * 获取用户状态统计
   * @returns 用户状态统计
   */
  getUserStatusStats(): Promise<UserStatusStatsVO>;

  /**
   * 获取用户活跃度统计
   * @returns 用户活跃度统计
   */
  getUserActivityStats(): Promise<UserActivityStatsVO>;
}

/**
 * 管理员用户统计服务实现
 */
class AdminUserStatsServiceImpl implements AdminUserStatsService {
  /**
   * 获取用户统计综合数据
   * @returns 用户统计综合数据
   */
  async getUserStats(): Promise<UserStatsVO> {
    try {
      const response: AxiosResponse<ApiResponse<UserStatsVO>> = await request.get('/admin/user-stats');
      return response.data.data;
    } catch (error) {
      console.error('获取用户统计综合数据失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户角色分布统计
   * @returns 用户角色分布统计
   */
  async getUserRoleDistribution(): Promise<UserRoleDistributionVO> {
    try {
      const response: AxiosResponse<ApiResponse<UserRoleDistributionVO>> = 
        await request.get('/admin/user-stats/role-distribution');
      return response.data.data;
    } catch (error) {
      console.error('获取用户角色分布统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户增长统计
   * @returns 用户增长统计
   */
  async getUserGrowthStats(): Promise<UserGrowthStatsVO> {
    try {
      const response: AxiosResponse<ApiResponse<UserGrowthStatsVO>> = 
        await request.get('/admin/user-stats/growth');
      return response.data.data;
    } catch (error) {
      console.error('获取用户增长统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户状态统计
   * @returns 用户状态统计
   */
  async getUserStatusStats(): Promise<UserStatusStatsVO> {
    try {
      const response: AxiosResponse<ApiResponse<UserStatusStatsVO>> = 
        await request.get('/admin/user-stats/status');
      return response.data.data;
    } catch (error) {
      console.error('获取用户状态统计失败:', error);
      throw error;
    }
  }

  /**
   * 获取用户活跃度统计
   * @returns 用户活跃度统计
   */
  async getUserActivityStats(): Promise<UserActivityStatsVO> {
    try {
      const response: AxiosResponse<ApiResponse<UserActivityStatsVO>> = 
        await request.get('/admin/user-stats/activity');
      return response.data.data;
    } catch (error) {
      console.error('获取用户活跃度统计失败:', error);
      throw error;
    }
  }
}

// 导出服务实例
const adminUserStatsService = new AdminUserStatsServiceImpl();
export default adminUserStatsService; 