import request from '@/lib/request';
import { AxiosResponse } from 'axios';

// 登录请求参数
interface LoginRequest {
  username: string;
  password: string;
  captchaKey: string;
  captchaCode: string;
}

// 注册请求参数
interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  emailCode: string;
  captchaKey: string;
  captchaCode: string;
  phone?: string;
}

// 邮箱验证码请求参数
interface EmailVerificationRequest {
  email: string;
  captchaKey: string;
  captchaCode: string;
}

// 密码重置请求参数
interface PasswordResetRequest {
  email: string;
  emailCode: string;
  captchaKey: string;
  captchaCode: string;
}

// JWT令牌响应
interface JwtTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// 刷新令牌请求参数
interface RefreshTokenRequest {
  refreshToken: string;
}

// API响应格式
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const authService = {
  /**
   * 用户登录
   * @param loginData 登录信息
   */
  login: async (loginData: LoginRequest): Promise<JwtTokenResponse> => {
    try {
      const response: AxiosResponse<ApiResponse<JwtTokenResponse>> = await request.post('/auth/login', loginData);
      return response.data.data;
    } catch (error) {
      console.error('登录失败:', error);
      throw error;
    }
  },

  /**
   * 用户注册
   * @param registerData 注册信息
   */
  register: async (registerData: RegisterRequest): Promise<void> => {
    try {
      await request.post('/api/auth/register', registerData);
    } catch (error) {
      console.error('注册失败:', error);
      throw error;
    }
  },

  /**
   * 发送邮箱验证码
   * @param data 邮箱验证码请求
   */
  sendEmailVerificationCode: async (data: EmailVerificationRequest): Promise<void> => {
    try {
      await request.post('/api/auth/email-verification-code', data);
    } catch (error) {
      console.error('发送邮箱验证码失败:', error);
      throw error;
    }
  },

  /**
   * 发送密码重置验证码
   * @param data 邮箱验证码请求
   */
  sendPasswordResetCode: async (data: EmailVerificationRequest): Promise<void> => {
    try {
      await request.post('/api/auth/password-reset-code', data);
    } catch (error) {
      console.error('发送密码重置验证码失败:', error);
      throw error;
    }
  },

  /**
   * 重置密码
   * @param data 密码重置请求
   */
  resetPassword: async (data: PasswordResetRequest): Promise<void> => {
    try {
      await request.post('/api/auth/reset-password', data);
    } catch (error) {
      console.error('重置密码失败:', error);
      throw error;
    }
  },

  /**
   * 刷新令牌
   * @param refreshToken 刷新令牌
   */
  refreshToken: async (refreshToken: string): Promise<JwtTokenResponse> => {
    try {
      const response: AxiosResponse<ApiResponse<JwtTokenResponse>> = await request.post('/api/auth/refresh-token', { refreshToken });
      return response.data.data;
    } catch (error) {
      console.error('刷新令牌失败:', error);
      throw error;
    }
  },

  /**
   * 用户注销
   */
  logout: async (): Promise<void> => {
    try {
      await request.post('/api/auth/logout');
    } catch (error) {
      console.error('注销失败:', error);
      throw error;
    }
  }
};

export default authService;
