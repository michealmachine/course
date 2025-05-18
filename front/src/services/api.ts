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
  timeout: 30000, // 增加超时时间到30秒
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = getStorageItem('token');

    // 如果存在token，添加到请求头
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
      // 确保不将token作为URL参数
      if (config.params && config.params.headers) {
        delete config.params.headers;
      }
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 检查token是否需要刷新
const shouldRefreshToken = (token: string): boolean => {
  try {
    // 解析token获取过期时间
    const payload = JSON.parse(atob(token.split('.')[1]));
    const expiresIn = payload.exp * 1000; // 转换为毫秒
    const now = Date.now();

    // 如果token还有15分钟就过期，才刷新
    return expiresIn - now < 15 * 60 * 1000;
  } catch (error) {
    console.error('解析token失败：', error);
    return false;
  }
};

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 检查当前token是否需要刷新
    const token = getStorageItem('token');
    if (token && shouldRefreshToken(token)) {
      // 静默刷新token
      refreshTokenSilently();
    }
    return response;
  },
  async (error: AxiosError<ApiResponse>) => {
    const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };
    const requestUrl = originalRequest.url || '';

    // 检查是否为登录或注册请求
    const isAuthRequest = requestUrl.includes('/auth/login') ||
                         requestUrl.includes('/auth/register') ||
                         requestUrl.includes('/auth/captcha') ||
                         requestUrl.includes('/auth/refresh-token');

    // 只在401错误时尝试刷新token
    if (error.response?.status === 401 && !originalRequest._retry && !isAuthRequest) {
      originalRequest._retry = true;

      try {
        // 尝试刷新令牌
        const refreshToken = getStorageItem('refreshToken');

        if (refreshToken) {
          console.log('Token过期，开始刷新');
          const response = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string; tokenType: string; expiresIn: number }>>(
            `${api.defaults.baseURL}/auth/refresh-token`,
            { refreshToken }
          );

          if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '刷新令牌失败');
          }

          const { accessToken, refreshToken: newRefreshToken, tokenType = 'Bearer' } = response.data.data;

          if (!accessToken) {
            throw new Error('刷新令牌失败：未获取到有效的访问令牌');
          }

          // 更新localStorage中的令牌
          setStorageItem('token', accessToken);
          setStorageItem('refreshToken', newRefreshToken);

          // 更新请求头并重试原始请求
          const authHeader = `${tokenType} ${accessToken}`;
          api.defaults.headers.common.Authorization = authHeader;
          originalRequest.headers = originalRequest.headers || {};
          originalRequest.headers.Authorization = authHeader;

          return api(originalRequest);
        } else {
          throw new Error('会话已过期，请重新登录');
        }
      } catch (refreshError) {
        // 刷新失败，清除令牌并重定向到登录页
        removeStorageItem('token');
        removeStorageItem('refreshToken');

        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }

        return Promise.reject(refreshError);
      }
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

// 静默刷新token的函数
const refreshTokenSilently = async () => {
  try {
    const refreshToken = getStorageItem('refreshToken');
    if (!refreshToken) return;

    const response = await axios.post<ApiResponse<{ accessToken: string; refreshToken: string; }>>(
      `${api.defaults.baseURL}/auth/refresh-token`,
      { refreshToken }
    );

    if (response.data.code === 200 && response.data.data) {
      const { accessToken, refreshToken: newRefreshToken } = response.data.data;
      setStorageItem('token', accessToken);
      setStorageItem('refreshToken', newRefreshToken);
      api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
    }
  } catch (error) {
    console.error('静默刷新token失败：', error);
  }
};

/**
 * 封装的请求函数
 */
