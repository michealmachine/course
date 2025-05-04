'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import {
  Users,
  BookOpen,
  Clock,
  DollarSign,
  TrendingUp,
  BarChart2,
  PieChart,
  Activity
} from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { ApiResponse } from '@/types/api';
// 导入服务
import adminInstitutionStatsService from '@/services/admin-institution-stats-service';

// 导入 recharts 基础组件
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  PieChart as RechartsPieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend as RechartsLegend,
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

interface InstitutionStatisticsProps {
  institutionId: number;
  stats: any;
}

export function InstitutionStatistics({ institutionId, stats }: InstitutionStatisticsProps) {
  const [activeTab, setActiveTab] = useState('overview');
  const [learningStats, setLearningStats] = useState<any>(null);
  const [incomeStats, setIncomeStats] = useState<any>(null);
  const [isLoadingLearning, setIsLoadingLearning] = useState(false);
  const [isLoadingIncome, setIsLoadingIncome] = useState(false);

  // 图表数据状态
  const [learningTrendData, setLearningTrendData] = useState<any[]>([
    { name: '1月', 学习时长: 0 },
    { name: '2月', 学习时长: 0 },
    { name: '3月', 学习时长: 0 }
  ]);
  const [activityTypeData, setActivityTypeData] = useState<any[]>([
    { name: '视频学习', value: 0 },
    { name: '阅读文档', value: 0 },
    { name: '做练习', value: 0 }
  ]);
  const [incomeTrendData, setIncomeTrendData] = useState<any[]>([
    { name: '1月', 收入: 0 },
    { name: '2月', 收入: 0 },
    { name: '3月', 收入: 0 }
  ]);
  const [courseIncomeData, setCourseIncomeData] = useState<any[]>([
    { name: '课程1', 收入: 0 },
    { name: '课程2', 收入: 0 },
    { name: '课程3', 收入: 0 }
  ]);

  useEffect(() => {
    if (institutionId) {
      console.log('管理员组件 - useEffect 触发，institutionId:', institutionId);
      fetchLearningStats();
      fetchIncomeStats();
    }
  }, [institutionId]);

  // 监听 stats 变化
  useEffect(() => {
    console.log('接收到的统计数据:', stats);
  }, [stats]);

  // 定义图表配置
  const chartConfig: ChartConfig = {
    learning: {
      label: "学习时长",
      theme: {
        light: "hsl(0, 0%, 15%)",
        dark: "hsl(0, 0%, 95%)",
      },
    },
    income: {
      label: "收入",
      theme: {
        light: "hsl(0, 0%, 35%)",
        dark: "hsl(0, 0%, 75%)",
      },
    },
    video: {
      label: "视频学习",
      theme: {
        light: "hsl(0, 0%, 15%)",
        dark: "hsl(0, 0%, 95%)",
      },
    },
    document: {
      label: "阅读文档",
      theme: {
        light: "hsl(0, 0%, 35%)",
        dark: "hsl(0, 0%, 75%)",
      },
    },
    exercise: {
      label: "做练习",
      theme: {
        light: "hsl(0, 0%, 55%)",
        dark: "hsl(0, 0%, 55%)",
      },
    },
    discussion: {
      label: "讨论",
      theme: {
        light: "hsl(0, 0%, 75%)",
        dark: "hsl(0, 0%, 35%)",
      },
    },
  };

  const fetchLearningStats = async () => {
    setIsLoadingLearning(true);
    try {
      console.log('管理员组件 - 获取学习统计数据，机构ID:', institutionId);

      // 获取机构学习统计概览
      const learningStatsData = await adminInstitutionStatsService.getStatisticsOverview(institutionId);

      // 获取每日学习统计
      const now = new Date();
      const startDate = new Date(now);
      startDate.setDate(now.getDate() - 30);

      const dailyStats = await adminInstitutionStatsService.getDailyStatistics(
        institutionId,
        startDate.toISOString().split('T')[0],
        now.toISOString().split('T')[0]
      );

      // 获取活动类型统计
      const activityStats = await adminInstitutionStatsService.getActivityTypeStatistics(institutionId);

      // 设置学习统计数据
      setLearningStats({
        totalLearningDuration: learningStatsData.totalLearningDuration || 0,
        averageDailyDuration: learningStatsData.averageDailyDuration || 0,
        activeUsers: learningStatsData.activeUsers || 0,
        completionRate: learningStatsData.completionRate || 0
      });

      // 设置每日学习统计数据
      if (dailyStats && dailyStats.length > 0) {
        setLearningTrendData(dailyStats.map(item => ({
          name: item.date,
          学习时长: item.durationSeconds / 60 // 转换为分钟
        })));
      }

      // 设置活动类型统计数据
      if (activityStats && activityStats.length > 0) {
        setActivityTypeData(activityStats.map(item => ({
          name: item.activityTypeDescription || item.activityType,
          value: (item.percentage || 0) * 100 // 转换为百分比
        })));
      }

    } catch (error) {
      console.error('获取学习统计数据出错:', error);
      toast.error('获取学习统计数据失败');
    } finally {
      setIsLoadingLearning(false);
    }
  };

  const fetchIncomeStats = async () => {
    setIsLoadingIncome(true);
    try {
      console.log('管理员组件 - 获取收入统计数据，机构ID:', institutionId);

      // 获取机构收入趋势
      const incomeTrend = await adminInstitutionStatsService.getIncomeTrend(institutionId, '90d', 'month');

      // 获取机构课程收入排行
      const courseIncomeRanking = await adminInstitutionStatsService.getCourseIncomeRanking(institutionId, 5);

      // 获取机构订单状态分布
      const orderStatusDistribution = await adminInstitutionStatsService.getOrderStatusDistribution(institutionId);

      // 计算总收入和月收入
      let totalIncome = 0;
      let monthIncome = 0;

      if (incomeTrend && incomeTrend.length > 0) {
        // 计算总收入（所有数据的总和）
        totalIncome = incomeTrend.reduce((sum, item) => {
          // 确保 income 是数字
          const incomeValue = typeof item.income === 'number' ? item.income :
                            (typeof item.income === 'string' ? parseFloat(item.income) :
                            (item.income ? parseFloat(item.income.toString()) : 0));
          return sum + incomeValue;
        }, 0);

        // 计算月收入（最近30天的总和）
        monthIncome = totalIncome;

        // 设置收入趋势数据
        setIncomeTrendData(incomeTrend.map(item => {
          // 确保 income 是数字
          const incomeValue = typeof item.income === 'number' ? item.income :
                            (typeof item.income === 'string' ? parseFloat(item.income) :
                            (item.income ? parseFloat(item.income.toString()) : 0));
          return {
            name: item.date,
            收入: incomeValue
          };
        }));
      }

      // 设置课程收入排行数据
      if (courseIncomeRanking && courseIncomeRanking.length > 0) {
        setCourseIncomeData(courseIncomeRanking.map(item => ({
          name: item.courseName,
          收入: item.income
        })));
      }

      // 计算退款率
      let refundRate = 0;
      if (orderStatusDistribution && orderStatusDistribution.length > 0) {
        const refundedOrders = orderStatusDistribution.find(item => item.status === 'REFUNDED');
        const totalOrders = orderStatusDistribution.reduce((sum, item) => sum + item.count, 0);

        if (refundedOrders && totalOrders > 0) {
          refundRate = refundedOrders.count / totalOrders;
        }
      }

      // 设置收入统计数据
      setIncomeStats({
        totalIncome,
        monthIncome,
        refundRate,
        topCourses: courseIncomeRanking || []
      });

    } catch (error) {
      console.error('获取收入统计数据出错:', error);
      toast.error('获取收入统计数据失败');
    } finally {
      setIsLoadingIncome(false);
    }
  };

  // 默认颜色
  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  const formatDuration = (seconds: number) => {
    // 确保 seconds 是数字，如果不是或者是 NaN，则设为 0
    const duration = typeof seconds === 'number' && !isNaN(seconds) ? seconds : 0;

    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60);

    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`;
    }
    return `${minutes}分钟`;
  };

  const formatMoney = (cents: number) => {
    if (!cents) return '¥0';
    return `¥${(cents / 100).toFixed(2)}`;
  };

  return (
    <div className="space-y-4 w-full">
      <h3 className="text-lg font-medium">统计数据</h3>

      {/* 统计卡片 */}
      <div className="grid gap-4 grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">用户数量</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats && stats.userCount !== undefined ? stats.userCount.toLocaleString() : <Skeleton className="h-8 w-20" />}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">课程数量</CardTitle>
            <BookOpen className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats && stats.publishedCourseCount !== undefined && stats.courseCount !== undefined ? (
                <>
                  {stats.publishedCourseCount.toLocaleString()}/{stats.courseCount.toLocaleString()}
                </>
              ) : (
                <Skeleton className="h-8 w-20" />
              )}
            </div>
            {stats && stats.publishedCourseCount !== undefined && (
              <p className="text-xs text-muted-foreground mt-1">
                已发布/总数
              </p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总学习时长</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats && stats.totalLearningDuration !== undefined ? formatDuration(stats.totalLearningDuration) : <Skeleton className="h-8 w-20" />}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总收入</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats && stats.totalIncome !== undefined ? formatMoney(stats.totalIncome) : <Skeleton className="h-8 w-20" />}
            </div>
            {stats && stats.monthIncome !== undefined && (
              <p className="text-xs text-muted-foreground mt-1">
                本月: {formatMoney(stats.monthIncome)}
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">概览</TabsTrigger>
          <TabsTrigger value="learning">学习统计</TabsTrigger>
          <TabsTrigger value="income">收入统计</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>学习时长趋势</CardTitle>
                <CardDescription>近7个月学习时长变化</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoadingLearning ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ChartContainer
                    config={chartConfig}
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
                      <ChartLegend />
                      <Line
                        type="monotone"
                        dataKey="学习时长"
                        name="学习时长"
                        stroke="var(--color-learning)"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ChartContainer>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>收入趋势</CardTitle>
                <CardDescription>近7个月收入变化</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoadingIncome ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ChartContainer
                    config={chartConfig}
                    className="h-full w-full"
                  >
                    <LineChart
                      accessibilityLayer
                      data={incomeTrendData}
                      margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <ChartTooltip
                        content={
                          <ChartTooltipContent
                            formatter={(value) => `¥${(value / 100).toFixed(2)}`}
                          />
                        }
                      />
                      <ChartLegend />
                      <Line
                        type="monotone"
                        dataKey="收入"
                        name="收入"
                        stroke="var(--color-income)"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ChartContainer>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="learning" className="space-y-4">
          <div className="grid gap-4 grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>学习时长趋势</CardTitle>
                <CardDescription>近7个月学习时长变化</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoadingLearning ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ChartContainer
                    config={chartConfig}
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
                      <ChartLegend />
                      <Line
                        type="monotone"
                        dataKey="学习时长"
                        name="学习时长"
                        stroke="var(--color-learning)"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ChartContainer>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>活动类型分布</CardTitle>
                <CardDescription>不同学习活动的时间分布</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoadingLearning ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ChartContainer
                    config={chartConfig}
                    className="h-full w-full"
                  >
                    <RechartsPieChart>
                      <Pie
                        data={activityTypeData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        outerRadius={80}
                        dataKey="value"
                      >
                        {activityTypeData.map((entry, index) => {
                          // 使用配置中的颜色
                          const colorKey = Object.keys(chartConfig)[index % Object.keys(chartConfig).length];
                          return (
                            <Cell
                              key={`cell-${index}`}
                              fill={`var(--color-${colorKey})`}
                            />
                          );
                        })}
                      </Pie>
                      <ChartTooltip
                        content={
                          <ChartTooltipContent
                            formatter={(value) => `${value}%`}
                          />
                        }
                      />
                      <ChartLegend />
                    </RechartsPieChart>
                  </ChartContainer>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="income" className="space-y-4">
          <div className="grid gap-4 grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>收入趋势</CardTitle>
                <CardDescription>近7个月收入变化</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoadingIncome ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ChartContainer
                    config={chartConfig}
                    className="h-full w-full"
                  >
                    <LineChart
                      accessibilityLayer
                      data={incomeTrendData}
                      margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <ChartTooltip
                        content={
                          <ChartTooltipContent
                            formatter={(value) => `¥${(value / 100).toFixed(2)}`}
                          />
                        }
                      />
                      <ChartLegend />
                      <Line
                        type="monotone"
                        dataKey="收入"
                        name="收入"
                        stroke="var(--color-income)"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ChartContainer>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>课程收入排行</CardTitle>
                <CardDescription>收入最高的5门课程</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoadingIncome ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ChartContainer
                    config={chartConfig}
                    className="h-full w-full"
                  >
                    <BarChart
                      accessibilityLayer
                      data={courseIncomeData}
                      layout="vertical"
                      margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis type="number" />
                      <YAxis type="category" dataKey="name" width={100} />
                      <ChartTooltip
                        content={
                          <ChartTooltipContent
                            formatter={(value) => `¥${(value / 100).toFixed(2)}`}
                          />
                        }
                      />
                      <ChartLegend />
                      <Bar dataKey="收入" name="收入" fill="var(--color-income)" />
                    </BarChart>
                  </ChartContainer>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
