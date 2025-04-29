'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { RefreshCw, PieChart as PieChartIcon } from 'lucide-react';
import { OrderStatusDistributionVO } from '@/types/order-stats';
import orderStatsService from '@/services/order-stats-service';

// 导入图表组件
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from "@/components/ui/chart";
import { type ChartConfig } from '@/components/ui/chart';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
} from 'recharts';

// 定义灰阶调色板 - 简化为单一数组
const GRAYSCALE_PALETTE = [
  'hsl(0, 0%, 15%)', // 近黑
  'hsl(0, 0%, 35%)',
  'hsl(0, 0%, 55%)',
  'hsl(0, 0%, 75%)',
  'hsl(0, 0%, 88%)', // 近白
];

interface OrderStatusDistributionProps {
  isAdmin?: boolean;
}

export function OrderStatusDistribution({ isAdmin = false }: OrderStatusDistributionProps) {
  const [loading, setLoading] = useState<boolean>(true);
  const [distributionData, setDistributionData] = useState<OrderStatusDistributionVO[]>([]);

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      // 根据用户角色加载不同的数据
      if (isAdmin) {
        // 管理员加载平台订单状态分布
        const data = await orderStatsService.getPlatformOrderStatusDistribution();
        // 过滤掉数量为0的状态
        const filteredData = data.filter(item => item.count > 0);
        setDistributionData(filteredData);
      } else {
        // 机构加载机构订单状态分布
        const data = await orderStatsService.getInstitutionOrderStatusDistribution();
        // 过滤掉数量为0的状态
        const filteredData = data.filter(item => item.count > 0);
        setDistributionData(filteredData);
      }
    } catch (error) {
      console.error('获取订单状态分布数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载
  useEffect(() => {
    loadData();
  }, []);

  // 为shadcn图表准备配置
  const chartConfig: ChartConfig = distributionData.reduce((config, item, index) => {
    config[`status_${item.status}`] = {
      label: item.statusName,
      color: GRAYSCALE_PALETTE[index % GRAYSCALE_PALETTE.length]
    };
    return config;
  }, {} as ChartConfig);

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <div className="space-y-1">
          <CardTitle>{isAdmin ? '平台订单状态分布' : '机构订单状态分布'}</CardTitle>
          <CardDescription>
            {isAdmin ? '平台' : '机构'}各状态订单数量及占比
          </CardDescription>
        </div>
        <Button variant="outline" size="icon" onClick={loadData} disabled={loading}>
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
        </Button>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="w-full h-[300px] flex items-center justify-center">
            <Skeleton className="w-full h-full" />
          </div>
        ) : distributionData.length > 0 ? (
          <ChartContainer
            config={chartConfig}
            className="h-[300px] w-full"
          >
            <PieChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
              <ChartTooltip
                content={
                  <ChartTooltipContent
                    formatter={(value, name, props) => {
                      const item = props.payload;
                      return [`${value} 个订单 (${item.percentage.toFixed(1)}%)`, item.statusName];
                    }}
                  />
                }
              />
              <Pie
                data={distributionData}
                dataKey="count"
                nameKey="statusName"
                cx="50%"
                cy="50%"
                outerRadius={100}
                labelLine={false}
              >
                {distributionData.map((entry, index) => (
                  <Cell
                    key={`cell-${index}`}
                    fill={`var(--color-status_${entry.status})`}
                    stroke="var(--background)"
                    strokeWidth={2}
                  />
                ))}
              </Pie>
              <ChartLegend content={<ChartLegendContent nameKey="statusName" />} />
            </PieChart>
          </ChartContainer>
        ) : (
          <div className="w-full h-[300px] flex items-center justify-center">
            <p className="text-muted-foreground">暂无订单状态分布数据</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
