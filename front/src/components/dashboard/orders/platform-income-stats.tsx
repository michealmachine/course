'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Button } from '@/components/ui/button';
import { DatePicker } from '@/components/ui/date-picker';
import { RefreshCw, Calendar, Loader2 } from 'lucide-react';
import { PlatformIncomeStatsVO } from '@/types/order-stats';
import orderStatsService from '@/services/order-stats-service';
import { formatPrice } from '@/lib/utils';

export function PlatformIncomeStats() {
  const [loading, setLoading] = useState<boolean>(true);
  const [statsData, setStatsData] = useState<PlatformIncomeStatsVO>({
    totalIncome: 0,
    totalRefund: 0,
    netIncome: 0,
    orderCount: 0,
    paidOrderCount: 0,
    refundOrderCount: 0
  });

  // 自定义时间范围
  const [startDate, setStartDate] = useState<Date | undefined>(undefined);
  const [endDate, setEndDate] = useState<Date | undefined>(undefined);
  const [isCustomRange, setIsCustomRange] = useState<boolean>(false);

  // 加载数据
  const loadData = async () => {
    setLoading(true);
    try {
      let startDateStr: string | undefined;
      let endDateStr: string | undefined;

      if (isCustomRange && startDate && endDate) {
        startDateStr = startDate.toISOString();
        endDateStr = endDate.toISOString();
      }

      const data = await orderStatsService.getPlatformIncomeStats(startDateStr, endDateStr);
      setStatsData(data);
    } catch (error) {
      console.error('获取平台收入统计数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载
  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 切换自定义时间范围
  const toggleCustomRange = () => {
    setIsCustomRange(!isCustomRange);
    if (!isCustomRange) {
      // 默认设置为最近30天
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 30);
      setStartDate(start);
      setEndDate(end);
    } else {
      // 重置为全部时间
      setStartDate(undefined);
      setEndDate(undefined);
      loadData();
    }
  };

  // 应用自定义时间范围
  const applyCustomRange = () => {
    if (startDate && endDate) {
      loadData();
    }
  };

  // 计算支付率和退款率
  const paymentRate = statsData.orderCount > 0
    ? (statsData.paidOrderCount / statsData.orderCount * 100).toFixed(1)
    : '0.0';

  const refundRate = statsData.paidOrderCount > 0
    ? (statsData.refundOrderCount / statsData.paidOrderCount * 100).toFixed(1)
    : '0.0';

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-4 border-b">
        <div>
          <div className="flex items-center gap-2">
            <CardTitle className="text-xl">平台收入统计</CardTitle>
            {loading && <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />}
          </div>
          <CardDescription className="mt-1">
            {isCustomRange && startDate && endDate
              ? `${startDate.toLocaleDateString()} 至 ${endDate.toLocaleDateString()}`
              : '全部时间'}
          </CardDescription>
        </div>
        <div className="flex flex-col gap-2">
          {isCustomRange ? (
            <div className="flex items-center gap-2">
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <DatePicker
                    date={startDate}
                    setDate={setStartDate}
                    className="w-full"
                    placeholder="开始日期"
                  />
                </div>
                <div>
                  <DatePicker
                    date={endDate}
                    setDate={setEndDate}
                    className="w-full"
                    placeholder="结束日期"
                  />
                </div>
              </div>
              <Button
                onClick={applyCustomRange}
                disabled={!startDate || !endDate || loading}
                size="sm"
                className="h-8"
              >
                应用
              </Button>
              <Button variant="outline" size="sm" onClick={toggleCustomRange} className="h-8">
                取消
              </Button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="outline" size="sm" onClick={toggleCustomRange} className="h-8">
                <Calendar className="h-3.5 w-3.5 mr-1.5" />
                自定义时间
              </Button>
              <Button variant="ghost" size="icon" onClick={loadData} disabled={loading} className="h-8 w-8">
                <RefreshCw className={`h-3.5 w-3.5 ${loading ? 'animate-spin' : ''}`} />
              </Button>
            </div>
          )}
        </div>
      </CardHeader>
      <CardContent>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {[...Array(9)].map((_, i) => (
              <Card key={i} className="overflow-hidden">
                <CardContent className="p-6">
                  <Skeleton className="h-4 w-24 mb-2" />
                  <Skeleton className="h-8 w-32" />
                </CardContent>
              </Card>
            ))}
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {/* 收入统计卡片 - 主要指标 */}
            <StatsCard
              title="总收入"
              value={formatPrice(statsData.totalIncome)}
              className="border-l-4 border-l-green-500"
            />
            <StatsCard
              title="总退款"
              value={formatPrice(statsData.totalRefund)}
              className="border-l-4 border-l-red-500"
            />
            <StatsCard
              title="净收入"
              value={formatPrice(statsData.netIncome)}
              className="border-l-4 border-l-blue-500"
            />

            {/* 订单统计卡片 */}
            <StatsCard
              title="订单总数"
              value={statsData.orderCount.toString()}
              className="border-l-4 border-l-gray-500"
            />
            <StatsCard
              title="已支付订单"
              value={statsData.paidOrderCount.toString()}
              className="border-l-4 border-l-emerald-500"
            />
            <StatsCard
              title="退款订单"
              value={statsData.refundOrderCount.toString()}
              className="border-l-4 border-l-amber-500"
            />

            {/* 比率统计卡片 */}
            <StatsCard
              title="支付率"
              value={`${paymentRate}%`}
              description={`${statsData.paidOrderCount}/${statsData.orderCount} 订单已支付`}
              className="border-l-4 border-l-indigo-500"
            />
            <StatsCard
              title="退款率"
              value={`${refundRate}%`}
              description={`${statsData.refundOrderCount}/${statsData.paidOrderCount} 订单已退款`}
              className="border-l-4 border-l-purple-500"
            />
            <StatsCard
              title="平均订单金额"
              value={statsData.paidOrderCount > 0
                ? formatPrice(statsData.totalIncome / statsData.paidOrderCount)
                : formatPrice(0)}
              description="已支付订单的平均金额"
              className="border-l-4 border-l-teal-500"
            />
          </div>
        )}
      </CardContent>
    </Card>
  );
}

// 统计卡片组件
interface StatsCardProps {
  title: string;
  value: string;
  description?: string;
  className?: string;
}

function StatsCard({ title, value, description, className }: StatsCardProps) {
  return (
    <div className={`flex flex-col ${className}`}>
      <p className="text-xs font-medium text-muted-foreground">{title}</p>
      <div className="flex items-center gap-2 mt-1">
        <h3 className="text-2xl font-bold tracking-tight">{value}</h3>
      </div>
      {description && (
        <p className="text-xs text-muted-foreground mt-1">{description}</p>
      )}
    </div>
  );
}
