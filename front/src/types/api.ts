/**
 * API相关类型定义
 */

/**
 * API通用响应结构
 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  success: boolean;
  errors?: Record<string, string[]>;
}

/**
 * API错误结构
 */
export interface ApiError {
  code: number;
  message: string;
  errors?: Record<string, string[]>;
}

/**
 * 分页请求参数
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  keyword?: string;
  sort?: string;
  order?: 'asc' | 'desc';
}

/**
 * 分页响应结果
 */
export interface PaginationResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  numberOfElements: number;
  pageable?: {
    pageNumber: number;
    pageSize: number;
    sort?: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
}

// Spring Boot Pageable参数
export interface Pageable {
  page?: number;
  size?: number;
  sort?: string | string[];
}

// Spring Boot Page响应结构
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

// 通用响应类型
export interface Result<T> {
  code: number;
  message: string;
  data: T;
}

// 存储配额信息
export interface QuotaInfoVO {
  type: string;
  typeName: string;
  totalQuota: number;
  usedQuota: number;
  lastUpdatedTime: string;
  availableQuota: number;
  usagePercentage: number;
} 