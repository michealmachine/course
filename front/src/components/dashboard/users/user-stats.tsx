'use client';

import { useEffect } from 'react';
import { useUserStatsStore } from '@/stores/user-stats-store';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { RefreshCw, Users, UserPlus, UserCheck, UserX, TrendingUp } from 'lucide-react';

// 导入 recharts 基础组件
import {
  PieChart, Pie, Cell, LabelList, 
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  LineChart, Line,
  ResponsiveContainer
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

// 定义更具区分度的灰阶调色板 (HSL格式) - 保持用于多分类图表
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

// 角色分布图表配置 (使用灰阶和 theme 对象)
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

// 用户增长图表配置 (单数据系列，移除特定颜色，使用默认主题色)
const getGrowthStatsConfig = (): ChartConfig => {
  return {
    count: { 
      label: '注册用户', 
      // color: `hsl(var(--foreground))` // 或者不设置，使用默认
    },
  };
};

// 用户活跃度图表配置 (单数据系列，移除特定颜色，使用默认主题色)
const getActivityStatsConfig = (): ChartConfig => {
  return {
    count: { 
      label: '活跃用户', 
      // color: `hsl(var(--foreground))` // 或者不设置，使用默认
    },
  };
};

// 格式化百分比
const formatPercentage = (value: number) => {
  return `${value.toFixed(2)}%`;
};

// 格式化用户数
const formatUserCount = (value: number) => {
  return value.toLocaleString();
};

// 格式化增长率
const formatGrowthRate = (value: number) => {
  return `${value > 0 ? '+' : ''}${value.toFixed(2)}%`;
};

export function UserStats() {
  const { 
    stats, roleDistribution, growthStats, statusStats, activityStats,
    isLoading, isLoadingRole, isLoadingGrowth, isLoadingStatus, isLoadingActivity,
    fetchAllStats, fetchRoleDistribution, fetchGrowthStats, fetchStatusStats, fetchActivityStats
  } = useUserStatsStore();
  
  // 初始加载
  useEffect(() => {
    fetchAllStats();
  }, [fetchAllStats]);
  
  // 手动刷新数据
  const handleRefresh = () => {
    fetchAllStats();
  };
  
  // 动态生成 chartConfig
  const roleChartConfig = stats?.roleDistribution ? getRoleDistributionConfig(stats.roleDistribution.roleDistributions) : {};
  const growthChartConfig = getGrowthStatsConfig();
  const activityChartConfig = getActivityStatsConfig();
  
  return (
    <div className="space-y-6">
      {/* 顶部操作栏 */}
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">用户统计</h2>
        <Button 
          variant="outline" 
          size="sm" 
          onClick={handleRefresh}
          disabled={isLoading}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
          刷新数据
        </Button>
      </div>
      
      {/* 统计卡片区域 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* 总用户数 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">总用户数</CardTitle>
            <Users className="h-5 w-5 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <Skeleton className="h-7 w-28" />
            ) : (
              <>
                <div className="text-2xl font-bold">
                  {stats?.growthStats.totalUserCount ? formatUserCount(stats.growthStats.totalUserCount) : '0'}
                </div>
                <p className="text-xs text-muted-foreground flex items-center gap-1">
                  {stats?.growthStats.monthlyGrowthRate !== undefined ? (
                    <span className={`${stats.growthStats.monthlyGrowthRate >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
                      本月 {formatGrowthRate(stats.growthStats.monthlyGrowthRate)}
                      {stats.growthStats.monthlyGrowthRate >= 0 && <TrendingUp className="h-4 w-4 inline-block ml-1" />}
                    </span>
                  ) : '暂无月增长数据'}
                </p>
              </>
            )}
          </CardContent>
        </Card>
        
        {/* 今日新增 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">今日新增</CardTitle>
            <UserPlus className="h-5 w-5 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <Skeleton className="h-7 w-28" />
            ) : (
              <>
                <div className="text-2xl font-bold">
                  {stats?.growthStats.todayNewUsers ? formatUserCount(stats.growthStats.todayNewUsers) : '0'}
                </div>
                <p className="text-xs text-muted-foreground flex items-center gap-1">
                  {stats?.growthStats.dailyGrowthRate !== undefined ? (
                    <span className={`${stats.growthStats.dailyGrowthRate >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
                      今日 {formatGrowthRate(stats.growthStats.dailyGrowthRate)}
                      {stats.growthStats.dailyGrowthRate >= 0 && <TrendingUp className="h-4 w-4 inline-block ml-1" />}
                    </span>
                  ) : '暂无日增长数据'}
                </p>
              </>
            )}
          </CardContent>
        </Card>
        
        {/* 活跃用户 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">活跃用户 (30天)</CardTitle>
            <UserCheck className="h-5 w-5 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <Skeleton className="h-7 w-28" />
            ) : (
              <>
                <div className="text-2xl font-bold">
                  {stats?.activityStats.activeUserCount ? formatUserCount(stats.activityStats.activeUserCount) : '0'}
                </div>
                <p className="text-xs text-muted-foreground">
                  {stats?.activityStats.activeUserPercentage !== undefined ? (
                    <span>
                      总用户占比 {formatPercentage(stats.activityStats.activeUserPercentage)}
                    </span>
                  ) : '暂无占比数据'}
                </p>
              </>
            )}
          </CardContent>
        </Card>
        
        {/* 禁用用户 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-sm font-medium">禁用用户</CardTitle>
            <UserX className="h-5 w-5 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <Skeleton className="h-7 w-28" />
            ) : (
              <>
                <div className="text-2xl font-bold">
                  {stats?.statusStats.disabledUserCount ? formatUserCount(stats.statusStats.disabledUserCount) : '0'}
                </div>
                <p className="text-xs text-muted-foreground">
                  {stats?.statusStats.disabledUserPercentage !== undefined ? (
                    <span>
                      总用户占比 {formatPercentage(stats.statusStats.disabledUserPercentage)}
                    </span>
                  ) : '暂无占比数据'}
                </p>
              </>
            )}
          </CardContent>
        </Card>
      </div>
      
      {/* 图表区域 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 用户角色分布 - 改回实心饼图 + ChartLegend */}
        <Card className="col-span-1 flex flex-col">
          <CardHeader>
            <CardTitle>用户角色分布</CardTitle>
            <CardDescription>各角色用户数量及占比</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 pb-0">
            {isLoading ? (
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
                    content={<ChartTooltipContent nameKey="roleName" indicator="dot" />} // 使用 dot 指示器
                  />
                  <Pie
                    data={stats.roleDistribution.roleDistributions}
                    dataKey="userCount"
                    nameKey="roleName" 
                    cx="50%"
                    cy="50%"
                    outerRadius={100} 
                    // innerRadius={60} // 移除内半径，变为实心饼图
                    labelLine={false}
                    // 移除自定义 label
                    // label={...}
                  >
                    {stats.roleDistribution.roleDistributions.map((entry, index) => {
                      const configKey = entry.roleCode ? entry.roleCode.toLowerCase().replace('role_','') : `role_${index}`;
                      return (
                        <Cell key={`cell-${index}`} fill={`var(--color-${configKey})`} />
                      )
                    })}
                  </Pie>
                  {/* 添加 ChartLegend */}
                  <ChartLegend content={<ChartLegendContent nameKey="roleName" />} />
                </PieChart>
              </ChartContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                暂无角色分布数据
              </div>
            )}
          </CardContent>
           <CardFooter className="flex-col gap-2 text-sm pt-4">
            <div className="leading-none text-muted-foreground">
              显示各用户角色的数量分布
            </div>
          </CardFooter>
        </Card>
        
        {/* 用户增长趋势 - 使用默认主题色 */}
        <Card className="col-span-1 flex flex-col">
          <CardHeader>
            <CardTitle>用户增长趋势</CardTitle>
            <CardDescription>最近30天用户注册数量</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 pb-0">
            {isLoading ? (
              <div className="flex items-center justify-center h-[300px]">
                <Skeleton className="h-[300px] w-full rounded-md" />
              </div>
            ) : stats?.growthStats?.dailyRegistrations?.length ? (
              <ChartContainer 
                config={growthChartConfig} 
                className="h-[300px] w-full"
              >
                <LineChart
                  accessibilityLayer
                  data={stats.growthStats.dailyRegistrations}
                  margin={{
                    top: 30, 
                    left: 12,
                    right: 30, 
                    bottom: 10
                  }}
                >
                  <CartesianGrid vertical={false} strokeDasharray="3 3" />
                  <XAxis 
                    dataKey="date" 
                    tickLine={false}
                    axisLine={false}
                    tickMargin={8}
                    tickFormatter={(value) => value.split('-').slice(1).join('/')}
                    interval="preserveStartEnd" 
                    minTickGap={30} 
                  />
                  <YAxis 
                    tickLine={false} 
                    axisLine={false} 
                    tickMargin={8} 
                    width={30} 
                  />
                  <ChartTooltip
                    cursor={false}
                    content={<ChartTooltipContent indicator="line" />} 
                  />
                  <Line
                    dataKey="count"
                    type="monotone" 
                    stroke="#000000" // 设置折线颜色为黑色
                    strokeWidth={2} 
                    dot={true} 
                    activeDot={{ r: 6 }} 
                    connectNulls={true} 
                  >
                    <LabelList
                      position="top"
                      offset={10} 
                      className="fill-foreground text-xs"
                      formatter={(value: number) => formatUserCount(value)} 
                    />
                  </Line>
                </LineChart>
              </ChartContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                暂无用户增长数据
              </div>
            )}
          </CardContent>
           <CardFooter className="flex-col items-start gap-2 text-sm pt-4">
            <div className="leading-none text-muted-foreground">
              显示最近30天的每日注册用户数
            </div>
          </CardFooter>
        </Card>
        
        {/* 用户活跃时间分布 - 使用默认主题色 + maxBarSize */}
        <Card className="col-span-1 lg:col-span-2 flex flex-col">
          <CardHeader>
            <CardTitle>用户活跃时间分布</CardTitle>
            <CardDescription>用户在一天中的活跃时间分布 (最近7天)</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 pb-0">
            {isLoading ? (
              <div className="flex items-center justify-center h-[300px]">
                <Skeleton className="h-[300px] w-full rounded-md" />
              </div>
            ) : stats?.activityStats?.hourlyActiveDistribution ? (
              <ChartContainer 
                config={activityChartConfig} 
                className="h-[300px] w-full"
              >
                <BarChart
                  accessibilityLayer
                  data={Object.entries(stats.activityStats.hourlyActiveDistribution)
                    .map(([hour, count]) => ({ hour: parseInt(hour), count }))
                    .sort((a, b) => a.hour - b.hour)}
                  margin={{ top: 10, right: 20, left: 0, bottom: 0 }} 
                >
                  <CartesianGrid vertical={false} strokeDasharray="3 3" />
                  <XAxis 
                    dataKey="hour" 
                    tickLine={false}
                    axisLine={false}
                    tickMargin={8}
                    tickFormatter={(value) => `${value}时`}
                    interval="preserveStartEnd"
                    minTickGap={15}
                  />
                  <YAxis 
                    tickLine={false} 
                    axisLine={false} 
                    tickMargin={8} 
                    width={30} 
                  />
                  <ChartTooltip
                    cursor={false}
                    content={
                      <ChartTooltipContent 
                        indicator="line" 
                        nameKey="hour" 
                        labelFormatter={(value) => `${value}:00 - ${value}:59`} 
                      />
                    }
                  />
                  <Bar
                    dataKey="count"
                    // fill="var(--color-count)" // 移除特定颜色，使用默认主题色
                    radius={[4, 4, 0, 0]}
                    maxBarSize={60} // 限制柱子最大宽度
                  />
                </BarChart>
              </ChartContainer>
            ) : (
              <div className="flex items-center justify-center h-[300px] text-muted-foreground">
                暂无用户活跃时间分布数据
              </div>
            )}
          </CardContent>
           <CardFooter className="flex-col items-start gap-2 text-sm pt-4">
            <div className="leading-none text-muted-foreground">
              显示最近7天内，用户在各个小时段的活跃次数
            </div>
          </CardFooter>
        </Card>
      </div>
    </div>
  );
} 