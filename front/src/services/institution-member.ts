'use client';

import { request } from './api';
import { AxiosResponse } from 'axios';
import { ApiResponse } from '@/types/api';
import { 
  InstitutionMemberPage, 
  InstitutionMemberQueryParams, 
  InstitutionMemberStats,
  InstitutionMemberVO 
} from '@/types/institution-member';

/**
 * 机构成员管理服务 - 处理机构成员管理相关接口
 */
const institutionMemberService = {
  /**
   * 获取机构成员列表
   * @param params 查询参数
   * @returns 分页成员列表
   */
  getMembers: async (params: InstitutionMemberQueryParams): Promise<InstitutionMemberPage> => {
    try {
      const { pageNum = 1, pageSize = 10, keyword } = params;
      let url = `/institutions/members?pageNum=${pageNum}&pageSize=${pageSize}`;
      
      if (keyword) {
        url += `&keyword=${encodeURIComponent(keyword)}`;
      }
      
      const response: AxiosResponse<ApiResponse<InstitutionMemberPage>> = 
        await request.get<InstitutionMemberPage>(url);
      return response.data.data;
    } catch (error) {
      console.error('获取机构成员列表失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构成员统计信息
   * @returns 统计信息
   */
  getMemberStats: async (): Promise<InstitutionMemberStats> => {
    try {
      const response: AxiosResponse<ApiResponse<InstitutionMemberStats>> = 
        await request.get<InstitutionMemberStats>('/institutions/members/stats');
      return response.data.data;
    } catch (error) {
      console.error('获取机构成员统计信息失败:', error);
      throw error;
    }
  },

  /**
   * 获取机构注册码
   * @returns 机构注册码
   */
  getRegisterCode: async (): Promise<string> => {
    try {
      const response: AxiosResponse<ApiResponse<string>> = 
        await request.get<string>('/institutions/members/register-code');
      return response.data.data;
    } catch (error) {
      console.error('获取机构注册码失败:', error);
      throw error;
    }
  },

  /**
   * 移除机构成员
   * @param userId 用户ID
   */
  removeMember: async (userId: number): Promise<void> => {
    try {
      await request.delete(`/institutions/members/${userId}`);
    } catch (error) {
      console.error(`移除机构成员失败, userId: ${userId}:`, error);
      throw error;
    }
  },

  /**
   * 根据成员数据，生成角色分布统计
   * @param members 成员列表
   * @returns 角色分布数据
   */
  generateRoleDistribution: (members: InstitutionMemberVO[]): Array<{name: string, value: number}> => {
    // 统计不同角色的成员数量
    const roleCountMap = new Map<string, number>();
    
    members.forEach(member => {
      if (member.roles && member.roles.length > 0) {
        member.roles.forEach(role => {
          const roleName = role.name || '未知';
          roleCountMap.set(roleName, (roleCountMap.get(roleName) || 0) + 1);
        });
      } else {
        roleCountMap.set('无角色', (roleCountMap.get('无角色') || 0) + 1);
      }
    });
    
    // 转换为图表数据格式
    return Array.from(roleCountMap.entries()).map(([name, value]) => ({
      name,
      value
    }));
  },

  /**
   * 根据成员数据，生成状态分布统计
   * @param members 成员列表
   * @returns 状态分布数据
   */
  generateStatusDistribution: (members: InstitutionMemberVO[]): Array<{name: string, value: number}> => {
    // 状态计数
    let activeCount = 0;
    let disabledCount = 0;
    
    members.forEach(member => {
      if (member.status === 1) {
        activeCount++;
      } else {
        disabledCount++;
      }
    });
    
    // 返回状态分布数据
    return [
      { name: '正常', value: activeCount },
      { name: '禁用', value: disabledCount }
    ];
  }
};

export default institutionMemberService; 