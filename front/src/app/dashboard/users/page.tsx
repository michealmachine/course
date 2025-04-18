'use client';

import { useState } from 'react';
import { UserList } from '@/components/dashboard/users/user-list';
import { UserStats } from '@/components/dashboard/users/user-stats';
import { UserForm } from '@/components/dashboard/users/user-form';
import { RoleAssignmentDialog } from '@/components/dashboard/users/role-assignment-dialog';
import { DeleteConfirmationDialog } from '@/components/dashboard/users/delete-confirmation-dialog';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Users, BarChart2 } from 'lucide-react';

export default function UsersPage() {
  const [activeTab, setActiveTab] = useState('list');
  
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">用户管理</h2>
        <p className="text-muted-foreground">
          管理系统用户，查看用户数据统计
        </p>
      </div>
      <Separator />
      
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full space-y-6">
        <TabsList>
          <TabsTrigger value="list" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            <span>用户列表</span>
          </TabsTrigger>
          <TabsTrigger value="stats" className="flex items-center gap-2">
            <BarChart2 className="h-4 w-4" />
            <span>用户统计</span>
          </TabsTrigger>
        </TabsList>
        
        <TabsContent value="list" className="space-y-6">
          <UserList />
        </TabsContent>
        
        <TabsContent value="stats" className="space-y-6">
          <UserStats />
        </TabsContent>
      </Tabs>
      
      {/* 对话框组件 */}
      <UserForm />
      <RoleAssignmentDialog />
      <DeleteConfirmationDialog />
    </div>
  );
} 