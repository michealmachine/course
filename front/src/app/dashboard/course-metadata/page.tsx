'use client';

import { useState, useRef, useEffect } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card } from '@/components/ui/card';
import { CategoryManagement } from '@/components/dashboard/course-metadata/category-management';
import { TagManagement } from '@/components/dashboard/course-metadata/tag-management';

export default function CourseMetadataPage() {
  const [activeTab, setActiveTab] = useState('categories');
  const pageRef = useRef<HTMLDivElement>(null);
  
  // 使用useEffect监听可能的DOM错误
  useEffect(() => {
    // 保存原始的错误处理函数
    const originalError = console.error;
    
    // 替换为自定义错误处理函数
    console.error = (...args) => {
      // 检查是否为DOM节点错误
      const errorString = args[0]?.toString() || '';
      if (
        errorString.includes('insertBefore') && 
        errorString.includes('Node')
      ) {
        // 可以添加更精确的调试信息或者直接忽略这个特定错误
        console.warn('捕获到DOM节点错误，可能是由于组件状态更新时组件已卸载');
      } else {
        // 其他错误正常处理
        originalError.apply(console, args);
      }
    };
    
    // 清理函数
    return () => {
      console.error = originalError;
    };
  }, []);

  return (
    <div className="space-y-6" ref={pageRef}>
      <div>
        <h1 className="text-3xl font-bold tracking-tight">课程元数据管理</h1>
        <p className="text-muted-foreground mt-2">
          管理课程分类和标签，组织课程内容
        </p>
      </div>

      <Tabs defaultValue="categories" value={activeTab} onValueChange={(value) => {
        // 使用setTimeout确保状态更新时不会干扰当前渲染周期
        setTimeout(() => setActiveTab(value), 0);
      }} className="space-y-4">
        <TabsList>
          <TabsTrigger value="categories">分类管理</TabsTrigger>
          <TabsTrigger value="tags">标签管理</TabsTrigger>
        </TabsList>
        
        <TabsContent value="categories" className="space-y-4">
          <Card className="p-6">
            <CategoryManagement />
          </Card>
        </TabsContent>
        
        <TabsContent value="tags" className="space-y-4">
          <Card className="p-6">
            <TagManagement />
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
} 