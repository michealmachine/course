'use client';

import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { Button } from '@/components/ui/button';
import { LearningProgressTrendVO } from '@/types/learning-stats';
import { format, subDays, parseISO } from 'date-fns';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  BarChart,
  Bar
} from 'recharts';

interface LearningProgressTrendProps {
  courseId: number;
  fetchTrendData: (courseId: number, startDate?: string, endDate?: string) => Promise<LearningProgressTrendVO>;
  title?: string;
  description?: string;
}

const LearningProgressTrend: React.FC<LearningProgressTrendProps> = ({
  courseId,
  fetchTrendData,
  title = '学习进度趋势',
  description = '课程学习进度随时间的变化趋势'
}) => {
  const [trendData, setTrendData] = useState<LearningProgressTrendVO | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<Date>(subDays(new Date(), 30));
  const [endDate, setEndDate] = useState<Date>(new Date());
  const [timeRange, setTimeRange] = useState<string>('30');
  const [chartType, setChartType] = useState<'line' | 'bar'>('line');

  const loadData = async () => {
    try {
      setLoading(true);
      const formattedStartDate = format(startDate, 'yyyy-MM-dd');
      const formattedEndDate = format(endDate, 'yyyy-MM-dd');
      
      const data = await fetchTrendData(courseId, formattedStartDate, formattedEndDate);
      setTrendData(data);
    } catch (error) {
      console.error('加载进度趋势数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [courseId]);

  const handleTimeRangeChange = (value: string) => {
    setTimeRange(value);
    const end = new Date();
    let start;
    
    switch (value) {
      case '7':
        start = subDays(end, 7);
        break;
      case '14':
        start = subDays(end, 14);
        break;
      case '30':
        start = subDays(end, 30);
        break;
      case '90':
        start = subDays(end, 90);
        break;
      default:
        start = subDays(end, 30);
    }
    
    setStartDate(start);
    setEndDate(end);
  };

  const handleApplyFilter = () => {
    loadData();
  };

  // 格式化图表数据
  const formatChartData = () => {
    if (!trendData || !trendData.progressData) return [];
    
    return trendData.progressData.map(item => ({
      date: item.date,
      averageProgress: parseFloat(item.averageProgress.toFixed(2)),
      activeUserCount: item.activeUserCount
    }));
  };

  const chartData = formatChartData();

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
        <div className="flex flex-wrap gap-4 mt-4">
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium">时间范围:</span>
            <Select value={timeRange} onValueChange={handleTimeRangeChange}>
              <SelectTrigger className="w-32">
                <SelectValue placeholder="选择时间范围" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="7">最近7天</SelectItem>
                <SelectItem value="14">最近14天</SelectItem>
                <SelectItem value="30">最近30天</SelectItem>
                <SelectItem value="90">最近90天</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium">开始日期:</span>
            <DatePicker date={startDate} setDate={setStartDate} />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium">结束日期:</span>
            <DatePicker date={endDate} setDate={setEndDate} />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-sm font-medium">图表类型:</span>
            <Select value={chartType} onValueChange={(value: 'line' | 'bar') => setChartType(value)}>
              <SelectTrigger className="w-32">
                <SelectValue placeholder="选择图表类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="line">折线图</SelectItem>
                <SelectItem value="bar">柱状图</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <Button onClick={handleApplyFilter} disabled={loading}>
            {loading ? '加载中...' : '应用筛选'}
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <p>加载中...</p>
          </div>
        ) : trendData && chartData.length > 0 ? (
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              {chartType === 'line' ? (
                <LineChart
                  data={chartData}
                  margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis yAxisId="left" domain={[0, 100]} />
                  <YAxis yAxisId="right" orientation="right" />
                  <Tooltip />
                  <Legend />
                  <Line
                    yAxisId="left"
                    type="monotone"
                    dataKey="averageProgress"
                    name="平均进度 (%)"
                    stroke="#8884d8"
                    activeDot={{ r: 8 }}
                  />
                  <Line
                    yAxisId="right"
                    type="monotone"
                    dataKey="activeUserCount"
                    name="活跃用户数"
                    stroke="#82ca9d"
                  />
                </LineChart>
              ) : (
                <BarChart
                  data={chartData}
                  margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis yAxisId="left" domain={[0, 100]} />
                  <YAxis yAxisId="right" orientation="right" />
                  <Tooltip />
                  <Legend />
                  <Bar
                    yAxisId="left"
                    dataKey="averageProgress"
                    name="平均进度 (%)"
                    fill="#8884d8"
                  />
                  <Bar
                    yAxisId="right"
                    dataKey="activeUserCount"
                    name="活跃用户数"
                    fill="#82ca9d"
                  />
                </BarChart>
              )}
            </ResponsiveContainer>
          </div>
        ) : (
          <div className="flex justify-center items-center h-64">
            <p>暂无数据</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default LearningProgressTrend;
