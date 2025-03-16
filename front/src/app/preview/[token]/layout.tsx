import React from 'react';
import type { Metadata } from 'next';
import { Button } from '@/components/ui/button';
import { BookOpen } from 'lucide-react';

export const metadata: Metadata = {
  title: '课程预览',
  description: '课程内容预览',
};

export default function PreviewLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex flex-col min-h-screen">
      {/* 简单的导航栏 */}
      <header className="border-b bg-background">
        <div className="container flex h-16 items-center px-4">
          <div className="flex items-center">
            <BookOpen className="h-6 w-6 mr-2" />
            <span className="font-bold text-xl">课程预览</span>
          </div>
          <div className="ml-auto flex items-center space-x-4">
            <Button variant="ghost" onClick={() => window.close()}>
              关闭预览
            </Button>
          </div>
        </div>
      </header>
      
      {/* 主内容区域 */}
      <main className="flex-1">
        {children}
      </main>
      
      {/* 简单的页脚 */}
      <footer className="border-t py-6 bg-muted">
        <div className="container px-4">
          <div className="text-center text-sm text-muted-foreground">
            <p>本页面仅用于课程内容预览，课程创建者可据此检查课程内容展示效果。</p>
            <p className="mt-1">© {new Date().getFullYear()} 课程平台</p>
          </div>
        </div>
      </footer>
    </div>
  );
} 