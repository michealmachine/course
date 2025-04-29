'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { UserRole } from '@/types/auth';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { OrderList } from '@/components/dashboard/orders/order-list';
import { InstitutionOrderStats } from '@/components/dashboard/orders/institution-order-stats';
import { InstitutionIncomeTrend } from '@/components/dashboard/orders/institution-income-trend';
import { OrderStatusDistribution } from '@/components/dashboard/orders/order-status-distribution';
import { CourseIncomeRanking } from '@/components/dashboard/orders/course-income-ranking';
import { PlatformIncomeStats } from '@/components/dashboard/orders/platform-income-stats';
import { PendingRefundsList } from '@/components/dashboard/orders/pending-refunds-list';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { ShoppingCart, BarChart } from 'lucide-react';

export default function OrdersPage() {
  const { user } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [activeTab, setActiveTab] = useState<string>('orders');

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

      {/* 管理员用户专属内容：平台收入统计 */}
      {isAdmin && <PlatformIncomeStats />}

      {/* 机构用户专属内容：收入统计 */}
      {isInstitution && <InstitutionOrderStats />}

      {/* 机构用户专属内容：待处理退款 */}
      {isInstitution && <PendingRefundsList />}

      {/* 添加Tab切换 - 管理员和机构用户都显示 */}
      {(isAdmin || isInstitution) && (
        <Tabs defaultValue="orders" value={activeTab} onValueChange={setActiveTab}>
          <TabsList>
            <TabsTrigger value="orders">订单列表</TabsTrigger>
            <TabsTrigger value="statistics">统计图表</TabsTrigger>
          </TabsList>

          <TabsContent value="orders">
            {/* 订单列表 */}
            <OrderList
              isAdmin={isAdmin}
              isInstitution={isInstitution}
            />
          </TabsContent>

          <TabsContent value="statistics">
            <div className="space-y-6">
              {/* 管理员显示平台级别的统计图表 */}
              {isAdmin && (
                <>
                  {/* 平台收入趋势图 - 使用相同的组件但传入不同的props */}
                  <InstitutionIncomeTrend isAdmin={true} />

                  {/* 平台订单状态分布饼图 */}
                  <OrderStatusDistribution isAdmin={true} />

                  {/* 平台课程收入排行 */}
                  <CourseIncomeRanking isAdmin={true} />
                </>
              )}

              {/* 机构显示机构级别的统计图表 */}
              {isInstitution && (
                <>
                  {/* 收入趋势图 */}
                  <InstitutionIncomeTrend />

                  {/* 订单状态分布饼图 */}
                  <OrderStatusDistribution />

                  {/* 课程收入排行 */}
                  <CourseIncomeRanking />
                </>
              )}
            </div>
          </TabsContent>
        </Tabs>
      )}

      {/* 普通用户只显示订单列表 */}
      {!isAdmin && !isInstitution && (
        <OrderList
          isAdmin={isAdmin}
          isInstitution={isInstitution}
        />
      )}
    </div>
  );
}