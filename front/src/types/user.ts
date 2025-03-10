// 导入基础类型
import { User, Role } from './auth';

// 用户查询参数
export interface UserQueryParams {
  username?: string;
  email?: string;
  phone?: string;
  status?: number;
  roleId?: number;
  institutionId?: number;
  pageNum?: number;
  pageSize?: number;
}

// 用户分页响应
export interface UserPageResponse {
  content: User[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// 用户创建/更新DTO
export interface UserDTO {
  id?: number;
  username: string;
  password?: string;
  email: string;
  phone?: string;
  nickname?: string;
  status?: number;
  institutionId?: number;
  roleIds?: number[];
}

// 用户状态更新DTO
export interface UserStatusDTO {
  status: number; // 0-禁用，1-正常
}

// 用户角色分配DTO
export interface UserRoleAssignmentDTO {
  roleIds: number[];
} 