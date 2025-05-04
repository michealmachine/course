'use client';

import { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import InstitutionManagement from '@/components/admin/institution-management';
import CourseManagement from '@/components/admin/course-management';

export default function AdminInstitutionsPage() {
  const [activeTab, setActiveTab] = useState('institutions');

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">机构课程管理</h1>
        <p className="text-muted-foreground">
          管理平台上的所有机构及其课程，查看统计数据和用户信息。
        </p>
      </div>

      <Tabs defaultValue="institutions" value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="institutions">机构管理</TabsTrigger>
          <TabsTrigger value="courses">课程管理</TabsTrigger>
        </TabsList>
        
        <TabsContent value="institutions" className="space-y-4">
          <InstitutionManagement />
        </TabsContent>
        
        <TabsContent value="courses" className="space-y-4">
          <CourseManagement />
        </TabsContent>
      </Tabs>
    </div>
  );
}
