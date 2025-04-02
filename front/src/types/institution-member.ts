import { Page } from './api';
import { Role } from './auth';

/**
 * 机构成员VO
 */
export interface InstitutionMemberVO {
  id: number;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  nickname?: string;
  status: number;  // 0-禁用，1-正常
  institutionId: number;
  createdAt: string;
  updatedAt: string;
  roles?: Role[];
}

/**
 * 机构成员查询参数
 */
export interface InstitutionMemberQueryParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
}

/**
 * 机构成员分页响应
 */
export type InstitutionMemberPage = Page<InstitutionMemberVO>;

/**
 * 机构成员统计信息
 */
export interface InstitutionMemberStats {
  total: number;      // 当前成员总数
  limit: number;      // 最大成员限制
  available: number;  // 剩余可用名额
}

/**
 * 成员角色分布数据（用于图表）
 */
export interface MemberRoleDistribution {
  name: string;
  value: number;
}

/**
 * 成员状态分布数据（用于图表）
 */
export interface MemberStatusDistribution {
  name: string;
  value: number;
} 