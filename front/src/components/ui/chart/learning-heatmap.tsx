'use client'

import * as React from 'react'
import { ResponsiveHeatMap, HeatMapSerie, DefaultHeatMapDatum } from '@nivo/heatmap'
import { format, subDays, parseISO } from 'date-fns'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { LearningHeatmapVO } from '@/types/learning-stats'
import { formatDuration } from '@/lib/utils/format'

interface LearningHeatmapProps {
  courseId: number
  fetchHeatmapData: (courseId: number, startDate?: string, endDate?: string) => Promise<LearningHeatmapVO>
  title?: string
  description?: string
}

// 将API数据转换为热力图格式
const transformHeatmapData = (data: LearningHeatmapVO | null) => {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  // 只使用常见的学习时间段，减少数据量提高性能
  const hours = Array.from({ length: 24 }, (_, i) => i.toString())

  // 创建空的热力图数据
  if (!data || !data.heatmapData) {
    return [
      {
        id: '无数据',
        data: hours.map(hour => ({
          x: hour,
          y: 0
        }))
      }
    ]
  }

  // 计算最大值以便于标准化
  let maxValue = 0;
  Object.values(data.heatmapData).forEach(dayData => {
    Object.values(dayData).forEach(count => {
      if (count > maxValue) maxValue = count;
    });
  });

  // 转换数据为热力图格式
  return weekdays.map((day, index) => {
    const dayIndex = index === 0 ? 7 : index // 调整周日的索引
    const heatmapData = data.heatmapData || {}
    const hourData = heatmapData[dayIndex] || {}

    return {
      id: day,
      data: hours.map(hour => {
        const value = hourData[parseInt(hour)] || 0;
        return {
          x: hour,
          y: value,
          formattedValue: formatDuration(value)
        };
      })
    }
  })
}

export function LearningHeatmap({
  courseId,
  fetchHeatmapData,
  title = '学习时长热力图',
  description = '按星期几和小时分布的学习时长热力图'
}: LearningHeatmapProps) {
  const [heatmapData, setHeatmapData] = React.useState<LearningHeatmapVO | null>(null)
  const [loading, setLoading] = React.useState<boolean>(false)
  const [timeRange, setTimeRange] = React.useState<string>('30')

  const loadData = async () => {
    try {
      setLoading(true)

      // 根据选择的时间范围计算日期
      const endDate = new Date()
      let startDate

      switch (timeRange) {
        case '7':
          startDate = subDays(endDate, 7)
          break
        case '14':
          startDate = subDays(endDate, 14)
          break
        case '30':
          startDate = subDays(endDate, 30)
          break
        case '90':
          startDate = subDays(endDate, 90)
          break
        default:
          startDate = subDays(endDate, 30)
      }

      const formattedStartDate = format(startDate, 'yyyy-MM-dd')
      const formattedEndDate = format(endDate, 'yyyy-MM-dd')

      const data = await fetchHeatmapData(courseId, formattedStartDate, formattedEndDate)
      setHeatmapData(data)
    } catch (error) {
      console.error('加载热力图数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => {
    loadData()
  }, [courseId, timeRange, fetchHeatmapData]) // eslint-disable-line react-hooks/exhaustive-deps

  // 转换数据为Nivo热力图格式
  const chartData = React.useMemo(() => {
    if (!heatmapData) return []
    return transformHeatmapData(heatmapData)
  }, [heatmapData])

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <div>
          <CardTitle>{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </div>
        <div className="flex items-center space-x-2">
          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="选择时间范围" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="7">最近7天</SelectItem>
              <SelectItem value="14">最近14天</SelectItem>
              <SelectItem value="30">最近30天</SelectItem>
              <SelectItem value="90">最近90天</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" onClick={loadData} disabled={loading}>
            {loading ? '加载中...' : '刷新'}
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="flex h-[350px] items-center justify-center">
            <p className="text-sm text-muted-foreground">加载中...</p>
          </div>
        ) : chartData.length > 0 ? (
          <div className="h-[350px]">
            <ResponsiveHeatMap
              data={chartData}
              margin={{ top: 20, right: 60, bottom: 60, left: 60 }}
              valueFormat={(value) => formatDuration(value)}
              axisTop={null}
              axisRight={null}
              axisBottom={{
                tickSize: 5,
                tickPadding: 5,
                tickRotation: 0,
                legend: '小时',
                legendPosition: 'middle',
                legendOffset: 36,
                tickValues: [0, 6, 12, 18, 23] // 减少刻度数量提高性能
              }}
              axisLeft={{
                tickSize: 5,
                tickPadding: 5,
                tickRotation: 0,
                legend: '星期',
                legendPosition: 'middle',
                legendOffset: -40
              }}
              colors={{
                type: 'sequential',
                scheme: 'blues' // 使用蓝色系更易区分
              }}
              emptyColor="#f5f5f5"
              borderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
              labelTextColor={{ from: 'color', modifiers: [['darker', 1.8]] }}
              legends={[
                {
                  anchor: 'bottom',
                  translateX: 0,
                  translateY: 30,
                  length: 400,
                  thickness: 8,
                  direction: 'row',
                  tickPosition: 'after',
                  tickSize: 3,
                  tickSpacing: 4,
                  tickOverlap: false,
                  tickFormat: (value) => `${value} 次`,
                  title: '活动次数',
                  titleAlign: 'start',
                  titleOffset: 4
                }
              ]}
              animate={false} // 关闭动画提高性能
              hoverTarget="cell"
            />
          </div>
        ) : (
          <div className="flex h-[350px] items-center justify-center">
            <p className="text-sm text-muted-foreground">暂无数据</p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
