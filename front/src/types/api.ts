// API响应通用结构
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  errors?: Record<string, string[]>;
}

// 分页请求参数
export interface PaginationParams {
  page: number;
  pageSize: number;
}

// Spring Boot Pageable参数
export interface Pageable {
  page?: number;
  size?: number;
  sort?: string | string[];
}

// 分页响应结构
export interface PaginationResult<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
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

// API错误
export interface ApiError {
  code: number;
  message: string;
  errors?: Record<string, string[]>;
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