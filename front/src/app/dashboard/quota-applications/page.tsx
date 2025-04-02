'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function QuotaApplicationsRedirect() {
  const router = useRouter();
  
  useEffect(() => {
    // 重定向到新的配额管理页面，并默认选择"配额申请"标签页
    router.push('/dashboard/admin-quota?tab=applications');
  }, [router]);
  
  return (
    <div className="p-6 flex items-center justify-center h-screen">
      <div className="text-center">
        <h1 className="text-2xl font-bold">正在重定向...</h1>
        <p className="text-muted-foreground">正在跳转到新的配额管理页面</p>
      </div>
    </div>
  );
} 