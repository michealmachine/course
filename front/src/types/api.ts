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

// 分页响应结构
export interface PaginationResult<T> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

// API错误
export interface ApiError {
  code: number;
  message: string;
  errors?: Record<string, string[]>;
} 