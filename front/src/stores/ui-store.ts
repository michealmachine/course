'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';

// 主题类型
export type Theme = 'light' | 'dark' | 'system';

// UI状态接口
interface UIState {
  // 侧边栏状态
  sidebarOpen: boolean;
  toggleSidebar: () => void;
  setSidebarOpen: (open: boolean) => void;
  
  // 主题状态
  theme: Theme;
  setTheme: (theme: Theme) => void;
}

// 创建UI状态
export const useUIStore = create<UIState>()(
  persist(
    (set) => ({
      // 侧边栏状态（默认收起）
      sidebarOpen: false,
      toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
      setSidebarOpen: (open: boolean) => set({ sidebarOpen: open }),
      
      // 主题状态（默认跟随系统）
      theme: 'system',
      setTheme: (theme: Theme) => {
        set({ theme });
        
        // 根据主题更新文档类
        const root = document.documentElement;
        root.classList.remove('light', 'dark');
        
        // 如果是系统主题，则根据系统偏好设置
        if (theme === 'system') {
          const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches
            ? 'dark'
            : 'light';
          root.classList.add(systemTheme);
        } else {
          root.classList.add(theme);
        }
      },
    }),
    {
      name: 'ui-storage', // localStorage的键名
    }
  )
); 