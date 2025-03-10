// 权限实体类型
export interface Permission {
  id: number;
  name: string;         // 权限名称
  code: string;         // 权限编码
  description?: string; // 权限描述
  url?: string;         // 资源URL
  method?: string;      // HTTP方法
  createdAt?: string;   // 创建时间
  updatedAt?: string;   // 更新时间
}

// 权限创建/更新DTO
export interface PermissionDTO {
  id?: number;
  name: string;         // 权限名称
  code: string;         // 权限编码
  description?: string; // 权限描述
  url?: string;         // 资源URL
  method?: string;      // HTTP方法
}

// 分页查询参数
export interface PermissionQueryParams {
  page?: number;
  pageSize?: number;
  name?: string;
  code?: string;
} 