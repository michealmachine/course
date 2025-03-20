'use client';

import { useEffect, useState } from 'react';
import { orderService } from '@/services';
import { 
  InstitutionIncomeVO, 
  InstitutionDailyIncomeVO,
  InstitutionWeeklyIncomeVO,
  InstitutionMonthlyIncomeVO,
  InstitutionCustomIncomeVO
} from '@/types/order';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2, Calendar as CalendarIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { formatPrice } from '@/lib/utils';
import { DatePicker } from '@/components/ui/date-picker';

export function InstitutionOrderStats() {
  const [loading, setLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<string>('all');
  
  // 各类型收入数据
  const [totalIncomeData, setTotalIncomeData] = useState<InstitutionIncomeVO>({
    totalIncome: 0,
    totalRefund: 0,
    netIncome: 0
  });
  
  const [dailyIncomeData, setDailyIncomeData] = useState<InstitutionDailyIncomeVO>({
    dailyIncome: 0,
    dailyRefund: 0,
    dailyNetIncome: 0
  });
  
  const [weeklyIncomeData, setWeeklyIncomeData] = useState<InstitutionWeeklyIncomeVO>({
    weeklyIncome: 0,
    weeklyRefund: 0,
    weeklyNetIncome: 0
  });
  
  const [monthlyIncomeData, setMonthlyIncomeData] = useState<InstitutionMonthlyIncomeVO>({
    monthlyIncome: 0,
    monthlyRefund: 0,
    monthlyNetIncome: 0
  });
  
  const [customIncomeData, setCustomIncomeData] = useState<InstitutionCustomIncomeVO>({
    customIncome: 0,
    customRefund: 0,
    customNetIncome: 0,
    startTime: 0,
    endTime: 0
  });
  
  // 自定义时间范围
  const [startDate, setStartDate] = useState<Date | undefined>(new Date());
  const [endDate, setEndDate] = useState<Date | undefined>(new Date());
  const [isCustomLoading, setIsCustomLoading] = useState<boolean>(false);
  
  // 获取机构总收入数据
  const fetchTotalIncomeData = async () => {
    setLoading(true);
    try {
      const data = await orderService.getInstitutionIncome();
      setTotalIncomeData(data);
    } catch (error) {
      console.error('获取机构总收入统计失败:', error);
    } finally {
      setLoading(false);
    }
  };
  
  // 获取机构日收入数据
  const fetchDailyIncomeData = async () => {
    setLoading(true);
    try {
      const data = await orderService.getInstitutionDailyIncome();
      setDailyIncomeData(data);
    } catch (error) {
      console.error('获取机构日收入统计失败:', error);
    } finally {
      setLoading(false);
    }
  };
  
  // 获取机构周收入数据
  const fetchWeeklyIncomeData = async () => {
    setLoading(true);
    try {
      const data = await orderService.getInstitutionWeeklyIncome();
      setWeeklyIncomeData(data);
    } catch (error) {
      console.error('获取机构周收入统计失败:', error);
    } finally {
      setLoading(false);
    }
  };
  
  // 获取机构月收入数据
  const fetchMonthlyIncomeData = async () => {
    setLoading(true);
    try {
      const data = await orderService.getInstitutionMonthlyIncome();
      setMonthlyIncomeData(data);
    } catch (error) {
      console.error('获取机构月收入统计失败:', error);
    } finally {
      setLoading(false);
    }
  };
  
  // 获取自定义时间范围收入数据
  const fetchCustomIncomeData = async () => {
    if (!startDate || !endDate) {
      return;
    }
    
    setIsCustomLoading(true);
    try {
      // 格式化日期为ISO字符串
      const startIso = startDate.toISOString();
      const endIso = endDate.toISOString();
      
      const data = await orderService.getInstitutionCustomIncome(startIso, endIso);
      setCustomIncomeData(data);
    } catch (error) {
      console.error('获取自定义时间范围收入统计失败:', error);
    } finally {
      setIsCustomLoading(false);
    }
  };
  
  // 处理Tab切换
  const handleTabChange = (value: string) => {
    setActiveTab(value);
    
    switch(value) {
      case 'all':
        fetchTotalIncomeData();
        break;
      case 'today':
        fetchDailyIncomeData();
        break;
      case 'week':
        fetchWeeklyIncomeData();
        break;
      case 'month':
        fetchMonthlyIncomeData();
        break;
      case 'custom':
        // 切换到自定义时不立即加载，等用户设置日期并点击查询
        break;
    }
  };
  
  // 初始加载数据
  useEffect(() => {
    fetchTotalIncomeData();
  }, []);
  
  return (
    <Card>
      <CardHeader>
        <CardTitle>机构收入统计</CardTitle>
      </CardHeader>
      <CardContent>
        {loading && activeTab !== 'custom' ? (
          <div className="flex justify-center py-6">
            <Loader2 className="h-6 w-6 animate-spin" />
          </div>
        ) : (
          <Tabs value={activeTab} onValueChange={handleTabChange}>
            <TabsList className="mb-4">
              <TabsTrigger value="all">全部收入</TabsTrigger>
              <TabsTrigger value="month">本月收入</TabsTrigger>
              <TabsTrigger value="week">本周收入</TabsTrigger>
              <TabsTrigger value="today">今日收入</TabsTrigger>
              <TabsTrigger value="custom">自定义</TabsTrigger>
            </TabsList>
            
            <TabsContent value="all" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="总收入" 
                  value={formatPrice(totalIncomeData.totalIncome)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="总退款" 
                  value={formatPrice(totalIncomeData.totalRefund)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="净收入" 
                  value={formatPrice(totalIncomeData.netIncome)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            <TabsContent value="month" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="本月收入" 
                  value={formatPrice(monthlyIncomeData.monthlyIncome)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="本月退款" 
                  value={formatPrice(monthlyIncomeData.monthlyRefund)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="本月净收入" 
                  value={formatPrice(monthlyIncomeData.monthlyNetIncome)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            <TabsContent value="week" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="本周收入" 
                  value={formatPrice(weeklyIncomeData.weeklyIncome)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="本周退款" 
                  value={formatPrice(weeklyIncomeData.weeklyRefund)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="本周净收入" 
                  value={formatPrice(weeklyIncomeData.weeklyNetIncome)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            <TabsContent value="today" className="space-y-0">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <StatsCard 
                  title="今日收入" 
                  value={formatPrice(dailyIncomeData.dailyIncome)}
                  className="bg-green-50"
                />
                <StatsCard 
                  title="今日退款" 
                  value={formatPrice(dailyIncomeData.dailyRefund)}
                  className="bg-red-50"
                />
                <StatsCard 
                  title="今日净收入" 
                  value={formatPrice(dailyIncomeData.dailyNetIncome)}
                  className="bg-blue-50"
                />
              </div>
            </TabsContent>
            
            <TabsContent value="custom" className="space-y-4">
              <div className="flex flex-col md:flex-row gap-4 mb-4">
                <div className="flex-1">
                  <label className="text-sm font-medium mb-1 block">开始日期</label>
                  <DatePicker
                    date={startDate}
                    setDate={setStartDate}
                    className="w-full"
                  />
                </div>
                <div className="flex-1">
                  <label className="text-sm font-medium mb-1 block">结束日期</label>
                  <DatePicker
                    date={endDate}
                    setDate={setEndDate}
                    className="w-full"
                  />
                </div>
                <div className="flex items-end">
                  <Button 
                    onClick={fetchCustomIncomeData} 
                    disabled={!startDate || !endDate || isCustomLoading}
                    className="w-full md:w-auto"
                  >
                    {isCustomLoading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : <CalendarIcon className="h-4 w-4 mr-2" />}
                    查询
                  </Button>
                </div>
              </div>
              
              {isCustomLoading ? (
                <div className="flex justify-center py-6">
                  <Loader2 className="h-6 w-6 animate-spin" />
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <StatsCard 
                    title="区间收入" 
                    value={formatPrice(customIncomeData.customIncome)}
                    className="bg-green-50"
                  />
                  <StatsCard 
                    title="区间退款" 
                    value={formatPrice(customIncomeData.customRefund)}
                    className="bg-red-50"
                  />
                  <StatsCard 
                    title="区间净收入" 
                    value={formatPrice(customIncomeData.customNetIncome)}
                    className="bg-blue-50"
                  />
                </div>
              )}
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