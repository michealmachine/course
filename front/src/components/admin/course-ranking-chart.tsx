'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { RefreshCw } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, LabelList, Legend } from 'recharts';
import { ChartContainer, ChartTooltip, ChartTooltipContent } from '@/components/ui/chart';
import { type ChartConfig } from '@/components/ui/chart';
import adminLearningStatsService from '@/services/admin-learning-stats-service';
import { CourseStatisticsVO } from '@/types/institution-stats';

interface CourseRankingChartProps {
  institutionId?: number;
}

export function CourseRankingChart({ institutionId }: CourseRankingChartProps) {
  const [loading, setLoading] = useState<boolean>(true);
  const [rankingData, setRankingData] = useState<CourseStatisticsVO[]>([]);
  const [limit, setLimit] = useState<number>(10);
  const [sortBy, setSortBy] = useState<string>('learnerCount');
  const [viewMode, setViewMode] = useState<'chart' | 'table'>('chart');

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      const data = await adminLearningStatsService.getCourseRanking(sortBy, institutionId, limit);
      setRankingData(data || []);
    } catch (error) {
      console.error('获取课程排名数据失败:', error);
      setRankingData([]); // 确保在错误情况下设置为空数组而不是 null
    } finally {
      setLoading(false);
    }
  };

  // 初始加载和参数变化时重新加载
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sortBy, limit, institutionId]);

  // 为图表准备数据
  const chartData = rankingData && rankingData.length > 0
    ? rankingData
        .slice(0, limit)
        .map(course => ({
          ...course,
          shortTitle: course.courseTitle?.length > 15
            ? course.courseTitle.substring(0, 15) + '...'
            : (course.courseTitle || '未命名课程'),
          displayValue: getSortByValue(course, sortBy)
        }))
    : [];

  // 获取排序字段的值
  function getSortByValue(course: CourseStatisticsVO, sortBy: string): number {
    switch (sortBy) {
      case 'learnerCount':
        return course.learnerCount || 0;
      case 'totalDuration':
        return course.totalDuration || 0;
      case 'activityCount':
        return course.activityCount || 0;
      case 'completionCount':
        return course.completionCount || 0;
      default:
        return 0;
    }
  }

  // 格式化显示值
  function formatValue(value: number, type: string): string {
    switch (type) {
      case 'learnerCount':
      case 'activityCount':
      case 'completionCount':
        return `${value}`;
      case 'totalDuration':
        return formatDuration(value);
      default:
        return `${value}`;
    }
  }

  // 格式化时长（秒转为小时:分钟:秒）
  function formatDuration(seconds: number): string {
    if (seconds < 60) return `${seconds}秒`;

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? minutes + '分' : ''}`;
    } else {
      return `${minutes}分钟`;
    }
  }

  // 获取排序字段的标签
  function getSortByLabel(sortBy: string): string {
    switch (sortBy) {
      case 'learnerCount':
        return '学习人数';
      case 'totalDuration':
        return '学习时长';
      case 'activityCount':
        return '活动次数';
      case 'completionCount':
        return '完成人数';
      default:
        return '数值';
    }
  }

  // 图表配置
  const chartConfig: ChartConfig = {
    value: {
      label: getSortByLabel(sortBy),
      theme: {
        light: "hsl(var(--primary))",
        dark: "hsl(var(--primary))",
      },
    }
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <div className="space-y-1">
          <CardTitle>课程{getSortByLabel(sortBy)}排行</CardTitle>
          <CardDescription>
            按{getSortByLabel(sortBy)}排序的热门课程
          </CardDescription>
        </div>
        <div className="flex items-center space-x-2">
          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="排序方式" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="learnerCount">学习人数</SelectItem>
              <SelectItem value="totalDuration">学习时长</SelectItem>
              <SelectItem value="activityCount">活动次数</SelectItem>
              <SelectItem value="completionCount">完成人数</SelectItem>
            </SelectContent>
          </Select>
          <Select value={limit.toString()} onValueChange={(value) => setLimit(parseInt(value))}>
            <SelectTrigger className="w-[100px]">
              <SelectValue placeholder="显示数量" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="5">前5名</SelectItem>
              <SelectItem value="10">前10名</SelectItem>
              <SelectItem value="20">前20名</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" size="icon" onClick={loadData} disabled={loading}>
            <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="w-full h-[300px] flex items-center justify-center">
            <Skeleton className="w-full h-full" />
          </div>
        ) : rankingData && rankingData.length > 0 ? (
          viewMode === 'chart' ? (
            <div className="h-[300px] overflow-x-auto">
              <div style={{ minWidth: `${Math.max(600, chartData.length * 100)}px`, height: '100%' }}>
                <ChartContainer
                  config={chartConfig}
                  className="h-full w-full"
                >
                  <BarChart
                    data={chartData}
                    layout="horizontal"
                    margin={{ top: 10, right: 20, left: 20, bottom: 80 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                      dataKey="shortTitle"
                      angle={-45}
                      textAnchor="end"
                      height={80}
                      interval={0}
                      tickLine={false}
                    />
                    <YAxis
                      type="number"
                      tickFormatter={(value) => formatValue(value, sortBy)}
                    />
                    <ChartTooltip
                      content={
                        <ChartTooltipContent
                          formatter={(value) => formatValue(Number(value), sortBy)}
                          labelFormatter={(label, payload) => {
                            const item = payload?.[0]?.payload;
                            return item ? item.courseTitle : label;
                          }}
                        />
                      }
                    />
                    <Legend
                      verticalAlign="top"
                      align="center"
                      wrapperStyle={{ paddingBottom: '10px' }}
                      payload={[
                        {
                          value: getSortByLabel(sortBy),
                          type: 'rect',
                          color: 'var(--color-value)'
                        }
                      ]}
                    />
                    <Bar
                      dataKey="displayValue"
                      name={getSortByLabel(sortBy)}
                      fill="var(--color-value)"
                      radius={[4, 4, 0, 0]}
                      maxBarSize={50}
                    >
                      <LabelList
                        dataKey="displayValue"
                        position="top"
                        formatter={(value: number) => formatValue(value, sortBy)}
                        style={{ fill: 'hsl(var(--foreground))' }}
                      />
                    </Bar>
                  </BarChart>
                </ChartContainer>
              </div>
            </div>
          ) : (
            <div className="h-[300px] overflow-y-auto pr-2">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="w-12 text-left py-2">排名</th>
                    <th className="text-left py-2">课程名称</th>
                    <th className="w-24 text-right py-2">{getSortByLabel(sortBy)}</th>
                  </tr>
                </thead>
                <tbody>
                  {chartData.map((course, index) => (
                    <tr key={course.courseId} className="border-b hover:bg-muted/50">
                      <td className="py-3 text-center font-medium">{index + 1}</td>
                      <td className="py-3">{course.courseTitle}</td>
                      <td className="py-3 text-right">{formatValue(course.displayValue, sortBy)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        ) : (
          <div className="flex items-center justify-center h-[300px]">
            <p className="text-muted-foreground">暂无排名数据</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
