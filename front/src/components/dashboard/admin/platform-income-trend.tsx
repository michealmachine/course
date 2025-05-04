'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { RefreshCw } from 'lucide-react';
import { IncomeTrendVO } from '@/types/order-stats';
import orderStatsService from '@/services/order-stats-service';
import { formatPrice } from '@/lib/utils';

// 导入图表组件
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  ResponsiveContainer
} from 'recharts';

// 导入 shadcn 图表组件
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
} from "@/components/ui/chart";

export function PlatformIncomeTrend() {
  const [loading, setLoading] = useState<boolean>(true);
  const [trendData, setTrendData] = useState<IncomeTrendVO[]>([]);
  const [timeRange, setTimeRange] = useState<string>('30d');
  const [groupBy, setGroupBy] = useState<string>('day');

  // 图表配置 - 直接使用颜色变量
  const incomeColor = 'hsl(var(--primary))';
  const refundColor = 'hsl(var(--destructive))';
  const netIncomeColor = 'hsl(var(--foreground))';

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      // 管理员加载平台收入趋势
      const data = await orderStatsService.getPlatformIncomeTrend(timeRange, groupBy);
      setTrendData(data);
    } catch (error) {
      console.error('获取收入趋势数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [timeRange, groupBy]);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <div className="space-y-1">
          <CardTitle>平台收入趋势</CardTitle>
          <CardDescription>
            查看平台不同时间段的收入、退款和净收入变化趋势
          </CardDescription>
        </div>
        <div className="flex items-center space-x-2">
          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="时间范围" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7d">最近7天</SelectItem>
              <SelectItem value="30d">最近30天</SelectItem>
              <SelectItem value="90d">最近90天</SelectItem>
            </SelectContent>
          </Select>
          <Select value={groupBy} onValueChange={setGroupBy}>
            <SelectTrigger className="w-[120px]">
              <SelectValue placeholder="分组方式" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="day">按天</SelectItem>
              <SelectItem value="week">按周</SelectItem>
              <SelectItem value="month">按月</SelectItem>
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
        ) : trendData.length > 0 ? (
          <div className="h-[300px] w-full">
            <ChartContainer
              config={{
                income: {
                  label: "收入",
                  theme: {
                    light: "hsl(0, 0%, 15%)",
                    dark: "hsl(0, 0%, 95%)",
                  },
                },
                refund: {
                  label: "退款",
                  theme: {
                    light: "hsl(0, 0%, 35%)",
                    dark: "hsl(0, 0%, 75%)",
                  },
                },
                netIncome: {
                  label: "净收入",
                  theme: {
                    light: "hsl(0, 0%, 55%)",
                    dark: "hsl(0, 0%, 55%)",
                  },
                },
              }}
              className="h-full w-full"
            >
              <LineChart
                accessibilityLayer
                data={trendData.map(item => ({
                  name: item.date,
                  收入: item.income,
                  退款: item.refund,
                  净收入: item.netIncome
                }))}
                margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
              >
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis
                  dataKey="name"
                  tickFormatter={(value) => {
                    if (groupBy === 'month') {
                      return value; // yyyy-MM
                    } else if (groupBy === 'week') {
                      return `第${value.split('-')[1]}周`; // 显示周数
                    } else {
                      return value.split('-').slice(1).join('-'); // MM-DD
                    }
                  }}
                />
                <YAxis />
                <ChartTooltip
                  content={
                    <ChartTooltipContent
                      formatter={(value) => formatPrice(Number(value))}
                    />
                  }
                />
                <ChartLegend
                  layout="horizontal"
                  verticalAlign="bottom"
                  align="center"
                  wrapperStyle={{ paddingTop: '10px' }}
                />
                <Line
                  type="monotone"
                  dataKey="收入"
                  name="收入"
                  stroke="var(--color-income)"
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                  connectNulls={true}
                />
                <Line
                  type="monotone"
                  dataKey="退款"
                  name="退款"
                  stroke="var(--color-refund)"
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                  connectNulls={true}
                />
                <Line
                  type="monotone"
                  dataKey="净收入"
                  name="净收入"
                  stroke="var(--color-netIncome)"
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                  connectNulls={true}
                />
              </LineChart>
            </ChartContainer>
          </div>
        ) : (
          <div className="w-full h-[300px] flex items-center justify-center">
            <p className="text-muted-foreground">暂无收入趋势数据</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
