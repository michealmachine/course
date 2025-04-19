'use client'

import * as React from 'react'
import { format, subDays, parseISO } from 'date-fns'
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts'

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { LearningProgressTrendVO } from '@/types/learning-stats'

interface LearningProgressTrendProps {
  courseId: number
  fetchTrendData: (courseId: number, startDate?: string, endDate?: string) => Promise<LearningProgressTrendVO>
  title?: string
  description?: string
}

// 格式化图表数据
const formatChartData = (data: LearningProgressTrendVO | null) => {
  if (!data || !data.progressData || !Array.isArray(data.progressData)) return []

  return data.progressData.map(item => ({
    date: item.date,
    averageProgress: parseFloat((item.averageProgress || 0).toFixed(2)),
    activeUserCount: item.activeUserCount || 0
  }))
}

export function LearningProgressTrend({
  courseId,
  fetchTrendData,
  title = '学习进度趋势',
  description = '课程学习进度随时间的变化趋势'
}: LearningProgressTrendProps) {
  const [trendData, setTrendData] = React.useState<LearningProgressTrendVO | null>(null)
  const [loading, setLoading] = React.useState<boolean>(false)
  const [timeRange, setTimeRange] = React.useState<string>('30')
  const [chartType, setChartType] = React.useState<'line' | 'bar'>('line')

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

      const data = await fetchTrendData(courseId, formattedStartDate, formattedEndDate)
      setTrendData(data)
    } catch (error) {
      console.error('加载进度趋势数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => {
    loadData()
  }, [courseId, timeRange, fetchTrendData]) // eslint-disable-line react-hooks/exhaustive-deps

  // 格式化图表数据
  const chartData = React.useMemo(() => {
    if (!trendData) return []
    return formatChartData(trendData)
  }, [trendData])

  // 计算平均进度和活跃用户数的总和
  const totals = React.useMemo(() => {
    if (chartData.length === 0) return { avgProgress: 0, totalUsers: 0 }

    const avgProgress = chartData.reduce((sum, item) => sum + item.averageProgress, 0) / chartData.length
    const totalUsers = chartData.reduce((sum, item) => sum + item.activeUserCount, 0)

    return { avgProgress: avgProgress.toFixed(2), totalUsers }
  }, [chartData])

  return (
    <Card>
      <CardHeader className="flex flex-col items-stretch space-y-0 border-b p-0 sm:flex-row">
        <div className="flex flex-1 flex-col justify-center gap-1 px-6 py-5 sm:py-6">
          <CardTitle>{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </div>
        <div className="flex">
          <div className="relative z-30 flex flex-1 flex-col justify-center gap-1 border-t px-6 py-4 text-left sm:border-l sm:border-t-0 sm:px-8 sm:py-6">
            <span className="text-xs text-muted-foreground">平均进度</span>
            <span className="text-lg font-bold leading-none sm:text-3xl">
              {totals.avgProgress}%
            </span>
          </div>
          <div className="relative z-30 flex flex-1 flex-col justify-center gap-1 border-t border-l px-6 py-4 text-left sm:border-l sm:border-t-0 sm:px-8 sm:py-6">
            <span className="text-xs text-muted-foreground">活跃用户总数</span>
            <span className="text-lg font-bold leading-none sm:text-3xl">
              {totals.totalUsers}
            </span>
          </div>
        </div>
      </CardHeader>
      <div className="flex items-center justify-between px-6 py-3">
        <Tabs value={chartType} onValueChange={(value: string) => setChartType(value as 'line' | 'bar')}>
          <TabsList>
            <TabsTrigger value="line">折线图</TabsTrigger>
            <TabsTrigger value="bar">柱状图</TabsTrigger>
          </TabsList>
        </Tabs>
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
      </div>
      <CardContent className="px-2 sm:p-6">
        {loading ? (
          <div className="flex h-[350px] items-center justify-center">
            <p className="text-sm text-muted-foreground">加载中...</p>
          </div>
        ) : chartData.length > 0 ? (
          <div className="h-[350px]">
            <ResponsiveContainer width="100%" height="100%">
              {chartType === 'line' ? (
                <LineChart
                  data={chartData}
                  margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                  connectNulls={true} // 连接空值点
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                  <XAxis
                    dataKey="date"
                    tickLine={false}
                    axisLine={{ stroke: 'hsl(var(--border))' }}
                    tickMargin={8}
                    minTickGap={32}
                    tickFormatter={(value) => {
                      const date = new Date(value)
                      return date.toLocaleDateString('zh-CN', {
                        month: 'short',
                        day: 'numeric',
                      })
                    }}
                  />
                  <YAxis
                    yAxisId="left"
                    domain={[0, 100]}
                    tickLine={false}
                    axisLine={{ stroke: 'hsl(var(--border))' }}
                    tickFormatter={(value) => `${value}%`}
                  />
                  <YAxis
                    yAxisId="right"
                    orientation="right"
                    tickLine={false}
                    axisLine={{ stroke: 'hsl(var(--border))' }}
                  />
                  <Tooltip
                    formatter={(value, name) => {
                      if (name === 'averageProgress') return [`${value}%`, '平均进度'];
                      if (name === 'activeUserCount') return [value, '活跃用户数'];
                      return [value, name];
                    }}
                    labelFormatter={(label) => {
                      return new Date(label).toLocaleDateString('zh-CN', {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric',
                      });
                    }}
                  />
                  <Legend />
                  <Line
                    yAxisId="left"
                    type="monotone"
                    dataKey="averageProgress"
                    name="平均进度"
                    stroke="hsl(var(--primary))"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    activeDot={{ r: 8 }}
                    connectNulls={true} // 连接空值点
                  />
                  <Line
                    yAxisId="right"
                    type="monotone"
                    dataKey="activeUserCount"
                    name="活跃用户数"
                    stroke="hsl(var(--secondary))"
                    strokeWidth={2}
                    dot={{ r: 4 }}
                    connectNulls={true} // 连接空值点
                  />
                </LineChart>
              ) : (
                <BarChart
                  data={chartData}
                  margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                  <XAxis
                    dataKey="date"
                    tickLine={false}
                    axisLine={{ stroke: 'hsl(var(--border))' }}
                    tickMargin={8}
                    minTickGap={32}
                    tickFormatter={(value) => {
                      const date = new Date(value)
                      return date.toLocaleDateString('zh-CN', {
                        month: 'short',
                        day: 'numeric',
                      })
                    }}
                  />
                  <YAxis
                    yAxisId="left"
                    domain={[0, 100]}
                    tickLine={false}
                    axisLine={{ stroke: 'hsl(var(--border))' }}
                    tickFormatter={(value) => `${value}%`}
                  />
                  <YAxis
                    yAxisId="right"
                    orientation="right"
                    tickLine={false}
                    axisLine={{ stroke: 'hsl(var(--border))' }}
                  />
                  <Tooltip
                    formatter={(value, name) => {
                      if (name === 'averageProgress') return [`${value}%`, '平均进度'];
                      if (name === 'activeUserCount') return [value, '活跃用户数'];
                      return [value, name];
                    }}
                    labelFormatter={(label) => {
                      return new Date(label).toLocaleDateString('zh-CN', {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric',
                      });
                    }}
                  />
                  <Legend />
                  <Bar
                    yAxisId="left"
                    dataKey="averageProgress"
                    name="平均进度"
                    fill="hsl(var(--primary))"
                    radius={[4, 4, 0, 0]} // 添加圆角
                  />
                  <Bar
                    yAxisId="right"
                    dataKey="activeUserCount"
                    name="活跃用户数"
                    fill="hsl(var(--secondary))"
                    radius={[4, 4, 0, 0]} // 添加圆角
                  />
                </BarChart>
              )}
            </ResponsiveContainer>
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
