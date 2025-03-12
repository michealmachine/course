'use client';

import { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card } from '@/components/ui/card';
import { CategoryManagement } from '@/components/dashboard/course-metadata/category-management';
import { TagManagement } from '@/components/dashboard/course-metadata/tag-management';

export default function CourseMetadataPage() {
  const [activeTab, setActiveTab] = useState('categories');

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">课程元数据管理</h1>
        <p className="text-muted-foreground mt-2">
          管理课程分类和标签，组织课程内容
        </p>
      </div>

      <Tabs defaultValue="categories" value={activeTab} onValueChange={setActiveTab} className="space-y-4">
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