export const request = {
  /**
   * GET请求
   * @param url 请求路径
   * @param config 请求配置
   */
  get: async <T>(url: string, config?: any): Promise<AxiosResponse<ApiResponse<T>>> => {
    try {
      console.log(`开始GET请求: ${url}`, config);
      const token = getStorageItem('token');
      console.log(`当前token: ${token ? '已设置' : '未设置'}`);

      // 确保请求头中包含Authorization
      if (token && (!config || !config.headers)) {
        config = config || {};
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${token}`;
      }

      const response = await api.get<ApiResponse<T>>(url, config);
      console.log(`GET ${url} 请求成功:`, response.status, response.data);
      return response;
    } catch (error: any) {
      // 如果配置中设置了silentOnAuthError，且是401或403错误，则静默失败（不打印错误）
      const isAuthError = axios.isAxiosError(error) &&
        (error.response?.status === 401 || error.response?.status === 403);

      console.log(`GET ${url} 请求失败:`, {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message,
        config: error.config
      });

      if (!(config?.silentOnAuthError && isAuthError)) {
        console.error(`GET ${url} 请求失败:`, error);
      }
      throw error;
    }
  },

  /**
   * POST请求
   * @param url 请求路径
   * @param data 请求数据
   * @param config 请求配置
   */
  post: async <T>(url: string, data?: any, config?: any): Promise<AxiosResponse<ApiResponse<T>>> => {
    try {
      console.log(`开始POST请求: ${url}`, { data });
      const response = await api.post<ApiResponse<T>>(url, data, config);
      console.log(`POST ${url} 请求成功:`, response.status, response.data);
      return response;
    } catch (error: any) {
      // 如果配置中设置了silentOnAuthError，且是401或403错误，则静默失败（不打印错误）
      const isAuthError = axios.isAxiosError(error) &&
        (error.response?.status === 401 || error.response?.status === 403);

      console.log(`POST ${url} 请求失败:`, {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message,
        config: error.config
      });

      if (!(config?.silentOnAuthError && isAuthError)) {
        console.error(`POST ${url} 请求失败:`, error);
      }
      throw error;
    }
  },

  /**
   * PATCH请求
   * @param url 请求路径
   * @param data 请求数据
   * @param config 请求配置
   */
  patch: async <T>(url: string, data?: any, config?: any): Promise<AxiosResponse<ApiResponse<T>>> => {
    try {
      return await api.patch<ApiResponse<T>>(url, data, config);
    } catch (error) {
      // 如果配置中设置了silentOnAuthError，且是401或403错误，则静默失败（不打印错误）
      const isAuthError = axios.isAxiosError(error) &&
        (error.response?.status === 401 || error.response?.status === 403);

      if (!(config?.silentOnAuthError && isAuthError)) {
        console.error(`PATCH ${url} 请求失败:`, error);
      }
      throw error;
    }
  },

  /**
   * PUT请求
   * @param url 请求路径
   * @param data 请求数据
   * @param config 请求配置
   */
  put: async <T>(url: string, data?: any, config?: any): Promise<AxiosResponse<ApiResponse<T>>> => {
    try {
      return await api.put<ApiResponse<T>>(url, data, config);
    } catch (error) {
      // 如果配置中设置了silentOnAuthError，且是401或403错误，则静默失败（不打印错误）
      const isAuthError = axios.isAxiosError(error) &&
        (error.response?.status === 401 || error.response?.status === 403);

      if (!(config?.silentOnAuthError && isAuthError)) {
        console.error(`PUT ${url} 请求失败:`, error);
      }
      throw error;
    }
  },

  /**
   * DELETE请求
   * @param url 请求路径
   * @param config 请求配置
   */
  delete: async <T>(url: string, config?: any): Promise<AxiosResponse<ApiResponse<T>>> => {
    try {
      return await api.delete<ApiResponse<T>>(url, config);
    } catch (error) {
      // 如果配置中设置了silentOnAuthError，且是401或403错误，则静默失败（不打印错误）
      const isAuthError = axios.isAxiosError(error) &&
        (error.response?.status === 401 || error.response?.status === 403);

      if (!(config?.silentOnAuthError && isAuthError)) {
        console.error(`DELETE ${url} 请求失败:`, error);
      }
      throw error;
    }
  }
};

export default api;