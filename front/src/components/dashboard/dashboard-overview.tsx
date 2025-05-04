'use client';

import { useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { RefreshCw, Users, BarChart2, Database, HardDrive, Building, PieChart as PieChartIcon, BookOpen, ShoppingCart, Activity } from 'lucide-react';

// 导入用户统计组件
import { useUserStatsStore } from '@/stores/user-stats-store';

// 导入课程排名图表
import { CourseRankingChart } from '@/components/admin/course-ranking-chart';

// 导入收入趋势图表
import { PlatformIncomeTrend } from '@/components/dashboard/admin/platform-income-trend';

// 导入配额管理相关
import adminQuotaService from '@/services/admin-quota-service';
import orderStatsService from '@/services/order-stats-service';
import { useState } from 'react';
import { toast } from 'sonner';
import { formatBytes, formatPercentage, formatPrice } from '@/lib/utils';
import {
  InstitutionQuotaStatsVO,
  MediaTypeDistributionVO
} from '@/types/quota';

// 导入 recharts 组件
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  BarChart as RechartBarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
} from 'recharts';

// 导入 shadcn 图表组件
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from "@/components/ui/chart";
import { type ChartConfig } from "@/components/ui/chart";
import { Skeleton } from '@/components/ui/skeleton';

// 定义灰阶调色板
const GRAYSCALE_PALETTE = {
  light: [
    'hsl(0, 0%, 15%)', // 近黑
    'hsl(0, 0%, 35%)',
    'hsl(0, 0%, 55%)',
    'hsl(0, 0%, 75%)',
    'hsl(0, 0%, 88%)', // 近白
  ],
  dark: [
    'hsl(0, 0%, 95%)', // 近白
    'hsl(0, 0%, 75%)',
    'hsl(0, 0%, 55%)',
    'hsl(0, 0%, 35%)',
    'hsl(0, 0%, 20%)', // 近黑
  ],
};

// 获取媒体类型图标
const getMediaTypeIcon = (mediaType: string) => {
  switch (mediaType) {
    case 'VIDEO':
      return <HardDrive className="h-4 w-4" />;
    case 'AUDIO':
      return <HardDrive className="h-4 w-4" />;
    case 'IMAGE':
      return <HardDrive className="h-4 w-4" />;
    case 'DOCUMENT':
      return <HardDrive className="h-4 w-4" />;
    default:
      return <Database className="h-4 w-4" />;
  }
};

// 媒体类型图表配置
const getMediaTypeChartConfig = (distribution: { type: string; typeName: string }[]): ChartConfig => {
  const config: ChartConfig = {};
  distribution.forEach((item, index) => {
    const key = item.type ? item.type.toLowerCase().replace(/[^a-z0-9]/gi, '_') : `type_${index}`;
    const colorIndex = index % GRAYSCALE_PALETTE.light.length;
    config[key] = {
      label: item.typeName,
      theme: {
        light: GRAYSCALE_PALETTE.light[colorIndex],
        dark: GRAYSCALE_PALETTE.dark[colorIndex]
      }
    };
  });
  return config;
};

// 获取饼图配置
const getPieChartConfig = (institutions: any[]): ChartConfig => {
  const config: ChartConfig = {};
  institutions.forEach((item, index) => {
    const key = `inst_${item.institutionId}`;
    const colorIndex = index % GRAYSCALE_PALETTE.light.length;
    config[key] = {
      label: item.institutionName,
      theme: {
        light: GRAYSCALE_PALETTE.light[colorIndex],
        dark: GRAYSCALE_PALETTE.dark[colorIndex]
      }
    };
  });
  return config;
};

// 获取柱状图配置
const getBarChartConfig = (): ChartConfig => {
  return {
    usedQuota: {
      label: "已用配额",
      color: "hsl(var(--primary))"
    },
    totalQuota: {
      label: "总配额",
      color: "hsl(var(--muted-foreground))"
    }
  };
};

