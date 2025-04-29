'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { RefreshCw, BarChart as BarChartIcon } from 'lucide-react';
import { CourseIncomeRankingVO } from '@/types/order-stats';
import orderStatsService from '@/services/order-stats-service';
import { formatPrice } from '@/lib/utils';
import Link from 'next/link';

// 导入图表组件
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { type ChartConfig } from '@/components/ui/chart';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  ResponsiveContainer,
  LabelList,
} from 'recharts';

interface CourseIncomeRankingProps {
  isAdmin?: boolean;
}

export function CourseIncomeRanking({ isAdmin = false }: CourseIncomeRankingProps) {
  const [loading, setLoading] = useState<boolean>(true);
  const [rankingData, setRankingData] = useState<CourseIncomeRankingVO[]>([]);
  const [limit, setLimit] = useState<number>(10);
  const [viewMode, setViewMode] = useState<'chart' | 'table'>('chart');

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      // 根据用户角色加载不同的数据
      if (isAdmin) {
        // 管理员加载平台课程收入排行
        const data = await orderStatsService.getPlatformCourseIncomeRanking(limit);
        setRankingData(data);
      } else {
        // 机构加载机构课程收入排行
        const data = await orderStatsService.getInstitutionCourseIncomeRanking(limit);
        setRankingData(data);
      }
    } catch (error) {
      console.error('获取课程收入排行数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [limit]);

  // 为图表准备数据
  const chartData = rankingData
    .slice(0, limit) // 显示指定数量
    .map(course => ({
      ...course,
      // 不再截断课程名称
      shortTitle: course.courseTitle,
      // 如果是管理员视图且有机构名称，则在标题中添加机构名称
      displayTitle: isAdmin && 'institutionName' in course
        ? `${course.courseTitle} (${course.institutionName})`
        : course.courseTitle
    }))
    .sort((a, b) => b.income - a.income); // 从大到小排序

  // 图表配置
  const chartConfig: ChartConfig = {
    income: {
      label: '收入',
      color: 'hsl(var(--primary))'
    }
  };

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <div className="space-y-1">
          <CardTitle>{isAdmin ? '平台课程收入排行' : '机构课程收入排行'}</CardTitle>
          <CardDescription>
            按收入金额排序的{isAdmin ? '平台' : '机构'}热门课程
          </CardDescription>
        </div>
        <div className="flex items-center space-x-2">
          <Select value={viewMode} onValueChange={(value) => setViewMode(value as 'chart' | 'table')}>
            <SelectTrigger className="w-[100px]">
              <SelectValue placeholder="显示方式" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="chart">图表</SelectItem>
              <SelectItem value="table">表格</SelectItem>
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
        ) : rankingData.length > 0 ? (
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
                      dataKey="displayTitle"
                      angle={-45}
                      textAnchor="end"
                      height={80}
                      interval={0}
                      tickLine={false}
                    />
                    <YAxis
                      type="number"
                      tickFormatter={(value) => formatPrice(value)}
                    />
                    <ChartTooltip
                      content={
                        <ChartTooltipContent
                          formatter={(value) => formatPrice(Number(value))}
                          labelFormatter={(label, payload) => {
                            const item = payload?.[0]?.payload;
                            return item ? (item.displayTitle || item.courseTitle) : label;
                          }}
                        />
                      }
                    />
                    <Bar
                      dataKey="income"
                      fill="var(--color-income)"
                      radius={[4, 4, 0, 0]}
                      maxBarSize={50}
                    >
                      <LabelList
                        dataKey="income"
                        position="top"
                        formatter={(value: number) => formatPrice(value)}
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
                    {isAdmin && <th className="text-left py-2">机构名称</th>}
                    <th className="w-24 text-right py-2">收入</th>
                  </tr>
                </thead>
                <tbody>
                  {[...rankingData]
                    .sort((a, b) => b.income - a.income) // 从大到小排序，与图表保持一致
                    .map((course, index) => (
                    <tr key={course.courseId} className="border-b hover:bg-muted/50">
                      <td className="py-3 text-center font-medium">{index + 1}</td>
                      <td className="py-3">
                        <Link
                          href={`/dashboard/courses/${course.courseId}`}
                          className="hover:underline flex items-center"
                        >
                          <div className="w-8 h-8 mr-2 bg-muted rounded-md flex-shrink-0 overflow-hidden relative">
                            <div className="w-full h-full flex items-center justify-center">
                              <BarChartIcon className="h-4 w-4 text-muted-foreground" />
                            </div>
                          </div>
                          <span>{course.courseTitle}</span>
                        </Link>
                      </td>
                      {isAdmin && (
                        <td className="py-3">
                          {/* 使用类型断言访问AdminCourseIncomeRankingVO特有的属性 */}
                          {('institutionName' in course) && (
                            <Link
                              href={`/dashboard/admin/institutions/${('institutionId' in course) ? course.institutionId : ''}`}
                              className="hover:underline"
                            >
                              {course.institutionName}
                            </Link>
                          )}
                        </td>
                      )}
                      <td className="py-3 text-right font-medium">
                        {formatPrice(course.income)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        ) : (
          <div className="w-full h-[300px] flex items-center justify-center">
            <p className="text-muted-foreground">暂无课程收入排行数据</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
