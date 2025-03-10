'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { User, LoginRequest, RegisterRequest, EmailVerificationRequest } from '@/types/auth';
import authService from '@/services/auth';
import { ApiError } from '@/types/api';

// 认证状态接口
interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  
  // 认证方法
  login: (credentials: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
  sendEmailVerificationCode: (data: EmailVerificationRequest) => Promise<void>;
  
  // 状态管理方法
  setUser: (user: User | null) => void;
  clearError: () => void;
  handleApiError: (error: any) => Promise<void>;
  
  // 添加初始化方法
  initializeAuth: () => Promise<void>;
}

// 创建认证状态
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      
      // 初始化认证状态
      initializeAuth: async () => {
        const token = localStorage.getItem('token');
        const refreshToken = localStorage.getItem('refreshToken');
        
        if (!token || !refreshToken) {
          set({ isAuthenticated: false, user: null });
          return;
        }
        
        // 如果有token但没有user信息，尝试获取用户信息
        if (!get().user && token) {
          try {
            const user = await authService.getCurrentUser();
            set({ user, isAuthenticated: true });
          } catch (error) {
            // 如果获取用户信息失败，尝试刷新token
            try {
              await get().refreshToken();
            } catch (refreshError) {
              // 如果刷新也失败，清除认证状态
              await get().logout();
            }
          }
        }
      },
      
      // 登录方法
      login: async (credentials: LoginRequest) => {
        set({ isLoading: true, error: null });
        console.log('开始登录，凭据：', { ...credentials, password: '******' });
        
        try {
          const response = await authService.login(credentials);
          console.log('登录成功，响应：', { ...response, accessToken: '******', refreshToken: '******' });
          
          if (!response.accessToken) {
            console.error('登录失败：未获取到有效的访问令牌');
            set({
              isLoading: false,
              error: '登录失败：未获取到有效的访问令牌',
            });
            throw new Error('登录失败：未获取到有效的访问令牌');
          }
          
          // 存储令牌
          localStorage.setItem('token', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          
          // 设置cookie
          document.cookie = `token=${response.accessToken}; path=/`;
          document.cookie = `refreshToken=${response.refreshToken}; path=/`;
          
          // 获取用户信息（只在首次登录时获取）
          try {
            const user = await authService.getCurrentUser();
            console.log('成功获取用户信息：', user);
            
            // 更新状态，包含用户信息
            set({
              user: user,
              isAuthenticated: true,
              isLoading: false,
            });
          } catch (userError) {
            console.error('获取用户信息失败：', userError);
            // 如果获取用户信息失败，认为是认证失败
            set({
              isLoading: false,
              error: '登录失败：无法获取用户信息',
              isAuthenticated: false,
              user: null
            });
            throw userError;
          }
        } catch (error) {
          const apiError = error as ApiError;
          console.error('登录失败：', apiError);
          set({
            isLoading: false,
            error: apiError.message || '登录失败',
          });
          throw error;
        }
      },
      
      // 注册方法
      register: async (data: RegisterRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          await authService.register(data);
          set({ isLoading: false });
        } catch (error) {
          const apiError = error as ApiError;
          set({
            isLoading: false,
            error: apiError.message || '注册失败',
          });
          throw error;
        }
      },
      
      // 发送邮箱验证码
      sendEmailVerificationCode: async (data: EmailVerificationRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          await authService.sendEmailVerificationCode(data);
          set({ isLoading: false });
        } catch (error) {
          const apiError = error as ApiError;
          set({
            isLoading: false,
            error: apiError.message || '发送邮箱验证码失败',
          });
          throw error;
        }
      },
      
      // 注销方法
      logout: async () => {
        set({ isLoading: true });
        
        try {
          const token = localStorage.getItem('token');
          if (token) {
            await authService.logout();
          }
        } catch (error) {
          console.error('注销时发生错误：', error);
        } finally {
          // 清除所有认证相关的状态和存储
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          
          // 清除cookie
          document.cookie = 'token=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT';
          document.cookie = 'refreshToken=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT';
          
          // 更新状态
          set({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error: null
          });
        }
      },
      
      // 刷新令牌
      refreshToken: async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        console.log('尝试刷新令牌，有刷新令牌：', !!refreshToken);
        
        if (!refreshToken) {
          console.warn('无法刷新令牌：没有刷新令牌');
          await get().logout();
          return;
        }
        
        set({ isLoading: true });
        
        try {
          const response = await authService.refreshToken(refreshToken);
          console.log('刷新令牌成功，响应：', { ...response, accessToken: '******', refreshToken: '******' });
          
          if (!response.accessToken) {
            console.error('刷新令牌失败：未获取到有效的访问令牌');
            throw new Error('刷新令牌失败：未获取到有效的访问令牌');
          }
          
          // 更新令牌
          localStorage.setItem('token', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          
          // 设置cookie
          document.cookie = `token=${response.accessToken}; path=/`;
          document.cookie = `refreshToken=${response.refreshToken}; path=/`;
          
          // 保持当前用户信息不变，只更新认证状态
          set({
            isLoading: false,
            isAuthenticated: true
          });
          
          console.log('令牌已更新');
        } catch (error) {
          console.error('刷新令牌失败：', error);
          // 刷新失败直接登出
          await get().logout();
        }
      },
      
      // 设置用户
      setUser: (user: User | null) => {
        set({
          user,
          isAuthenticated: !!user,
        });
      },
      
      // 清除错误
      clearError: () => {
        set({ error: null });
      },

      // 处理API请求错误
      handleApiError: async (error: any) => {
        if (error.status === 401) {
          // token失效直接登出
          await get().logout();
          return;
        }
        // 其他错误正常处理
        set({ error: error.message || '请求失败' });
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated
      }),
      // 添加存储版本控制
      version: 1,
      // 添加迁移逻辑
      migrate: (persistedState: any, version: number) => {
        if (version === 0) {
          // 处理旧版本的状态迁移
          return {
            user: persistedState.user,
            isAuthenticated: persistedState.isAuthenticated
          };
        }
        return persistedState;
      }
    }
  )
); 