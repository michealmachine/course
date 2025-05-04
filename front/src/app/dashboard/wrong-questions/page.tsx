'use client';

import { useEffect } from 'react';
import WrongQuestionListNew from '@/components/learning/wrong-question/wrong-question-list-new';
import { useAuthStore } from '@/stores/auth-store';
import { UserRole } from '@/types/auth';

export default function WrongQuestionsPage() {
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
        <h1 className="text-3xl font-bold tracking-tight">错题管理</h1>
        <p className="text-muted-foreground mt-2">
          在这里管理您的错题记录，帮助您更好地复习和提高
        </p>
      </div>

      <WrongQuestionListNew />
    </div>
  );
}