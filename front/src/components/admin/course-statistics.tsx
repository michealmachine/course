'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';
import { Progress } from '@/components/ui/progress';
import {
  Users,
  Clock,
  BarChart2,
  TrendingUp,
  Calendar
} from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { ApiResponse } from '@/types/api';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell
} from 'recharts';

interface CourseStatisticsProps {
  courseId: number;
  stats: any;
}

export function CourseStatistics({ courseId, stats }: CourseStatisticsProps) {
  const [activeTab, setActiveTab] = useState('overview');
  const [learningStats, setLearningStats] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (courseId) {
      fetchLearningStats();
    }
  }, [courseId]);

  const fetchLearningStats = async () => {
    setIsLoading(true);
    try {
      // 生成模拟数据，因为后端API尚未实现
      // 生成过去7天的日期
      const trendData = Array.from({ length: 7 }, (_, i) => {
        const date = new Date();
        date.setDate(date.getDate() - (6 - i));
        return {
          date: date.toISOString().split('T')[0],
          // 生成随机学习人数，但保持一定的趋势
          count: Math.floor(Math.random() * 20) + (i * 2) // 随机值加上递增趋势
        };
      });

      // 生成24小时的活跃时段数据
      const hourlyData = Array.from({ length: 24 }, (_, i) => {
        // 模拟真实的活跃时段分布：早上和晚上活跃度高
        let baseCount = 5;
        if (i >= 8 && i <= 11) baseCount = 15; // 上午高峰
        if (i >= 19 && i <= 22) baseCount = 20; // 晚上高峰
        if (i >= 0 && i <= 5) baseCount = 2; // 凌晨低谷

        return {
          hour: i,
          count: Math.floor(Math.random() * (baseCount / 2)) + baseCount
        };
      });

      // 设置学习统计数据
      setLearningStats({
        trendData,
        hourlyData
      });

      // 模拟API调用延迟
      await new Promise(resolve => setTimeout(resolve, 500));

    } catch (error) {
      console.error('获取学习统计数据出错:', error);
      toast.error('获取学习统计数据出错');
    } finally {
      setIsLoading(false);
    }
  };

  const formatDuration = (seconds: number) => {
    if (!seconds) return '0分钟';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`;
    }
    return `${minutes}分钟`;
  };

  // 模拟数据 - 学习时长分布
  const durationDistributionData = [
    { name: '0-10分钟', value: 20 },
    { name: '10-30分钟', value: 35 },
    { name: '30-60分钟', value: 25 },
    { name: '1-2小时', value: 15 },
    { name: '2小时以上', value: 5 },
  ];

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-medium">学习统计</h3>

      {/* 统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">学习人数</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats ? stats.totalLearners.toLocaleString() : <Skeleton className="h-8 w-20" />}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">完成率</CardTitle>
            <BarChart2 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats ? `${(stats.completionRate * 100).toFixed(0)}%` : <Skeleton className="h-8 w-20" />}
            </div>
            {stats && (
              <Progress value={stats.completionRate * 100} className="h-2 mt-2" />
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">平均学习时长</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats ? formatDuration(stats.averageDuration) : <Skeleton className="h-8 w-20" />}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">今日学习人数</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {stats ? '15' : <Skeleton className="h-8 w-20" />}
            </div>
            {stats && (
              <p className="text-xs text-muted-foreground mt-1">
                较昨日 <span className="text-green-600">+20%</span>
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList>
          <TabsTrigger value="overview">概览</TabsTrigger>
          <TabsTrigger value="trends">趋势分析</TabsTrigger>
          <TabsTrigger value="distribution">分布分析</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>学习人数趋势</CardTitle>
                <CardDescription>近7天学习人数变化</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoading ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : learningStats ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                      data={learningStats.trendData}
                      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis
                        dataKey="date"
                        tickFormatter={(value) => {
                          const date = new Date(value);
                          return `${date.getMonth() + 1}/${date.getDate()}`;
                        }}
                      />
                      <YAxis />
                      <Tooltip
                        formatter={(value) => [`${value} 人`, '学习人数']}
                        labelFormatter={(label) => {
                          const date = new Date(label);
                          return date.toLocaleDateString();
                        }}
                      />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="count"
                        name="学习人数"
                        stroke="#8884d8"
                        strokeWidth={2}
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex items-center justify-center h-full text-muted-foreground">
                    暂无数据
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>学习时长分布</CardTitle>
                <CardDescription>学习者的学习时长分布</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoading ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={durationDistributionData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {durationDistributionData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => [`${value}%`, '占比']} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="trends" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>学习人数趋势</CardTitle>
              <CardDescription>近7天学习人数变化</CardDescription>
            </CardHeader>
            <CardContent className="h-96">
              {isLoading ? (
                <div className="flex items-center justify-center h-full">
                  <Skeleton className="h-full w-full" />
                </div>
              ) : learningStats ? (
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart
                    data={learningStats.trendData}
                    margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="date"
                      tickFormatter={(value) => {
                        const date = new Date(value);
                        return `${date.getMonth() + 1}/${date.getDate()}`;
                      }}
                    />
                    <YAxis />
                    <Tooltip
                      formatter={(value) => [`${value} 人`, '学习人数']}
                      labelFormatter={(label) => {
                        const date = new Date(label);
                        return date.toLocaleDateString();
                      }}
                    />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="count"
                      name="学习人数"
                      stroke="#8884d8"
                      strokeWidth={2}
                      dot={{ r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="flex items-center justify-center h-full text-muted-foreground">
                  暂无数据
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="distribution" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>学习时长分布</CardTitle>
                <CardDescription>学习者的学习时长分布</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoading ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={durationDistributionData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        outerRadius={80}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {durationDistributionData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => [`${value}%`, '占比']} />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>活跃时段分布</CardTitle>
                <CardDescription>学习者的活跃时段分布</CardDescription>
              </CardHeader>
              <CardContent className="h-80">
                {isLoading ? (
                  <div className="flex items-center justify-center h-full">
                    <Skeleton className="h-full w-full" />
                  </div>
                ) : learningStats ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                      data={learningStats.hourlyData}
                      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis
                        dataKey="hour"
                        tickFormatter={(value) => `${value}:00`}
                      />
                      <YAxis />
                      <Tooltip
                        formatter={(value) => [`${value} 人`, '学习人数']}
                        labelFormatter={(label) => `${label}:00 - ${label}:59`}
                      />
                      <Legend />
                      <Bar
                        dataKey="count"
                        name="学习人数"
                        fill="#8884d8"
                      />
                    </BarChart>
                  </ResponsiveContainer>
                ) : (
                  <div className="flex items-center justify-center h-full text-muted-foreground">
                    暂无数据
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>学习进度分布</CardTitle>
              <CardDescription>学习者的学习进度分布</CardDescription>
            </CardHeader>
            <CardContent className="h-80">
              {isLoading ? (
                <div className="flex items-center justify-center h-full">
                  <Skeleton className="h-full w-full" />
                </div>
              ) : stats ? (
                <div className="flex flex-col h-full justify-center">
                  <div className="space-y-6">
                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>未开始</span>
                        <span>{(stats.progressDistribution.notStarted * 100).toFixed(0)}%</span>
                      </div>
                      <Progress value={stats.progressDistribution.notStarted * 100} className="h-3" />
                    </div>

                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>学习中</span>
                        <span>{(stats.progressDistribution.inProgress * 100).toFixed(0)}%</span>
                      </div>
                      <Progress value={stats.progressDistribution.inProgress * 100} className="h-3" />
                    </div>

                    <div className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span>已完成</span>
                        <span>{(stats.progressDistribution.completed * 100).toFixed(0)}%</span>
                      </div>
                      <Progress value={stats.progressDistribution.completed * 100} className="h-3" />
                    </div>
                  </div>
                </div>
              ) : (
                <div className="flex items-center justify-center h-full text-muted-foreground">
                  暂无数据
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
