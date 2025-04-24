'use client';

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { format, subDays } from 'date-fns';
import { LearningHeatmapVO } from '@/types/learning-stats';
import { formatDuration, formatDurationShort } from '@/lib/utils/format';

interface SimpleHeatmapProps {
  courseId: number;
  fetchHeatmapData: (courseId: number, startDate?: string, endDate?: string) => Promise<LearningHeatmapVO>;
  title?: string;
  description?: string;
}

const WEEKDAYS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
const HOURS = Array.from({ length: 24 }, (_, i) => i);

export function SimpleHeatmap({
  courseId,
  fetchHeatmapData,
  title = '学习时长热力图',
  description = '按星期几和小时分布的学习时长热力图'
}: SimpleHeatmapProps) {
  const [data, setData] = useState<Record<number, Record<number, number>>>({});
  const [maxValue, setMaxValue] = useState(0);
  const [loading, setLoading] = useState(false);
  const [timeRange, setTimeRange] = useState('30');
  const [error, setError] = useState<string | null>(null);

  // 加载数据
  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);

      // 计算日期范围
      const endDate = new Date();
      let startDate;

      switch (timeRange) {
        case '7':
          startDate = subDays(endDate, 7);
          break;
        case '14':
          startDate = subDays(endDate, 14);
          break;
        case '30':
          startDate = subDays(endDate, 30);
          break;
        case '90':
          startDate = subDays(endDate, 90);
          break;
        default:
          startDate = subDays(endDate, 30);
      }

      const formattedStartDate = format(startDate, 'yyyy-MM-dd');
      const formattedEndDate = format(endDate, 'yyyy-MM-dd');

      // 获取热力图数据
      const response = await fetchHeatmapData(courseId, formattedStartDate, formattedEndDate);

      if (response && response.heatmapData) {
        setData(response.heatmapData);
        setMaxValue(response.maxActivityCount || 0);
      } else {
        setData({});
        setMaxValue(0);
      }
    } catch (err) {
      console.error('加载热力图数据失败:', err);
      setError('加载热力图数据失败');
      setData({});
      setMaxValue(0);
    } finally {
      setLoading(false);
    }
  };

  // 初始加载和时间范围变化时重新加载数据
  useEffect(() => {
    loadData();
  }, [courseId, timeRange]); // eslint-disable-line react-hooks/exhaustive-deps

  // 获取热度颜色
  const getHeatColor = (count: number) => {
    if (count === 0) return 'bg-gray-100';
    if (maxValue === 0) return 'bg-gray-100';

    const intensity = Math.min(Math.max(count / maxValue, 0), 1);

    if (intensity < 0.2) return 'bg-blue-100';
    if (intensity < 0.4) return 'bg-blue-200';
    if (intensity < 0.6) return 'bg-blue-300';
    if (intensity < 0.8) return 'bg-blue-400';
    return 'bg-blue-500';
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-row items-center justify-between">
          <div>
            <CardTitle>{title}</CardTitle>
            <CardDescription>{description}</CardDescription>
          </div>
          <div className="flex items-center space-x-2">
            <Select value={timeRange} onValueChange={setTimeRange}>
              <SelectTrigger className="w-[140px]">
                <SelectValue placeholder="选择时间范围" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="7">最近7天</SelectItem>
                <SelectItem value="14">最近14天</SelectItem>
                <SelectItem value="30">最近30天</SelectItem>
                <SelectItem value="90">最近90天</SelectItem>
              </SelectContent>
            </Select>
            <Button variant="outline" size="sm" onClick={loadData} disabled={loading}>
              {loading ? '加载中...' : '刷新'}
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <p className="text-muted-foreground">加载中...</p>
          </div>
        ) : error ? (
          <div className="flex justify-center items-center h-64">
            <p className="text-muted-foreground">{error}</p>
          </div>
        ) : (
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
                        const duration = data[dayIndex]?.[hour] || 0;
                        return (
                          <div
                            key={`${dayIndex}-${hour}`}
                            className={`h-6 ${getHeatColor(duration)} rounded-sm flex items-center justify-center`}
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
              <span className="text-sm font-medium">活动频率:</span>
              <div className="flex items-center gap-1">
                <div className="w-4 h-4 bg-gray-100 rounded-sm"></div>
                <span className="text-xs">无</span>
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
        )}
      </CardContent>
    </Card>
  );
}
