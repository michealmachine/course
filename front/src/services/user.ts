'use client';

import { request } from './api';
import { User } from '@/types/auth';
import { UserDTO, UserQueryParams, UserPageResponse, UserStatusDTO } from '@/types/user';
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
        silentError: true
      });
      return response.data.data;
    } catch (error) {
      console.error('获取用户信息失败', error);
      throw error;
    }
  },

  /**
   * 更新当前用户个人资料
   */
  updateCurrentUserProfile: async (profile: ProfileUpdateRequest): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.put<User>('/users/current', profile);
      return response.data.data;
    } catch (error) {
      console.error('更新用户资料失败:', error);
      throw error;
    }
  },

  /**
   * 修改当前用户密码
   */
  changePassword: async (passwordData: PasswordChangeRequest): Promise<void> => {
    try {
      await request.put('/users/current/password', passwordData);
    } catch (error) {
      console.error('修改密码失败:', error);
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

  /**
   * 更新用户邮箱
   */
  updateEmail: async (emailData: EmailUpdateRequest): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.put<User>('/users/current/email', emailData);
      return response.data.data;
    } catch (error) {
      console.error('更新邮箱失败:', error);
      throw error;
    }
  },

  /**
   * 获取用户基本信息
   */
  getBasicUserInfo: async (userId: number): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.get<User>(`/users/basic/${userId}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取用户基本信息失败, ID: ${userId}:`, error);
      throw error;
    }
  },

  /**
   * 获取用户列表（分页）
   */
  getUserList: async (queryParams: UserQueryParams): Promise<UserPageResponse> => {
    try {
      const response: AxiosResponse<ApiResponse<UserPageResponse>> = await request.get<UserPageResponse>(
        '/users',
        { params: queryParams }
      );
      return response.data.data;
    } catch (error) {
      console.error('获取用户列表失败:', error);
      throw error;
    }
  },

  /**
   * 根据ID获取用户详情
   */
  getUserById: async (id: number): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.get<User>(`/users/${id}`);
      return response.data.data;
    } catch (error) {
      console.error(`获取用户详情失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 创建用户
   */
  createUser: async (user: UserDTO): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.post<User>('/users', user);
      return response.data.data;
    } catch (error) {
      console.error('创建用户失败:', error);
      throw error;
    }
  },

  /**
   * 更新用户
   */
  updateUser: async (id: number, user: UserDTO): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.put<User>(`/users/${id}`, user);
      return response.data.data;
    } catch (error) {
      console.error(`更新用户失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 删除用户
   */
  deleteUser: async (id: number): Promise<void> => {
    try {
      await request.delete(`/users/${id}`);
    } catch (error) {
      console.error(`删除用户失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 批量删除用户
   */
  batchDeleteUsers: async (ids: number[]): Promise<void> => {
    try {
      await request.delete('/users/batch', { data: ids });
    } catch (error) {
      console.error('批量删除用户失败:', error);
      throw error;
    }
  },

  /**
   * 修改用户状态
   */
  updateUserStatus: async (id: number, status: UserStatusDTO): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.patch<User>(
        `/users/${id}/status?status=${status.status}`
      );
      return response.data.data;
    } catch (error) {
      console.error(`修改用户状态失败, ID: ${id}:`, error);
      throw error;
    }
  },

  /**
   * 给用户分配角色
   */
  assignRoles: async (userId: number, roleIds: number[]): Promise<User> => {
    try {
      const response: AxiosResponse<ApiResponse<User>> = await request.put<User>(
        `/users/${userId}/roles`, 
        roleIds
      );
      return response.data.data;
    } catch (error) {
      console.error(`给用户分配角色失败, userId: ${userId}:`, error);
      throw error;
    }
  }
};

export default userService; 