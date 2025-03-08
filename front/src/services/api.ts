'use client';

import axios, { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ApiError, ApiResponse } from '@/types/api';

// 安全获取localStorage中的值
const getStorageItem = (key: string): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(key);
};

// 安全设置localStorage中的值
const setStorageItem = (key: string, value: string): void => {
  if (typeof window === 'undefined') return;
  localStorage.setItem(key, value);
};

// 安全删除localStorage中的值
const removeStorageItem = (key: string): void => {
  if (typeof window === 'undefined') return;
  localStorage.removeItem(key);
};

// 创建axios实例
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = getStorageItem('token');
    
    // 如果存在token，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 直接返回响应数据
    return response;
  },
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };
    const requestUrl = originalRequest.url || '';
    
    // 检查是否为登录或注册请求
    const isAuthRequest = requestUrl.includes('/auth/login') || 
                          requestUrl.includes('/auth/register') ||
                          requestUrl.includes('/auth/captcha');
    
    // 处理401错误（未授权），但不处理认证请求
    if (error.response?.status === 401 && !originalRequest._retry && !isAuthRequest) {
      originalRequest._retry = true;
      console.log('收到401错误，尝试刷新令牌');
      
      try {
        // 尝试刷新令牌
        const refreshToken = getStorageItem('refreshToken');
        
        if (refreshToken) {
          console.log('开始刷新令牌');
          const response = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string; tokenType: string; expiresIn: number }>>(
            `${api.defaults.baseURL}/auth/refresh-token`,
            { refreshToken }
          );
          
          console.log('刷新令牌响应：', response);
          
          // 检查响应状态码
          if (response.data.code !== 200 || !response.data.data) {
            console.error('刷新令牌失败：', response.data.message);
            throw new Error(response.data.message || '刷新令牌失败');
          }
          
          const { accessToken, refreshToken: newRefreshToken, tokenType = 'Bearer' } = response.data.data;
          
          if (!accessToken) {
            console.error('刷新令牌失败：未获取到有效的访问令牌');
            throw new Error('刷新令牌失败：未获取到有效的访问令牌');
          }
          
          console.log('刷新令牌成功，新令牌：', accessToken);
          
          // 更新localStorage中的令牌
          setStorageItem('token', accessToken);
          setStorageItem('refreshToken', newRefreshToken);
          
          // 更新请求头并重试原始请求
          const authHeader = `${tokenType} ${accessToken}`;
          api.defaults.headers.common.Authorization = authHeader;
          originalRequest.headers = originalRequest.headers || {};
          originalRequest.headers.Authorization = authHeader;
          
          console.log('使用新令牌重试原始请求');
          return api(originalRequest);
        } else {
          console.error('刷新令牌失败：本地没有刷新令牌');
          throw new Error('会话已过期，请重新登录');
        }
      } catch (refreshError) {
        console.error('刷新令牌出错：', refreshError);
        
        // 刷新令牌失败，清除本地存储的令牌
        removeStorageItem('token');
        removeStorageItem('refreshToken');
        
        // 重定向到登录页
        if (typeof window !== 'undefined') {
          console.log('重定向到登录页');
          window.location.href = '/login';
        }
        
        return Promise.reject(refreshError);
      }
    }
    
    // 对于认证请求的错误，直接返回错误，不尝试刷新令牌
    if (isAuthRequest) {
      console.log('认证请求失败，不尝试刷新令牌');
    }
    
    // 构造API错误对象
    const apiError: ApiError = {
      code: error.response?.data?.code || error.response?.status || 500,
      message: error.response?.data?.message || error.message || '请求失败',
      errors: error.response?.data?.errors,
    };
    
    return Promise.reject(apiError);
  }
);

// 通用请求方法
export const request = {
  get: <T>(url: string, params?: any, config?: AxiosRequestConfig) =>
    api.get<ApiResponse<T>>(url, { params, ...config }),
    
  post: <T>(url: string, data?: any, config?: AxiosRequestConfig) =>
    api.post<ApiResponse<T>>(url, data, config),
    
  put: <T>(url: string, data?: any, config?: AxiosRequestConfig) =>
    api.put<ApiResponse<T>>(url, data, config),
    
  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    api.delete<ApiResponse<T>>(url, config),
};

export default api; 