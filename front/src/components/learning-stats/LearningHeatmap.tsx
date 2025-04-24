'use client';

import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { Button } from '@/components/ui/button';
import { LearningHeatmapVO } from '@/types/learning-stats';
import { format, subDays } from 'date-fns';
import { formatDuration, formatDurationShort } from '@/lib/utils/format';

interface LearningHeatmapProps {
  courseId: number;
  fetchHeatmapData: (courseId: number, startDate?: string, endDate?: string) => Promise<LearningHeatmapVO>;
  title?: string;
  description?: string;
}

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
const HOURS = Array.from({ length: 24 }, (_, i) => i);

const LearningHeatmap: React.FC<LearningHeatmapProps> = ({
  courseId,
  fetchHeatmapData,
  title = '学习时长热力图',
  description = '按星期几和小时分布的学习时长热力图'
}) => {
  const [heatmapData, setHeatmapData] = useState<LearningHeatmapVO | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<Date>(subDays(new Date(), 30));
  const [endDate, setEndDate] = useState<Date>(new Date());
  const [timeRange, setTimeRange] = useState<string>('30');

  const loadData = async () => {
    try {
      setLoading(true);
      const formattedStartDate = format(startDate, 'yyyy-MM-dd');
      const formattedEndDate = format(endDate, 'yyyy-MM-dd');

      const data = await fetchHeatmapData(courseId, formattedStartDate, formattedEndDate);
      setHeatmapData(data);
    } catch (error) {
      console.error('加载热力图数据失败:', error);
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

  // 计算热度颜色
  const getHeatColor = (duration: number, maxDuration: number) => {
    if (duration === 0) return 'bg-gray-100';

    const intensity = Math.min(Math.max(duration / maxDuration, 0), 1);

    if (intensity < 0.2) return 'bg-blue-100';
    if (intensity < 0.4) return 'bg-blue-200';
    if (intensity < 0.6) return 'bg-blue-300';
    if (intensity < 0.8) return 'bg-blue-400';
    return 'bg-blue-500';
  };

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
        ) : heatmapData ? (
          <div className="overflow-x-auto">
            <div className="min-w-max">
              <div className="grid grid-cols-[auto_repeat(24,minmax(24px,1fr))] gap-1">
                {/* 表头 - 小时 */}
                <div className="text-center font-medium"></div>
                {HOURS.map((hour) => (
                  <div key={hour} className="text-center text-xs font-medium">
                    {hour}
                  </div>
                ))}

                {/* 热力图数据 */}
                {WEEKDAYS.map((day, index) => {
                  const dayIndex = index === 0 ? 7 : index; // 调整周日的索引
                  return (
                    <React.Fragment key={dayIndex}>
                      <div className="text-right pr-2 font-medium text-sm">{day}</div>
                      {HOURS.map((hour) => {
                        const duration = heatmapData.heatmapData[dayIndex]?.[hour] || 0;
                        return (
                          <div
                            key={`${dayIndex}-${hour}`}
                            className={`h-6 ${getHeatColor(duration, heatmapData.maxActivityCount)} rounded-sm flex items-center justify-center`}
                            title={`${day} ${hour}:00 - ${formatDuration(duration)}`}
                          >
                            <span className="text-xs text-gray-700">{duration > 0 ? formatDurationShort(duration) : ''}</span>
                          </div>
                        );
                      })}
                    </React.Fragment>
                  );
                })}
              </div>
            </div>

            {/* 图例 */}
            <div className="mt-4 flex items-center gap-2">
              <span className="text-sm font-medium">学习时长:</span>
              <div className="flex items-center gap-1">
                <div className="w-4 h-4 bg-gray-100 rounded-sm"></div>
                <span className="text-xs">0分钟</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-4 h-4 bg-blue-100 rounded-sm"></div>
                <span className="text-xs">低</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-4 h-4 bg-blue-300 rounded-sm"></div>
                <span className="text-xs">中</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-4 h-4 bg-blue-500 rounded-sm"></div>
                <span className="text-xs">高</span>
              </div>
            </div>
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

export default LearningHeatmap;
