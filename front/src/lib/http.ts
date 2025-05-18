import axios from 'axios';
import { toast } from 'sonner';

// 创建axios实例
const instance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  timeout: 30000, // 增加超时时间到30秒
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
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
instance.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data;

    // 如果code不是200，说明请求出错
    if (code !== 200) {
      toast.error(message || '请求失败');
      return Promise.reject(new Error(message));
    }

    return response.data;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;

      // 处理常见的HTTP错误
      switch (status) {
        case 401:
          toast.error('请先登录');
          // TODO: 跳转到登录页
          break;
        case 403:
          toast.error('没有权限');
          break;
        case 404:
          toast.error('资源不存在');
          break;
        case 500:
          toast.error('服务器错误');
          break;
        default:
          toast.error(data?.message || '请求失败');
      }
    } else if (error.request) {
      toast.error('网络错误');
    } else {
      toast.error('请求配置错误');
    }

    return Promise.reject(error);
  }
);

// HTTP工具类
export const http = {
  get: <T>(url: string, config?: any) =>
    instance.get<any, T>(url, config),

  post: <T>(url: string, data?: any, config?: any) =>
    instance.post<any, T>(url, data, config),

  put: <T>(url: string, data?: any, config?: any) =>
    instance.put<any, T>(url, data, config),

  delete: <T>(url: string, config?: any) =>
    instance.delete<any, T>(url, config),

  patch: <T>(url: string, data?: any, config?: any) =>
    instance.patch<any, T>(url, data, config),
};