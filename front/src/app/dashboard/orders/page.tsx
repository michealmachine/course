'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { UserRole } from '@/types/auth';
import { Separator } from '@/components/ui/separator';
import { OrderList } from '@/components/dashboard/orders/order-list';
import { InstitutionOrderStats } from '@/components/dashboard/orders/institution-order-stats';
import { PendingRefundsList } from '@/components/dashboard/orders/pending-refunds-list';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { ShoppingCart } from 'lucide-react';

export default function OrdersPage() {
  const { user } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  // 确保只在客户端运行
  useEffect(() => {
    setMounted(true);
  }, []);

  // 检查用户是否有特定角色
  const hasRole = (role: UserRole) => {
    if (!user || !user.roles || user.roles.length === 0) return false;
    return user.roles.some(userRole => userRole.code?.replace('ROLE_', '') === role);
  };
  
  // 判断角色
  const isAdmin = hasRole(UserRole.ADMIN);
  const isInstitution = hasRole(UserRole.INSTITUTION);

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight">订单管理</h2>
        <p className="text-muted-foreground">
          {isAdmin ? '查看和管理所有订单' : isInstitution ? '查看和管理机构订单' : '查看您的订单历史记录'}
        </p>
      </div>
      <Separator />
      
      {/* 根据角色显示不同提示 */}
      {isAdmin && (
        <Alert>
          <ShoppingCart className="h-4 w-4" />
          <AlertTitle>管理员订单视图</AlertTitle>
          <AlertDescription>
            您正在查看所有用户的订单信息。您可以搜索、筛选订单，并处理退款申请。
          </AlertDescription>
        </Alert>
      )}
      
      {isInstitution && (
        <Alert>
          <ShoppingCart className="h-4 w-4" />
          <AlertTitle>机构订单视图</AlertTitle>
          <AlertDescription>
            您正在查看机构的订单信息。您可以查看收入统计，搜索订单，并处理退款申请。
          </AlertDescription>
        </Alert>
      )}
      
      {/* 机构用户专属内容：收入统计 */}
      {isInstitution && <InstitutionOrderStats />}
      
      {/* 机构用户专属内容：待处理退款 */}
      {isInstitution && <PendingRefundsList />}
      
      {/* 根据不同角色展示不同的订单列表 */}
      <OrderList 
        isAdmin={isAdmin} 
        isInstitution={isInstitution} 
      />
    </div>
  );
} 