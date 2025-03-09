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
}

// 创建认证状态
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      
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
          
          // 存储令牌 - 使用accessToken作为主要的认证令牌
          localStorage.setItem('token', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          
          // 设置cookie
          document.cookie = `token=${response.accessToken}; path=/`;
          document.cookie = `refreshToken=${response.refreshToken}; path=/`;
          
          try {
            // 如果响应中没有用户信息，则主动获取
            if (!response.user) {
              console.log('登录成功，未获取到用户信息，主动获取用户信息');
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
                // 即使获取用户信息失败，我们仍然认为登录成功了，只是没有用户信息
                set({
                  user: null,
                  isAuthenticated: true,
                  isLoading: false,
                  error: '登录成功，但获取用户信息失败'
                });
              }
            } else {
              // 如果响应中包含用户信息，直接使用
              console.log('登录响应中包含用户信息，直接使用');
              set({
                user: response.user,
                isAuthenticated: true,
                isLoading: false,
              });
            }
          } catch (userError) {
            console.error('获取用户信息过程中出现异常：', userError);
            // 即使获取用户信息失败，我们仍然认为登录成功了，只是没有用户信息
            set({
              user: null,
              isAuthenticated: true,
              isLoading: false,
              error: '登录成功，但获取用户信息失败'
            });
          }
          
          console.log('认证状态已更新，isAuthenticated: true');
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
          await authService.logout();
          
          // 清除令牌
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
          });
        } catch (error) {
          set({ isLoading: false });
          
          // 即使注销失败，也清除本地状态
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          
          set({
            user: null,
            isAuthenticated: false,
          });
        }
      },
      
      // 刷新令牌
      refreshToken: async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        console.log('尝试刷新令牌，有刷新令牌：', !!refreshToken);
        
        if (!refreshToken) {
          console.warn('无法刷新令牌：没有刷新令牌');
          set({ isAuthenticated: false, user: null });
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
          
          // 更新令牌 - 使用accessToken作为主要的认证令牌
          localStorage.setItem('token', response.accessToken);
          localStorage.setItem('refreshToken', response.refreshToken);
          
          // 设置cookie
          document.cookie = `token=${response.accessToken}; path=/`;
          document.cookie = `refreshToken=${response.refreshToken}; path=/`;
          
          // 尝试获取用户信息
          try {
            console.log('刷新令牌后获取用户信息');
            const user = await authService.getCurrentUser();
            
            set({
              isLoading: false,
              isAuthenticated: true,
              user,
            });
            
            console.log('用户信息获取成功，认证状态已更新');
          } catch (userError) {
            console.error('获取用户信息失败：', userError);
            // 即使获取用户信息失败，令牌仍然有效
            set({
              isLoading: false,
              isAuthenticated: true,
            });
          }
        } catch (error) {
          console.error('刷新令牌失败：', error);
          // 刷新失败，清除状态
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          
          set({
            isLoading: false,
            isAuthenticated: false,
            user: null,
            error: '会话已过期，请重新登录',
          });
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
    }),
    {
      name: 'auth-storage', // localStorage的键名
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }), // 只持久化这些字段
    }
  )
); 