// 角色分布图表配置
const getRoleDistributionConfig = (roleDistributions: any[]): ChartConfig => {
  const config: ChartConfig = {};
  roleDistributions.forEach((item, index) => {
    const key = item.roleCode ? item.roleCode.toLowerCase().replace('role_','') : `role_${index}`;
    const colorIndex = index % GRAYSCALE_PALETTE.light.length;
    config[key] = {
      label: item.roleName,
      theme: {
        light: GRAYSCALE_PALETTE.light[colorIndex],
        dark: GRAYSCALE_PALETTE.dark[colorIndex]
      }
    };
  });
  return config;
};

// 格式化百分比
const formatPercentageValue = (value: number) => {
  return `${value.toFixed(2)}%`;
};

export function DashboardOverview() {
  // 用户统计状态
  const {
    stats, roleDistribution,
    isLoading: isLoadingUserStats,
    fetchAllStats
  } = useUserStatsStore();

  // 配额统计状态
  const [isLoadingQuota, setIsLoadingQuota] = useState(true);
  const [quotaStats, setQuotaStats] = useState<InstitutionQuotaStatsVO | null>(null);
  const [mediaTypeDistribution, setMediaTypeDistribution] = useState<MediaTypeDistributionVO | null>(null);
  const [loadingMediaTypeDistribution, setLoadingMediaTypeDistribution] = useState(false);

  // 收入统计状态
  const [isLoadingIncome, setIsLoadingIncome] = useState(true);
  const [incomeStats, setIncomeStats] = useState<any>({
    totalIncome: 0,
    totalRefund: 0,
    netIncome: 0,
    orderCount: 0,
    paidOrderCount: 0,
    refundOrderCount: 0
  });

  // 初始加载
  useEffect(() => {
    fetchAllStats();
    fetchQuotaStats();
    fetchMediaTypeDistribution();
    fetchIncomeStats();
  }, [fetchAllStats]);

  // 获取配额统计数据
  const fetchQuotaStats = async () => {
    setIsLoadingQuota(true);
    try {
      const response = await adminQuotaService.getAllInstitutionsQuotaStats();
      if (response && response.data) {
        setQuotaStats(response.data);
      }
    } catch (error) {
      console.error('获取机构配额统计数据失败:', error);
      toast.error('获取机构配额统计数据失败');
    } finally {
      setIsLoadingQuota(false);
    }
  };

  // 获取媒体类型分布
  const fetchMediaTypeDistribution = async () => {
    setLoadingMediaTypeDistribution(true);
    try {
      const result = await adminQuotaService.getMediaTypeDistribution();
      setMediaTypeDistribution(result.data);
    } catch (error) {
      console.error('获取媒体类型分布失败:', error);
      toast.error('获取媒体类型分布失败');
    } finally {
      setLoadingMediaTypeDistribution(false);
    }
  };

  // 获取收入统计数据
  const fetchIncomeStats = async () => {
    setIsLoadingIncome(true);
    try {
      const response = await orderStatsService.getPlatformIncomeStats();
      if (response) {
        setIncomeStats(response);
      }
    } catch (error) {
      console.error('获取收入统计数据失败:', error);
      toast.error('获取收入统计数据失败');
    } finally {
      setIsLoadingIncome(false);
    }
  };

  // 手动刷新数据
  const handleRefresh = () => {
    fetchAllStats();
    fetchQuotaStats();
    fetchMediaTypeDistribution();
    fetchIncomeStats();
    toast.success('正在刷新数据...');
  };

  // 动态生成 chartConfig
  const roleChartConfig = stats?.roleDistribution ? getRoleDistributionConfig(stats.roleDistribution.roleDistributions) : {};

  return (
    <div className="space-y-6">
      {/* 顶部操作栏 */}
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">管理员仪表盘</h2>
        <Button
          variant="outline"
          size="sm"
          onClick={handleRefresh}
          disabled={isLoadingUserStats || isLoadingQuota}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoadingUserStats || isLoadingQuota ? 'animate-spin' : ''}`} />
          刷新数据
        </Button>
      </div>

      {/* 配额卡片样式 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* 总用户数卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">总用户数</h3>
              {isLoadingUserStats ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {stats?.growthStats.totalUserCount ? stats.growthStats.totalUserCount.toLocaleString() : '0'}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                系统中的注册用户总数
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <Users className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>

        {/* 总课程数卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">总课程数</h3>
              {isLoadingQuota ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  156
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                已发布的课程总数
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <BookOpen className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>

        {/* 总存储空间卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">总存储空间</h3>
              {isLoadingQuota ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {quotaStats ? formatBytes(quotaStats.totalUsage.totalQuota) : '0 B'}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                系统总存储容量
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <HardDrive className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>

        {/* 已用存储空间卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">已用存储空间</h3>
              {isLoadingQuota ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {quotaStats ? formatBytes(quotaStats.totalUsage.usedQuota) : '0 B'}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                当前已使用的存储空间
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <Database className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>
      </div>

      {/* 第二行卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* 机构数量卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">机构数量</h3>
              {isLoadingQuota ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {quotaStats ? quotaStats.totalUsage.institutionCount : '0'}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                系统中的机构总数
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <Building className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>

        {/* 总收入卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">总收入</h3>
              {isLoadingIncome ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {formatPrice(incomeStats.totalIncome)}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                系统总收入
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <ShoppingCart className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>

        {/* 净收入卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">净收入</h3>
              {isLoadingIncome ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {formatPrice(incomeStats.netIncome)}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                总收入减去退款后的净收入
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <ShoppingCart className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>

        {/* 订单总数卡片 */}
        <Card className="p-6 rounded-lg shadow-sm">
          <div className="flex justify-between items-start">
            <div>
              <h3 className="text-sm font-medium text-muted-foreground">订单总数</h3>
              {isLoadingIncome ? (
                <Skeleton className="h-9 w-28 mt-2" />
              ) : (
                <div className="text-2xl font-bold mt-2">
                  {incomeStats.orderCount.toLocaleString()}
                </div>
              )}
              <p className="text-xs text-muted-foreground mt-1">
                系统中的订单总数
              </p>
            </div>
            <Button variant="ghost" size="icon" className="h-8 w-8 rounded-full">
              <ShoppingCart className="h-5 w-5 text-muted-foreground" />
            </Button>
          </div>
        </Card>
      </div>

      {/* 图表区域 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 用户角色分布 */}
        <Card className="col-span-1 flex flex-col">
          <CardHeader>
            <CardTitle>用户角色分布</CardTitle>
            <CardDescription>各角色用户数量及占比</CardDescription>
          </CardHeader>
          <CardContent className="flex-1">
            {isLoadingUserStats ? (
              <div className="flex items-center justify-center h-[300px]">
                <Skeleton className="h-[300px] w-full rounded-md" />
              </div>
            ) : stats?.roleDistribution?.roleDistributions?.length ? (
              <ChartContainer
                config={roleChartConfig}
                className="mx-auto aspect-square max-h-[300px]"
              >
                <PieChart>
                  <ChartTooltip
                    content={<ChartTooltipContent nameKey="roleName" indicator="dot" />}
                  />
                  <Pie
                    data={stats.roleDistribution.roleDistributions}
                    dataKey="userCount"
                    nameKey="roleName"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    labelLine={true}
                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  >
                    {stats.roleDistribution.roleDistributions.map((entry, index) => {
                      const configKey = entry.roleCode ? entry.roleCode.toLowerCase().replace('role_','') : `role_${index}`;
                      return (
                        <Cell key={`cell-${index}`} fill={`var(--color-${configKey})`} />
                      )
                    })}
                  </Pie>
                  <ChartLegend
                    content={<ChartLegendContent nameKey="roleName" />}
                    layout="horizontal"
                    verticalAlign="bottom"
                    align="center"
                    wrapperStyle={{ paddingTop: '20px' }}
                  />
                </PieChart>
              </ChartContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                暂无角色分布数据
              </div>
            )}
          </CardContent>
        </Card>

        {/* 媒体类型分布 */}
        <Card className="col-span-1 flex flex-col">
          <CardHeader>
            <CardTitle>媒体类型分布</CardTitle>
            <CardDescription>系统中各类型媒体资源占比</CardDescription>
          </CardHeader>
          <CardContent className="flex-1">
            {loadingMediaTypeDistribution ? (
              <div className="flex items-center justify-center h-[300px]">
                <RefreshCw className="h-8 w-8 animate-spin text-muted-foreground" />
              </div>
            ) : mediaTypeDistribution ? (
              <ChartContainer
                className="h-[300px]"
                config={getMediaTypeChartConfig(mediaTypeDistribution.distribution)}
              >
                <PieChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
                  <Pie
                    data={mediaTypeDistribution.distribution}
                    dataKey="count"
                    nameKey="typeName"
                    cx="50%"
                    cy="50%"
                    outerRadius={80}
                    labelLine={true}
                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  >
                    {mediaTypeDistribution.distribution.map((entry, index) => {
                      const configKey = entry.type ? entry.type.toLowerCase().replace(/[^a-z0-9]/gi, '_') : `type_${index}`;
                      return (
                        <Cell
                          key={`cell-${index}`}
                          fill={`var(--color-${configKey})`}
                          stroke="var(--background)"
                          strokeWidth={2}
                        />
                      );
                    })}
                  </Pie>
                  <ChartTooltip
                    content={
                      <ChartTooltipContent
                        formatter={(value, name, props) => {
                          return [value, props.payload.typeName];
                        }}
                        nameKey="typeName"
                        indicator="dot"
                      />
                    }
                  />
                  <ChartLegend
                    content={<ChartLegendContent nameKey="typeName" />}
                    layout="horizontal"
                    verticalAlign="bottom"
                    align="center"
                    wrapperStyle={{ paddingTop: '20px' }}
                  />
                </PieChart>
              </ChartContainer>
            ) : (
              <div className="flex flex-col items-center justify-center h-[300px] space-y-4">
                <PieChartIcon className="h-12 w-12 text-muted-foreground opacity-20" />
                <p className="text-muted-foreground">暂无媒体类型数据</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* 课程排名图表 */}
      <div className="grid grid-cols-1 gap-6">
        <CourseRankingChart />
      </div>

      {/* 机构配额使用排行和收入趋势图 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 机构配额使用排行 */}
        {quotaStats && (
          <Card className="col-span-1">
            <CardHeader>
              <CardTitle>机构配额使用排行</CardTitle>
              <CardDescription>按使用量排序的前10个机构</CardDescription>
            </CardHeader>
            <CardContent>
              <ChartContainer
                className="h-[300px]"
                config={getBarChartConfig()}
              >
                <RechartBarChart
                  data={quotaStats.institutions
                    .sort((a, b) => b.usagePercentage - a.usagePercentage)
                    .slice(0, 10)
                    .map(inst => ({
                      name: inst.institutionName,
                      usedQuota: inst.usedQuota,
                      totalQuota: inst.totalQuota,
                      percentage: inst.usagePercentage
                    }))
                  }
                  layout="vertical"
                  margin={{ top: 20, right: 30, left: 70, bottom: 10 }}
                >
                  <CartesianGrid strokeDasharray="3 3" horizontal={true} vertical={false} />
                  <XAxis type="number" tickFormatter={(value) => formatBytes(value)} />
                  <YAxis
                    type="category"
                    dataKey="name"
                    width={65}
                    tickLine={false}
                    axisLine={false}
                    tickFormatter={(value) => {
                      return value.length > 8 ? `${value.slice(0, 8)}...` : value;
                    }}
                  />
                  <ChartTooltip
                    content={
                      <ChartTooltipContent
                        labelFormatter={(label) => `机构: ${label}`}
                        formatter={(value, name, props) => {
                          if (name === 'usedQuota') {
                            return [formatBytes(Number(value)), '已用配额'];
                          } else if (name === 'totalQuota') {
                            return [formatBytes(Number(value)), '总配额'];
                          }
                          return [value, name];
                        }}
                      />
                    }
                  />
                  <Bar dataKey="usedQuota" fill="var(--color-usedQuota)" radius={[4, 4, 0, 0]} maxBarSize={20} />
                  <Bar dataKey="totalQuota" fill="var(--color-totalQuota)" radius={[4, 4, 0, 0]} maxBarSize={20} />
                </RechartBarChart>
              </ChartContainer>
            </CardContent>
          </Card>
        )}

        {/* 收入趋势图 */}
        <div className="col-span-1">
          <PlatformIncomeTrend />
        </div>
      </div>
    </div>
  );
}
