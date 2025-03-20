'use client';

import { useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2 } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { UserRole } from '@/types/auth';
import { MyCoursesList } from '@/components/dashboard/my-courses/my-courses-list';

export default function MyCoursesPage() {
  const { user } = useAuthStore();

  // 检查用户是否有权限访问此页面
  if (!user || !user.roles || user.roles.length === 0) {
    return (
      <div className="flex items-center justify-center h-full">
        <p className="text-muted-foreground">您没有权限访问此页面</p>
      </div>
    );
  }

  const hasUserRole = user.roles.some(role => 
    role.code?.replace('ROLE_', '') === UserRole.USER
  );

  if (!hasUserRole) {
    return (
      <div className="flex items-center justify-center h-full">
        <p className="text-muted-foreground">您没有权限访问此页面</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">我的课程</h1>
        <p className="text-muted-foreground mt-2">
          在这里管理您已购买的课程和学习进度
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>已购课程</CardTitle>
        </CardHeader>
        <CardContent>
          <MyCoursesList />
        </CardContent>
      </Card>
    </div>
  );
} 