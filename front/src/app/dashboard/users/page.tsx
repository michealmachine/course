'use client';

import { UserList } from '@/components/dashboard/users/user-list';
import { UserForm } from '@/components/dashboard/users/user-form';
import { RoleAssignmentDialog } from '@/components/dashboard/users/role-assignment-dialog';
import { DeleteConfirmationDialog } from '@/components/dashboard/users/delete-confirmation-dialog';
import { Separator } from '@/components/ui/separator';

export default function UsersPage() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">用户管理</h2>
        <p className="text-muted-foreground">
          管理系统用户，包括创建、编辑、删除用户以及分配角色
        </p>
      </div>
      <Separator />
      
      <UserList />
      
      {/* 对话框组件 */}
      <UserForm />
      <RoleAssignmentDialog />
      <DeleteConfirmationDialog />
    </div>
  );
} 