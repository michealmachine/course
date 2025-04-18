'use client';

import { create } from 'zustand';
import adminUserStatsService from '@/services/admin-user-stats-service';
import { 
  UserStatsVO,
  UserRoleDistributionVO,
  UserGrowthStatsVO,
  UserStatusStatsVO,
  UserActivityStatsVO
} from '@/types/user-stats';
import { toast } from 'sonner';

interface UserStatsState {
  // 统计数据
  stats: UserStatsVO | null;
  roleDistribution: UserRoleDistributionVO | null;
  growthStats: UserGrowthStatsVO | null;
  statusStats: UserStatusStatsVO | null;
  activityStats: UserActivityStatsVO | null;
  
  // 加载状态
  isLoading: boolean;
  isLoadingRole: boolean;
  isLoadingGrowth: boolean;
  isLoadingStatus: boolean;
  isLoadingActivity: boolean;
  
  // 错误信息
  error: string | null;
  
  // 获取所有统计数据
  fetchAllStats: () => Promise<void>;
  
  // 获取角色分布统计
  fetchRoleDistribution: () => Promise<void>;
  
  // 获取用户增长统计
  fetchGrowthStats: () => Promise<void>;
  
  // 获取用户状态统计
  fetchStatusStats: () => Promise<void>;
  
  // 获取用户活跃度统计
  fetchActivityStats: () => Promise<void>;
  
  // 清除错误
  clearError: () => void;
}

export const useUserStatsStore = create<UserStatsState>()((set, get) => ({
  // 初始状态
  stats: null,
  roleDistribution: null,
  growthStats: null,
  statusStats: null,
  activityStats: null,
  
  isLoading: false,
  isLoadingRole: false,
  isLoadingGrowth: false,
  isLoadingStatus: false,
  isLoadingActivity: false,
  
  error: null,
  
  // 获取所有统计数据
  fetchAllStats: async () => {
    set({ isLoading: true, error: null });
    try {
      const stats = await adminUserStatsService.getUserStats();
      set({ 
        stats,
        roleDistribution: stats.roleDistribution,
        growthStats: stats.growthStats,
        statusStats: stats.statusStats,
        activityStats: stats.activityStats,
        isLoading: false
      });
    } catch (error) {
      console.error('获取用户统计数据失败:', error);
      set({ 
        isLoading: false, 
        error: error instanceof Error ? error.message : '获取用户统计数据失败'
      });
      toast.error('获取用户统计数据失败');
    }
  },
  
  // 获取角色分布统计
  fetchRoleDistribution: async () => {
    set({ isLoadingRole: true, error: null });
    try {
      const roleDistribution = await adminUserStatsService.getUserRoleDistribution();
      set({ roleDistribution, isLoadingRole: false });
    } catch (error) {
      console.error('获取用户角色分布统计失败:', error);
      set({ 
        isLoadingRole: false, 
        error: error instanceof Error ? error.message : '获取用户角色分布统计失败'
      });
      toast.error('获取用户角色分布统计失败');
    }
  },
  
  // 获取用户增长统计
  fetchGrowthStats: async () => {
    set({ isLoadingGrowth: true, error: null });
    try {
      const growthStats = await adminUserStatsService.getUserGrowthStats();
      set({ growthStats, isLoadingGrowth: false });
    } catch (error) {
      console.error('获取用户增长统计失败:', error);
      set({ 
        isLoadingGrowth: false, 
        error: error instanceof Error ? error.message : '获取用户增长统计失败'
      });
      toast.error('获取用户增长统计失败');
    }
  },
  
  // 获取用户状态统计
  fetchStatusStats: async () => {
    set({ isLoadingStatus: true, error: null });
    try {
      const statusStats = await adminUserStatsService.getUserStatusStats();
      set({ statusStats, isLoadingStatus: false });
    } catch (error) {
      console.error('获取用户状态统计失败:', error);
      set({ 
        isLoadingStatus: false, 
        error: error instanceof Error ? error.message : '获取用户状态统计失败'
      });
      toast.error('获取用户状态统计失败');
    }
  },
  
  // 获取用户活跃度统计
  fetchActivityStats: async () => {
    set({ isLoadingActivity: true, error: null });
    try {
      const activityStats = await adminUserStatsService.getUserActivityStats();
      set({ activityStats, isLoadingActivity: false });
    } catch (error) {
      console.error('获取用户活跃度统计失败:', error);
      set({ 
        isLoadingActivity: false, 
        error: error instanceof Error ? error.message : '获取用户活跃度统计失败'
      });
      toast.error('获取用户活跃度统计失败');
    }
  },
  
  // 清除错误
  clearError: () => {
    set({ error: null });
  }
})); 