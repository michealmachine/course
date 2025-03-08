'use client';

import { CaptchaResponse, LoginRequest, LoginResponse, RegisterRequest, User } from '@/types/auth';
import { request } from './api';
import axios from 'axios';

/**
 * 认证服务
 */
const authService = {
  /**
   * 获取验证码
   */
  getCaptcha: async () => {
    try {
      console.log('开始获取验证码');
      
      // 先生成一个随机的captchaKey
      const generatedCaptchaKey = Math.random().toString(36).substring(2, 15);
      console.log('生成的captchaKey：', generatedCaptchaKey);
      
      // 将captchaKey作为查询参数传递给后端
      const response = await axios.get(
        `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api'}/auth/captcha?captchaKey=${encodeURIComponent(generatedCaptchaKey)}`, 
        { 
          responseType: 'arraybuffer',
          // 添加请求完成回调以记录响应头
          onDownloadProgress: (progressEvent) => {
            if (progressEvent.loaded === progressEvent.total) {
              console.log('验证码请求完成');
            }
          }
        }
      );
      
      console.log('验证码响应状态：', response.status);
      console.log('验证码响应头：', response.headers);
      
      // 优先使用响应头中的Captcha-Key，如果不存在再使用自生成的captchaKey
      // 尝试各种大小写形式获取Captcha-Key
      const headerCaptchaKey = response.headers['captcha-key'] ||
                              response.headers['Captcha-Key'] ||
                              response.headers['CAPTCHA-KEY'];
      
      const captchaKey = headerCaptchaKey || generatedCaptchaKey;
      console.log('最终使用的验证码Key：', captchaKey);
      
      // 将图片数据转换为base64
      const captchaImage = `data:image/jpeg;base64,${Buffer.from(response.data).toString('base64')}`;
      
      return { captchaId: captchaKey, captchaImage };
    } catch (error) {
      console.error('获取验证码失败：', error);
      throw new Error('获取验证码失败，请刷新页面重试');
    }
  },

  /**
   * 用户登录
   */
  login: async (data: LoginRequest) => {
    // 确保captchaKey和captchaCode正确传递
    console.log('开始登录，验证码参数：', {
      captchaKey: data.captchaKey,
      captchaCode: data.captchaCode
    });
    
    // 打印完整的请求参数（密码用星号隐藏）
    const logData = {
      ...data,
      password: '******'
    };
    console.log('登录请求参数：', logData);
    
    // 发起登录请求
    const response = await request.post<any>('/auth/login', data);
    console.log('登录响应：', response);
    
    // 检查是否有响应和响应数据
    if (!response || !response.data) {
      console.error('登录响应为空');
      throw new Error('登录失败，响应为空');
    }
    
    // 检查响应状态码
    if (response.data.code !== 200) {
      console.error('登录失败，错误码：', response.data.code, '错误信息：', response.data.message);
      throw new Error(response.data.message || '登录失败');
    }
    
    // 提取令牌数据
    const tokenData = response.data.data;
    console.log('提取的令牌数据：', tokenData);
    
    if (!tokenData) {
      console.error('令牌数据为空');
      throw new Error('登录失败，未获取到令牌数据');
    }
    
    if (!tokenData.accessToken) {
      console.error('访问令牌为空', tokenData);
      throw new Error('登录失败，未获取到有效的访问令牌');
    }
    
    // 为了兼容旧代码，设置token字段
    const loginResponse: LoginResponse = {
      accessToken: tokenData.accessToken,
      refreshToken: tokenData.refreshToken,
      tokenType: tokenData.tokenType || 'Bearer',
      expiresIn: tokenData.expiresIn || 0,
      token: tokenData.accessToken, // 兼容旧代码
      user: undefined // 稍后会尝试获取
    };
    
    try {
      // 获取用户信息
      console.log('尝试获取用户信息，使用令牌：', loginResponse.accessToken);
      const authHeader = {
        headers: {
          Authorization: `${loginResponse.tokenType} ${loginResponse.accessToken}`
        }
      };
      
      // 获取用户信息
      const userResponse = await request.get<User>('/auth/me', authHeader);
      console.log('用户信息响应：', userResponse);
      
      if (userResponse?.data?.data) {
        loginResponse.user = userResponse.data.data;
      } else {
        console.warn('未获取到用户信息或格式不正确');
      }
    } catch (error) {
      console.error('获取用户信息失败', error);
      // 继续返回令牌数据，但没有用户信息
    }
    
    return loginResponse;
  },

  /**
   * 用户注册
   */
  register: async (data: RegisterRequest) => {
    const response = await request.post<{ message: string }>('/auth/register', data);
    return response.data.data;
  },

  /**
   * 刷新令牌
   */
  refreshToken: async (refreshToken: string) => {
    // 发起刷新令牌请求
    const response = await request.post<any>(
      '/auth/refresh-token',
      { refreshToken }
    );
    console.log('刷新令牌响应：', response);
    
    // 检查是否有响应和响应数据
    if (!response || !response.data) {
      console.error('刷新令牌响应为空');
      throw new Error('刷新令牌失败，响应为空');
    }
    
    // 检查响应状态码
    if (response.data.code !== 200) {
      console.error('刷新令牌失败，错误码：', response.data.code, '错误信息：', response.data.message);
      throw new Error(response.data.message || '刷新令牌失败');
    }
    
    // 提取令牌数据
    const tokenData = response.data.data;
    console.log('提取的令牌数据：', tokenData);
    
    if (!tokenData) {
      console.error('令牌数据为空');
      throw new Error('刷新令牌失败，未获取到令牌数据');
    }
    
    if (!tokenData.accessToken) {
      console.error('访问令牌为空', tokenData);
      throw new Error('刷新令牌失败，未获取到有效的访问令牌');
    }
    
    // 构造响应对象
    const loginResponse: LoginResponse = {
      accessToken: tokenData.accessToken,
      refreshToken: tokenData.refreshToken,
      tokenType: tokenData.tokenType || 'Bearer',
      expiresIn: tokenData.expiresIn || 0,
      token: tokenData.accessToken // 兼容旧代码
    };
    
    return loginResponse;
  },

  /**
   * 用户注销
   */
  logout: async () => {
    const response = await request.post<{ message: string }>('/auth/logout');
    return response.data.data;
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: async () => {
    console.log('开始获取当前用户信息');
    try {
      const response = await request.get<any>('/auth/me');
      console.log('获取用户信息响应：', response);
      
      // 检查是否有响应和响应数据
      if (!response || !response.data) {
        console.error('获取用户信息响应为空');
        throw new Error('获取用户信息失败，响应为空');
      }
      
      // 检查响应状态码
      if (response.data.code !== 200) {
        console.error('获取用户信息失败，错误码：', response.data.code, '错误信息：', response.data.message);
        throw new Error(response.data.message || '获取用户信息失败');
      }
      
      // 提取用户数据
      const userData = response.data.data;
      console.log('提取的用户数据：', userData);
      
      if (!userData) {
        console.error('用户数据为空');
        throw new Error('获取用户信息失败，用户数据为空');
      }
      
      return userData;
    } catch (error) {
      console.error('获取用户信息出错：', error);
      throw error;
    }
  }
};

export default authService; 