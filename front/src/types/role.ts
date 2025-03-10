import { Permission } from './permission';

// 角色实体类型
export interface Role {
  id: number;
  name: string;         // 角色名称
  code: string;         // 角色编码
  description?: string; // 角色描述
  createdAt?: string;   // 创建时间
  updatedAt?: string;   // 更新时间
  permissions?: Permission[]; // 权限列表
}

// 角色创建/更新DTO
export interface RoleDTO {
  id?: number;
  name: string;          // 角色名称
  code: string;          // 角色编码（格式：ROLE_XXX）
  description?: string;  // 角色描述
  permissionIds?: number[]; // 权限ID列表
}

// 分页查询参数
export interface RoleQueryParams {
  page?: number;
  pageSize?: number;
  name?: string;
  code?: string;
} 