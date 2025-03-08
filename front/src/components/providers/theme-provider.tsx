'use client';

import { useEffect, useState } from 'react';
import { useUIStore, Theme } from '@/stores/ui-store';

interface ThemeProviderProps {
  children: React.ReactNode;
}

export default function ThemeProvider({ children }: ThemeProviderProps) {
  const { theme, setTheme } = useUIStore();
  const [mounted, setMounted] = useState(false);

  // 仅在客户端挂载后执行
  useEffect(() => {
    setMounted(true);
  }, []);

  // 监听系统主题变化
  useEffect(() => {
    if (!mounted) return;
    
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    const handleChange = () => {
      if (theme === 'system') {
        const root = document.documentElement;
        const isDark = mediaQuery.matches;
        root.classList.remove('light', 'dark');
        root.classList.add(isDark ? 'dark' : 'light');
      }
    };
    
    // 初始设置
    if (theme === 'system') {
      handleChange();
    } else {
      const root = document.documentElement;
      root.classList.remove('light', 'dark');
      root.classList.add(theme);
    }
    
    // 添加监听
    mediaQuery.addEventListener('change', handleChange);
    
    return () => {
      // 清理监听
      mediaQuery.removeEventListener('change', handleChange);
    };
  }, [theme, mounted]);

  // 防止服务器端渲染和客户端渲染不匹配
  if (!mounted) {
    // 返回一个空的占位符，避免闪烁
    return <>{children}</>;
  }

  return <>{children}</>;
} 