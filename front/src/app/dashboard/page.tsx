'use client';

import { useEffect, useState, useCallback } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { UserRole } from '@/types/auth';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { BookOpen, Users, ShoppingCart, Activity, ShieldCheck, FileText } from 'lucide-react';
import { ClearCacheButton } from '@/components/dashboard/admin/clear-cache-button';
import { SyncLearningRecordsButton } from '@/components/dashboard/admin/sync-learning-records-button';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Skeleton } from '@/components/ui/skeleton';
import { formatPrice, formatDuration } from '@/lib/utils';
import institutionLearningStatsService from '@/services/institution-learning-stats-service';
import { StudentDashboardCards } from '@/components/dashboard/student/student-dashboard-cards';
import { InstitutionStatsVO } from '@/types/institution';
import orderStatsService from '@/services/order-stats-service';

import { InstitutionIncomeTrend } from '@/components/dashboard/orders/institution-income-trend';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid
} from 'recharts';
import { ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/ui/chart';

// 导入管理员仪表盘组件
import { DashboardOverview } from '@/components/dashboard/dashboard-overview';

export default function DashboardPage() {
  const { user } = useAuthStore();
  const [greeting, setGreeting] = useState('欢迎');
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [institutionStats, setInstitutionStats] = useState<InstitutionStatsVO | null>(null);
  const [learningTrendData, setLearningTrendData] = useState<{name: string; 学习时长: number}[]>([]);

  // 确保只在客户端运行
  useEffect(() => {
    setMounted(true);

    // 根据时间设置问候语
    const hours = new Date().getHours();
    let greet = '';

    if (hours < 6) {
      greet = '夜深了';
    } else if (hours < 9) {
      greet = '早上好';
    } else if (hours < 12) {
      greet = '上午好';
    } else if (hours < 14) {
      greet = '中午好';
    } else if (hours < 17) {
      greet = '下午好';
    } else if (hours < 22) {
      greet = '晚上好';
    } else {
      greet = '夜深了';
    }

    setGreeting(greet);
  }, []);

  // 根据用户角色获取角色名称
  const getRoleName = () => {
    if (!user || !user.roles || user.roles.length === 0) return '用户';

    // 获取第一个角色代码（通常是最高权限的角色）
    const primaryRoleCode = user.roles[0].code?.replace('ROLE_', '');

    switch (primaryRoleCode) {
      case UserRole.ADMIN:
        return '管理员';
      case UserRole.REVIEWER:
        return '审核员';
      case UserRole.INSTITUTION:
        return '机构用户';
      case UserRole.USER:
      default:
        return '学员';
    }
  };

  // 检查用户是否有特定角色
  const hasRole = (role: UserRole) => {
    if (!user || !user.roles || user.roles.length === 0) return false;
    return user.roles.some(userRole => userRole.code?.replace('ROLE_', '') === role);
  };

  // 判断是否为管理员
  const isAdmin = hasRole(UserRole.ADMIN);

  // 判断是否为审核员
  const isReviewer = hasRole(UserRole.REVIEWER);

  // 判断是否为机构用户
  const isInstitution = hasRole(UserRole.INSTITUTION);

  // 加载机构统计数据
  const loadInstitutionStats = useCallback(async () => {
    if (!isInstitution) return;

    setLoading(true);
    try {
      // 获取机构学习统计概览
      const learningStatsData = await institutionLearningStatsService.getStatisticsOverview();

      // 构建机构统计数据
      // 注意：这里我们使用学习统计数据中的一些字段，实际项目中可能需要调整
      const stats: InstitutionStatsVO = {
        userCount: learningStatsData?.totalLearners || 0,
        courseCount: learningStatsData?.courseStatistics?.length || 0,
        publishedCourseCount: learningStatsData?.courseStatistics?.length || 0, // 假设所有课程都是已发布的
        totalLearners: learningStatsData?.totalLearners || 0,
        totalLearningDuration: learningStatsData?.totalLearningDuration || 0,
        totalIncome: 0, // 这个需要从订单统计中获取
        monthIncome: 0  // 这个需要从订单统计中获取
      };

      // 尝试获取收入数据
      try {
        const incomeTrend = await orderStatsService.getInstitutionIncomeTrend('30d', 'month');
        if (incomeTrend && incomeTrend.length > 0) {
          // 计算总收入和月收入
          const totalIncome = incomeTrend.reduce((sum, item) => sum + item.income, 0);
          const currentMonthIncome = incomeTrend[incomeTrend.length - 1]?.income || 0;

          stats.totalIncome = totalIncome * 100; // 转换为分
          stats.monthIncome = currentMonthIncome * 100; // 转换为分
        }
      } catch (incomeError) {
        console.error('获取收入数据失败:', incomeError);
      }

      // 获取学习趋势数据
      try {
        // 获取每日学习统计
        const now = new Date();
        const startDate = new Date(now);
        startDate.setDate(now.getDate() - 30);

        const dailyStats = await institutionLearningStatsService.getDailyStatistics(
          startDate.toISOString().split('T')[0],
          now.toISOString().split('T')[0]
        );

        // 设置每日学习统计数据
        if (dailyStats && dailyStats.length > 0) {
          setLearningTrendData(dailyStats.map(item => ({
            name: item.date,
            学习时长: item.durationSeconds / 60 // 转换为分钟
          })));
        }
      } catch (trendError) {
        console.error('获取学习趋势数据失败:', trendError);
      }

      setInstitutionStats(stats);
    } catch (error) {
      console.error('加载机构统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  }, [isInstitution]);

  // 加载数据
  useEffect(() => {
    if (mounted && isInstitution) {
      loadInstitutionStats();
    }
  }, [mounted, isInstitution, loadInstitutionStats]);

  return (
    <div className="space-y-6">
      {/* 欢迎区域 */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          {greeting}，{user?.username || '同学'}
        </h1>
        <p className="text-muted-foreground mt-2">
          欢迎回到您的在线课程平台仪表盘，您当前的身份是：<span className="font-medium">{getRoleName()}</span>
        </p>
      </div>

      {/* 角色提示 */}
      {isAdmin && (
        <Alert>
          <ShieldCheck className="h-4 w-4" />
          <AlertTitle>管理员权限</AlertTitle>
          <AlertDescription>
            您拥有管理员权限，可以管理用户、角色和权限，以及查看系统数据。
          </AlertDescription>
        </Alert>
      )}

      {isReviewer && (
        <Alert>
          <FileText className="h-4 w-4" />
          <AlertTitle>审核员权限</AlertTitle>
          <AlertDescription>
            您拥有审核员权限，可以审核课程内容和用户评论。
          </AlertDescription>
        </Alert>
      )}

      {isInstitution && (
        <Alert>
          <BookOpen className="h-4 w-4" />
          <AlertTitle>机构用户权限</AlertTitle>
          <AlertDescription>
            您拥有机构用户权限，可以创建和管理课程，查看学员数据。
          </AlertDescription>
        </Alert>
      )}

      {/* 统计卡片 - 根据角色显示不同内容 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {isInstitution ? (
          <>
            {/* 机构用户专属卡片 */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">已发布课程</CardTitle>
                <BookOpen className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                {loading ? (
                  <Skeleton className="h-8 w-20" />
                ) : (
                  <>
                    <div className="text-2xl font-bold">
                      {institutionStats?.publishedCourseCount || 0}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      已发布课程总数
                    </p>
                  </>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">学员总数</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                {loading ? (
                  <Skeleton className="h-8 w-20" />
                ) : (
                  <>
                    <div className="text-2xl font-bold">
                      {institutionStats?.totalLearners || 0}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      平台学习用户数
                    </p>
                  </>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">本月收入</CardTitle>
                <ShoppingCart className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                {loading ? (
                  <Skeleton className="h-8 w-20" />
                ) : (
                  <>
                    <div className="text-2xl font-bold">
                      {institutionStats?.monthIncome ? formatPrice(institutionStats.monthIncome / 100) : '¥0.00'}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      当月订单收入
                    </p>
                  </>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">学习时长</CardTitle>
                <Activity className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                {loading ? (
                  <Skeleton className="h-8 w-20" />
                ) : (
                  <>
                    <div className="text-2xl font-bold">
                      {institutionStats?.totalLearningDuration ? formatDuration(institutionStats.totalLearningDuration) : '0小时'}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      总学习时长
                    </p>
                  </>
                )}
              </CardContent>
            </Card>
          </>
        ) : isAdmin ? (
          <>          </>
        ) : isReviewer ? (
          <>
            {/* 审核员卡片 */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">已学课程</CardTitle>
                <BookOpen className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">12</div>
                <p className="text-xs text-muted-foreground mt-1">
                  共计学习 42 小时
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">学习社区</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">+573</div>
                <p className="text-xs text-muted-foreground mt-1">
                  平台活跃学习者
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">订单记录</CardTitle>
                <ShoppingCart className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">5</div>
                <p className="text-xs text-muted-foreground mt-1">
                  最近30天内
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">待审核</CardTitle>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">8</div>
                <p className="text-xs text-muted-foreground mt-1">
                  待审核课程内容
                </p>
              </CardContent>
            </Card>
          </>
        ) : (
          <>
            {/* 普通用户卡片 - 使用真实数据 */}
            <StudentDashboardCards />
          </>
        )}
      </div>



      {/* 管理员专属内容 */}
      {isAdmin && (
        <div className="space-y-6">
          {/* 引入 DashboardOverview 组件 */}
          <DashboardOverview />

          {/* 管理员工具按钮 */}
          <div className="flex justify-end">
            <ClearCacheButton />
            <SyncLearningRecordsButton />
          </div>
        </div>
      )}

      {/* 审核员专属内容 */}
      {isReviewer && (
        <div>
          <h2 className="text-xl font-semibold mb-4">待审核内容</h2>
          <Card>
            <CardHeader>
              <CardTitle>内容审核队列</CardTitle>
              <CardDescription>待审核的课程和评论</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span>待审核课程</span>
                  <span className="font-medium">8</span>
                </div>
                <div className="flex justify-between">
                  <span>待审核评论</span>
                  <span className="font-medium">24</span>
                </div>
                <div className="flex justify-between">
                  <span>今日已审核</span>
                  <span className="font-medium">12</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* 机构用户专属内容 */}
      {isInstitution && (
        <div className="space-y-6">
          {/* 趋势图并排布局 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* 学习时长趋势图 */}
            <Card>
              <CardHeader>
                <CardTitle>学习时长趋势</CardTitle>
                <CardDescription>近30天学习时长变化</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {loading ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : learningTrendData.length > 0 ? (
                  <ChartContainer
                    config={{
                      学习时长: {
                        label: "学习时长 (分钟)",
                        theme: {
                          light: "hsl(0, 0%, 15%)",
                          dark: "hsl(0, 0%, 95%)",
                        },
                      },
                    }}
                    className="h-full w-full"
                  >
                    <LineChart
                      accessibilityLayer
                      data={learningTrendData}
                      margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <ChartTooltip
                        content={
                          <ChartTooltipContent
                            formatter={(value) => `${value} 分钟`}
                          />
                        }
                      />
                      <Line
                        type="monotone"
                        dataKey="学习时长"
                        stroke="hsl(0, 0%, 15%)"
                        activeDot={{ r: 8 }}
                      />
                    </LineChart>
                  </ChartContainer>
                ) : (
                  <div className="flex items-center justify-center h-full">
                    <p className="text-muted-foreground">暂无学习趋势数据</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* 收入趋势图 */}
            <InstitutionIncomeTrend />
          </div>


        </div>
      )}
    </div>
  );
}