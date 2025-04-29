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
  ResponsiveContainer,
  Legend,
  Tooltip
} from 'recharts';

interface InstitutionIncomeTrendProps {
  isAdmin?: boolean;
}

export function InstitutionIncomeTrend({ isAdmin = false }: InstitutionIncomeTrendProps) {
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
      // 根据用户角色加载不同的数据
      if (isAdmin) {
        // 管理员加载平台收入趋势
        const data = await orderStatsService.getPlatformIncomeTrend(timeRange, groupBy);
        setTrendData(data);
      } else {
        // 机构加载机构收入趋势
        const data = await orderStatsService.getInstitutionIncomeTrend(timeRange, groupBy);
        setTrendData(data);
      }
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
          <CardTitle>{isAdmin ? '平台收入趋势' : '机构收入趋势'}</CardTitle>
          <CardDescription>
            查看{isAdmin ? '平台' : '机构'}不同时间段的收入、退款和净收入变化趋势
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
            <ResponsiveContainer width="100%" height="100%">
              <LineChart
                data={trendData}
                margin={{ top: 10, right: 20, left: 20, bottom: 30 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis
                  dataKey="date"
                  tickLine={false}
                  axisLine={{ stroke: 'hsl(var(--border))' }}
                  tickMargin={8}
                  minTickGap={30}
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
                <YAxis
                  tickLine={false}
                  axisLine={{ stroke: 'hsl(var(--border))' }}
                  tickMargin={8}
                  tickFormatter={(value) => formatPrice(value)}
                />
                <Tooltip
                  formatter={(value, name) => {
                    const nameMap = {
                      income: '收入',
                      refund: '退款',
                      netIncome: '净收入'
                    };
                    return [formatPrice(Number(value)), nameMap[name as keyof typeof nameMap] || name];
                  }}
                  labelFormatter={(label) => {
                    if (groupBy === 'month') {
                      return `${label.split('-')[0]}年${label.split('-')[1]}月`;
                    } else if (groupBy === 'week') {
                      return `${label.split('-')[0]}年第${label.split('-')[1]}周`;
                    } else {
                      const parts = label.split('-');
                      return `${parts[0]}年${parts[1]}月${parts[2]}日`;
                    }
                  }}
                  separator=": "
                  itemSorter={(item) => {
                    // 确保收入、退款、净收入的顺序
                    const order = {income: 0, refund: 1, netIncome: 2};
                    return order[item.dataKey as keyof typeof order] || 0;
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="income"
                  stroke={incomeColor}
                  strokeWidth={2}
                  dot={{ r: 2 }}
                  activeDot={{ r: 4 }}
                  connectNulls={true}
                />
                <Line
                  type="monotone"
                  dataKey="refund"
                  stroke={refundColor}
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                  connectNulls={true}
                />
                <Line
                  type="monotone"
                  dataKey="netIncome"
                  stroke={netIncomeColor}
                  strokeWidth={2}
                  dot={{ r: 4 }}
                  activeDot={{ r: 6 }}
                  connectNulls={true}
                />
                <Legend />
              </LineChart>
            </ResponsiveContainer>
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
