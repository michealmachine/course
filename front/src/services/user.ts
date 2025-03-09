'use client';

import { request } from './api';
import { User } from '@/types/auth';
import { ApiResponse } from '@/types/api';
import { AxiosResponse } from 'axios';

// 用户个人资料更新请求参数
export interface ProfileUpdateRequest {
  nickname?: string;
  phone?: string;
}

// 密码修改请求参数
export interface PasswordChangeRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// 邮箱更新验证码请求参数
export interface EmailCodeRequest {
  email: string;
  captchaKey: string;
  captchaCode: string;
}

// 邮箱更新请求参数
export interface EmailUpdateRequest {
  newEmail: string;
  emailCode: string;
  password: string;
}

// 头像上传响应
export interface AvatarUploadResponse {
  avatarUrl: string;
}

/**
 * 用户服务
 */
const userService = {
  /**
   * 获取当前用户信息
   */
  getCurrentUser: async (): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.get<User>('/users/current', {
        // 允许用户登出后静默失败
        silentOnAuthError: true,
      });
      return response.data.data;
    } catch (error) {
      console.error('获取当前用户信息失败:', error);
      throw error;
    }
  },

  /**
   * 更新当前用户个人资料
   */
  updateProfile: async (data: ProfileUpdateRequest): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.put<User>('/users/current', data);
      return response.data.data;
    } catch (error) {
      console.error('更新个人资料失败:', error);
      throw error;
    }
  },

  /**
   * 修改当前用户密码
   */
  changePassword: async (data: PasswordChangeRequest): Promise<void> => {
    try {
      await request.put<void>('/users/current/password', data);
    } catch (error) {
      console.error('修改密码失败:', error);
      throw error;
    }
  },

  /**
   * 发送邮箱更新验证码
   */
  sendEmailUpdateCode: async (data: EmailCodeRequest): Promise<void> => {
    try {
      await request.post<void>('/auth/email-update-code', data);
    } catch (error) {
      console.error('发送邮箱验证码失败:', error);
      throw error;
    }
  },

  /**
   * 更新当前用户邮箱
   */
  updateEmail: async (data: EmailUpdateRequest): Promise<void> => {
    try {
      await request.put<void>('/users/current/email', data);
    } catch (error) {
      console.error('更新邮箱失败:', error);
      throw error;
    }
  },

  /**
   * 上传用户头像
   */
  uploadAvatar: async (file: File): Promise<AvatarUploadResponse> => {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response: AxiosResponse<ApiResponse<AvatarUploadResponse>> = await request.post<AvatarUploadResponse>(
        '/users/current/avatar',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      return response.data.data;
    } catch (error) {
      console.error('上传头像失败:', error);
      throw error;
    }
  },
};

export default userService; 