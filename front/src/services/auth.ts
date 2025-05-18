'use client';

import { CaptchaResponse, EmailVerificationRequest, LoginRequest, LoginResponse, RegisterRequest, User } from '@/types/auth';
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

      // 第一步：获取验证码key
      const keyResponse = await request.get<any>('/auth/captcha/key');

      // 检查响应
      if (!keyResponse || !keyResponse.data) {
        console.error('获取验证码key响应为空');
        throw new Error('获取验证码失败，响应为空');
      }

      if (keyResponse.data.code !== 200) {
        console.error('获取验证码key失败，错误码：', keyResponse.data.code, '错误信息：', keyResponse.data.message);
        throw new Error(keyResponse.data.message || '获取验证码失败');
      }

      // 提取验证码key
      const captchaKey = keyResponse.data.data;
      console.log('获取到验证码key：', captchaKey);

      if (!captchaKey) {
        console.error('验证码key为空');
        throw new Error('获取验证码失败，验证码key为空');
      }

      // 第二步：获取验证码图片
      const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';
      const imageUrl = `${baseUrl}/auth/captcha/image/${encodeURIComponent(captchaKey)}`;
      console.log('请求验证码图片URL:', imageUrl);

      const imageResponse = await axios.get(
        imageUrl,
        {
          responseType: 'arraybuffer'
        }
      );

      console.log('验证码图片响应状态：', imageResponse.status);

      // 将图片数据转换为base64
      const captchaImage = `data:image/jpeg;base64,${Buffer.from(imageResponse.data).toString('base64')}`;

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
    // 打印登录参数
    console.log('开始登录，参数：', {
      username: data.username,
      password: '******' // 隐藏密码
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
      // 确保使用正确的授权头
      const authHeader = {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `${loginResponse.tokenType} ${loginResponse.accessToken}`
        }
      };

      // 获取用户信息
      const userResponse = await request.get<User>('/users/current', authHeader);
      console.log('用户信息响应：', userResponse);

      if (userResponse?.data?.data) {
        loginResponse.user = userResponse.data.data;
        console.log('成功设置用户信息到登录响应');
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
      // 获取存储的访问令牌
      const token = localStorage.getItem('token');
      if (!token) {
        console.error('获取用户信息失败：未找到访问令牌');
        throw new Error('获取用户信息失败：未找到访问令牌');
      }

      // 确保使用正确的API路径和授权头
      const response = await request.get<any>('/users/current', {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      });

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
  },

  /**
   * 发送邮箱验证码
   */
  sendEmailVerificationCode: async (data: EmailVerificationRequest) => {
    console.log('开始发送邮箱验证码，参数：', {
      email: data.email,
      captchaKey: data.captchaKey,
      captchaCode: data.captchaCode
    });

    // 为邮箱验证码请求特别指定60秒的超时时间
    const response = await request.post<any>('/auth/email-verification-code', data, {
      timeout: 60000 // 60秒超时
    });
    console.log('发送邮箱验证码响应：', response);

    // 检查响应状态
    if (!response || !response.data) {
      console.error('发送邮箱验证码响应为空');
      throw new Error('发送邮箱验证码失败，响应为空');
    }

    if (response.data.code !== 200) {
      console.error('发送邮箱验证码失败，错误码：', response.data.code, '错误信息：', response.data.message);
      throw new Error(response.data.message || '发送邮箱验证码失败');
    }

    return response.data.data;
  },

  /**
   * 发送密码重置验证码
   */
  sendPasswordResetCode: async (data: EmailVerificationRequest) => {
    console.log('开始发送密码重置验证码，参数：', {
      email: data.email,
      captchaKey: data.captchaKey,
      captchaCode: data.captchaCode
    });

    try {
      console.log('准备发送POST请求到 /auth/password-reset-code');
      // 确保使用完整的 URL
      const fullUrl = '/auth/password-reset-code';
      console.log('完整请求URL:', fullUrl);
      // 为密码重置验证码请求特别指定60秒的超时时间
      const response = await request.post<any>(fullUrl, data, {
        timeout: 60000 // 60秒超时
      });
      console.log('发送密码重置验证码响应：', response);

      // 检查响应状态
      if (!response || !response.data) {
        console.error('发送密码重置验证码响应为空');
        throw new Error('发送密码重置验证码失败，响应为空');
      }

      if (response.data.code !== 200) {
        console.error('发送密码重置验证码失败，错误码：', response.data.code, '错误信息：', response.data.message);
        throw new Error(response.data.message || '发送密码重置验证码失败');
      }

      console.log('发送密码重置验证码成功');
      return response.data.data;
    } catch (error) {
      console.error('发送密码重置验证码请求异常：', error);
      console.error('错误详情：', JSON.stringify(error, null, 2));
      throw error;
    }
  },

  /**
   * 重置密码
   */
  resetPassword: async (data: {
    email: string;
    emailCode: string;
  }) => {
    console.log('开始重置密码，参数：', {
      email: data.email,
      emailCode: '******' // 隐藏验证码
    });

    const response = await request.post<any>('/auth/reset-password', data);
    console.log('重置密码响应：', response);

    // 检查响应状态
    if (!response || !response.data) {
      console.error('重置密码响应为空');
      throw new Error('重置密码失败，响应为空');
    }

    if (response.data.code !== 200) {
      console.error('重置密码失败，错误码：', response.data.code, '错误信息：', response.data.message);
      throw new Error(response.data.message || '重置密码失败');
    }

    return response.data.data;
  }
};

export default authService;