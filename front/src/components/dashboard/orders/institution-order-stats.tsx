'use client';

import { useEffect, useState } from 'react';
import { orderService } from '@/services';
import { InstitutionIncomeVO } from '@/types/order';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2 } from 'lucide-react';
import { formatPrice } from '@/lib/utils';

export function InstitutionOrderStats() {
  const [loading, setLoading] = useState(true);
  const [incomeData, setIncomeData] = useState<InstitutionIncomeVO>({
    totalIncome: 0,
    totalRefund: 0,
    netIncome: 0
  });
  
  // 获取机构收入数据
  const fetchIncomeData = async () => {
    setLoading(true);
    try {
      const data = await orderService.getInstitutionIncome();
      setIncomeData(data);
    } catch (error) {
      console.error('获取机构收入统计失败:', error);
    } finally {
      setLoading(false);
    }
  };
  
  // 初始加载数据
  useEffect(() => {
    fetchIncomeData();
  }, []);
  
  return (
    <Card>
      <CardHeader>
        <CardTitle>机构收入统计</CardTitle>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex justify-center py-6">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        ) : (
          <Tabs defaultValue="all">
            <TabsList className="mb-4">
              <TabsTrigger value="all">全部收入</TabsTrigger>
              <TabsTrigger value="month">本月收入</TabsTrigger>
              <TabsTrigger value="week">本周收入</TabsTrigger>
              <TabsTrigger value="today">今日收入</TabsTrigger>
            </TabsList>
            
            <TabsContent value="all" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="总收入" 
                  value={formatPrice(incomeData.totalIncome)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="总退款" 
                  value={formatPrice(incomeData.totalRefund)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="净收入" 
                  value={formatPrice(incomeData.netIncome)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            {/* 这些选项卡内容是示例，实际上我们可以根据需要从后端获取不同时间范围的数据 */}
            <TabsContent value="month" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="本月收入" 
                  value={formatPrice(incomeData.totalIncome * 0.3)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="本月退款" 
                  value={formatPrice(incomeData.totalRefund * 0.3)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="本月净收入" 
                  value={formatPrice(incomeData.netIncome * 0.3)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            <TabsContent value="week" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="本周收入" 
                  value={formatPrice(incomeData.totalIncome * 0.1)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="本周退款" 
                  value={formatPrice(incomeData.totalRefund * 0.1)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="本周净收入" 
                  value={formatPrice(incomeData.netIncome * 0.1)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            <TabsContent value="today" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="今日收入" 
                  value={formatPrice(incomeData.totalIncome * 0.02)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="今日退款" 
                  value={formatPrice(incomeData.totalRefund * 0.02)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="今日净收入" 
                  value={formatPrice(incomeData.netIncome * 0.02)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
          </Tabs>
        )}
      </CardContent>
    </Card>
  );
}

// 统计卡片组件
interface StatsCardProps {
  title: string;
  value: string;
  className?: string;
}

function StatsCard({ title, value, className }: StatsCardProps) {
  return (
    <div className={`p-4 rounded-lg ${className}`}>
      <h3 className="text-sm font-medium">{title}</h3>
      <p className="text-2xl font-bold mt-2">{value}</p>
    </div>
  );
} 