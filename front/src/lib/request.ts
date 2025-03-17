import axios from 'axios';
import { toast } from 'sonner';

// 创建axios实例
export const axiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
axiosInstance.interceptors.request.use(
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
axiosInstance.interceptors.response.use(
  (response) => {
    const data = response.data;
    // 如果响应成功但业务状态码不是200，显示错误信息
    if (data && data.code !== 200) {
      toast.error(data.message || '操作失败');
      return Promise.reject(new Error(data.message || '操作失败'));
    }
    return response;
  },
  (error) => {
    // 处理网络错误
    if (!error.response) {
      toast.error('网络错误，请检查您的网络连接');
      return Promise.reject(error);
    }

    // 处理HTTP错误
    const status = error.response.status;
    switch (status) {
      case 401:
        toast.error('登录已过期，请重新登录');
        // 清除token并跳转到登录页
        localStorage.removeItem('token');
        window.location.href = '/login';
        break;
      case 403:
        toast.error('没有权限访问该资源');
        break;
      case 404:
        toast.error('请求的资源不存在');
        break;
      case 500:
        toast.error('服务器错误，请稍后重试');
        break;
      default:
        toast.error(error.response.data?.message || '操作失败，请重试');
    }

    return Promise.reject(error);
  }
); 