"use client"

import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { CartesianGrid, LabelList, Line, LineChart, PolarAngleAxis, PolarGrid, Radar, RadarChart, XAxis, YAxis } from 'recharts'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
  ChartConfig
} from '@/components/ui/chart'
import { Skeleton } from '@/components/ui/skeleton'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import {
  Clock,
  Calendar,
  BarChart2,
  PieChart as PieChartIcon,
  AlertCircle,
  BookOpen,
  CheckCircle,
  TrendingUp
} from 'lucide-react'
import { learningService } from '@/services'
import { formatDuration } from '@/lib/utils'
import { DateLearningHeatmapVO, LearningHeatmapVO } from '@/types/learning-stats'
import { SimpleHeatmap } from '@/components/ui/chart/simple-heatmap'
import { addDays, format, subDays, eachDayOfInterval, isSameMonth } from 'date-fns'
import { Calendar as CalendarIcon } from 'lucide-react'
import { SimpleCalendarHeatmap } from '@/components/ui/simple-calendar-heatmap'

interface CourseStatisticsProps {
  courseId?: number;
}

export function CourseStatistics({ courseId }: CourseStatisticsProps) {
  // 全局学习统计数据
  const [statistics, setStatistics] = useState<any>(null);
  // 课程特定的统计数据
  const [courseStats, setCourseStats] = useState<any>(null);
  // 活动类型统计
  const [activityStats, setActivityStats] = useState<any[]>([]);
  // 学习热图数据
  const [heatmapData, setHeatmapData] = useState<any[]>([]);
  // 学习热力图数据
  const [learningHeatmap, setLearningHeatmap] = useState<LearningHeatmapVO | null>(null);
  // 按日期分组的学习热力图数据
  const [dateHeatmap, setDateHeatmap] = useState<DateLearningHeatmapVO | null>(null);
  // 加载状态
  const [loading, setLoading] = useState(true);
  // 错误信息
  const [error, setError] = useState<string | null>(null);
  // 当前选择的统计类型
  const [activeTab, setActiveTab] = useState('overview');
  // 热力图日期范围
  const startDate = subDays(new Date(), 30);
  const endDate = new Date();

  // 图表配置
  const chartConfig = {
    daily: {
      label: '每日学习时长',
      color: 'hsl(var(--chart-1))'
    },
    video: {
      label: '视频观看',
      color: 'hsl(var(--chart-2))'
    },
    document: {
      label: '文档阅读',
      color: 'hsl(var(--chart-3))'
    },
    quiz: {
      label: '测验尝试',
      color: 'hsl(var(--chart-4))'
    },
    section: {
      label: '小节完成',
      color: 'hsl(var(--chart-5))'
    }
  };

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        setError(null);

        // 获取统计数据
        if (courseId) {
          // 获取特定课程的统计数据
          const stats = await learningService.getCourseLearningStatistics(courseId);
          setCourseStats(stats);
        } else {
          // 获取总体学习统计数据
          const stats = await learningService.getLearningStatistics();
          setStatistics(stats);
        }

        // 获取活动类型统计
        const activityData = await learningService.getActivityTypeStats();
        setActivityStats(activityData);

        // 获取学习热图数据
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - 30); // 过去30天
        const formattedStartDate = startDate.toISOString().split('T')[0];

        const heatmap = await learningService.getLearningHeatmap(formattedStartDate);
        setHeatmapData(heatmap);

        // 获取学习热力图数据
        await loadHeatmapData();

      } catch (err) {
        console.error('获取学习统计数据失败:', err);
        setError('获取学习统计数据失败，请稍后重试');
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, [courseId]);

  // 修改格式化每日学习数据的部分，确保连续性
  const dailyLearningData = (() => {
    // 创建最近14天的日期数组，确保即使没有数据的日期也包含在内
    const last14Days = Array.from({ length: 14 }, (_, i) => {
      const date = new Date();
      date.setDate(date.getDate() - 13 + i);
      return date.toISOString().split('T')[0].substring(5); // 只返回MM-DD格式
    });

    // 创建一个日期到数据的映射
    const dateMap = new Map();

    // 将现有数据放入映射中
    heatmapData.forEach(day => {
      const dateStr = day.date.substring(5); // MM-DD格式
      dateMap.set(dateStr, {
        date: dateStr,
        hours: Math.round((day.durationSeconds / 3600) * 10) / 10, // 转换为小时并保留一位小数
        minutes: Math.round(day.durationSeconds / 60), // 转换为分钟
        count: day.activityCount
      });
    });

    // 确保所有日期都有数据，如果没有则填充零值
    return last14Days.map(date =>
      dateMap.get(date) || {
        date,
        hours: 0,
        minutes: 0,
        count: 0
      }
    );
  })();

  // 格式化活动类型数据为图表所需格式
  const activityTypeData = activityStats.map(stat => {
    // 创建一个映射，确保活动类型描述的一致性
    const activityTypeMap = {
      'VIDEO_WATCH': '视频观看',
      'DOCUMENT_READ': '文档阅读',
      'QUIZ_ATTEMPT': '测验尝试',
      'SECTION_START': '小节开始',
      'SECTION_END': '小节完成'
    };

    // 使用映射中的描述，如果没有则使用原始描述
    const description = activityTypeMap[stat.activityType] || stat.activityTypeDescription;

    return {
      name: description,
      value: Math.round(stat.totalDurationSeconds / 60), // 转换为分钟
      count: stat.activityCount,
      activityType: stat.activityType
    };
  });

  function getActivityColor(activityType: string): string {
    switch (activityType) {
      case 'VIDEO_WATCH': return 'var(--color-video)';
      case 'DOCUMENT_READ': return 'var(--color-document)';
      case 'QUIZ_ATTEMPT': return 'var(--color-quiz)';
      case 'SECTION_START':
      case 'SECTION_END': return 'var(--color-section)';
      default: return 'var(--color-daily)';
    }
  }

  // 加载热力图数据
  const loadHeatmapData = async () => {
    try {
      const formattedStartDate = format(startDate, 'yyyy-MM-dd');
      const formattedEndDate = format(endDate, 'yyyy-MM-dd');

      // 使用按日期分组的API
      const heatmapByDate = await learningService.getLearningHeatmapByDate(formattedStartDate, formattedEndDate);
      setDateHeatmap(heatmapByDate);
    } catch (err) {
      console.error('获取学习热力图数据失败:', err);
    }
  };

  // 将热力图数据转换为日历热力图格式
  const getCalendarHeatmapData = () => {
    if (!dateHeatmap) return {};

    // 直接返回按日期分组的热力图数据
    return dateHeatmap.heatmapData;
  };

  // 获取日历热力图数据的最大值
  const getCalendarHeatmapMaxValue = () => {
    if (!dateHeatmap) return 0;
    return dateHeatmap.maxActivityCount || 0;
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {[...Array(3)].map((_, i) => (
            <Card key={i}>
              <CardContent className="pt-6">
                <Skeleton className="h-8 w-full mb-2" />
                <Skeleton className="h-6 w-2/3" />
              </CardContent>
            </Card>
          ))}
        </div>
        <Card>
          <CardContent className="pt-6">
            <Skeleton className="h-64 w-full" />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>获取数据失败</AlertTitle>
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  // 获取要展示的统计数据(全局统计或课程特定统计)
  const statsData = courseId ? courseStats : statistics;

  // 计算总体完成率
  const completionRate = statsData?.totalCourses
    ? Math.round((statsData.completedCourses / statsData.totalCourses) * 100)
    : 0;

  return (
    <div className="space-y-6">
      {/* 顶部统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">学习时长</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatDuration(courseId
                ? statsData?.learningDuration || 0
                : statsData?.totalLearningDuration || 0)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {courseId
                ? `最近学习: ${statsData?.lastLearnTime ? new Date(statsData.lastLearnTime).toLocaleDateString() : '未学习'}`
                : `今日学习: ${formatDuration(statsData?.todayLearningDuration || 0)}`
              }
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {courseId ? "学习进度" : "学习天数"}
            </CardTitle>
            {courseId
              ? <BarChart2 className="h-4 w-4 text-muted-foreground" />
              : <Calendar className="h-4 w-4 text-muted-foreground" />
            }
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {courseId
                ? `${statsData?.progress || 0}%`
                : statsData?.learningDays || 0
              }
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {courseId
                ? (statsData?.progress >= 100 ? "已完成" : "进行中")
                : `连续学习: ${statsData?.currentConsecutiveDays || 0} 天`
              }
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              {courseId ? "学习活动" : "课程完成"}
            </CardTitle>
            {courseId
              ? <CheckCircle className="h-4 w-4 text-muted-foreground" />
              : <BookOpen className="h-4 w-4 text-muted-foreground" />
            }
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {courseId
                ? activityStats.reduce((sum, stat) => sum + stat.activityCount, 0)
                : `${statsData?.completedCourses || 0}/${statsData?.totalCourses || 0}`
              }
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {courseId
                ? "总学习活动次数"
                : `完成率: ${completionRate}%`
              }
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 图表区域 */}
      <Tabs defaultValue="daily" className="space-y-4">
        <TabsList>
          <TabsTrigger value="daily">每日学习趋势</TabsTrigger>
          <TabsTrigger value="activity">活动类型分布</TabsTrigger>
          <TabsTrigger value="heatmap">学习时间模式</TabsTrigger>
          {!courseId && <TabsTrigger value="courses">课程学习情况</TabsTrigger>}
        </TabsList>

        {/* 每日学习趋势 */}
        <TabsContent value="daily" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>每日学习时长</CardTitle>
              <CardDescription>最近14天的学习时长统计</CardDescription>
            </CardHeader>
            <CardContent>
              {dailyLearningData.length > 0 ? (
                <ChartContainer config={chartConfig} className="min-h-[300px] w-full">
                  <LineChart
                    accessibilityLayer
                    data={dailyLearningData}
                    margin={{
                      top: 20,
                      left: 20,
                      right: 20,
                      bottom: 20,
                    }}
                  >
                    <CartesianGrid vertical={false} />
                    <XAxis
                      dataKey="date"
                      tickLine={false}
                      axisLine={false}
                      tickMargin={8}
                      tickFormatter={(value) => value}
                    />
                    <YAxis
                      tickLine={false}
                      axisLine={false}
                      tickMargin={8}
                      tickFormatter={(value) => `${value}分钟`}
                    />
                    <ChartTooltip
                      cursor={false}
                      content={<ChartTooltipContent indicator="line" />}
                    />
                    <Line
                      dataKey="minutes"
                      type="monotone"
                      stroke="var(--color-daily)"
                      strokeWidth={2}
                      connectNulls={true}
                      dot={{
                        fill: "var(--color-daily)",
                      }}
                      activeDot={{
                        r: 6,
                      }}
                    >
                      <LabelList
                        position="top"
                        offset={12}
                        className="fill-foreground"
                        fontSize={12}
                      />
                    </Line>
                  </LineChart>
                </ChartContainer>
              ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                  <Calendar className="h-12 w-12 mb-4 opacity-20" />
                  <p>暂无学习记录数据</p>
                  <p className="text-sm mt-1">开始学习后这里将显示您的学习时长统计</p>
                </div>
              )}
            </CardContent>
            {dailyLearningData.length > 0 && (
              <CardFooter className="flex-col items-start gap-2 text-sm">
                <div className="flex gap-2 font-medium leading-none">
                  {dailyLearningData[dailyLearningData.length - 1].minutes >
                   dailyLearningData[dailyLearningData.length - 2].minutes ?
                    "学习时间趋势上升" : "保持学习习惯"}
                  <TrendingUp className="h-4 w-4" />
                </div>
                <div className="leading-none text-muted-foreground">
                  显示最近14天的学习时长统计
                </div>
              </CardFooter>
            )}
          </Card>
        </TabsContent>

        {/* 活动类型分布 */}
        <TabsContent value="activity" className="space-y-4">
          <Card>
            <CardHeader className="items-center">
              <CardTitle>学习活动分布</CardTitle>
              <CardDescription>不同类型学习活动的时间分布</CardDescription>
            </CardHeader>
            <CardContent>
              {activityTypeData.length > 0 ? (
                <ChartContainer
                  config={chartConfig}
                  className="mx-auto aspect-square max-h-[350px]"
                >
                  <RadarChart data={activityTypeData}>
                    <ChartTooltip cursor={false} content={<ChartTooltipContent />} />
                    <PolarAngleAxis dataKey="name" />
                    <PolarGrid />
                    <Radar
                      name="学习时长"
                      dataKey="value"
                      stroke="var(--color-daily)"
                      fill="var(--color-daily)"
                      fillOpacity={0.6}
                    />
                  </RadarChart>
                </ChartContainer>
              ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                  <PieChartIcon className="h-12 w-12 mb-4 opacity-20" />
                  <p>暂无活动类型数据</p>
                  <p className="text-sm mt-1">开始学习后这里将显示您的学习活动分布</p>
                </div>
              )}
            </CardContent>
            {activityTypeData.length > 0 && (
              <CardFooter className="flex-col gap-2 text-sm">
                <div className="flex items-center gap-2 font-medium leading-none">
                  {activityTypeData.sort((a, b) => b.value - a.value)[0]?.name || ""} 活动最多
                  <TrendingUp className="h-4 w-4" />
                </div>
                <div className="flex items-center gap-2 leading-none text-muted-foreground">
                  共 {activityTypeData.reduce((sum, entry) => sum + entry.count, 0)} 次学习活动
                </div>
              </CardFooter>
            )}
          </Card>
        </TabsContent>

        {/* 学习热力图 */}
        <TabsContent value="heatmap" className="space-y-4">
          <Card>
            <CardHeader>
              <div>
                <CardTitle>学习时间模式</CardTitle>
                <CardDescription>展示您在一周内不同时间段的学习活动分布</CardDescription>
              </div>
            </CardHeader>
            <CardContent>
              {dateHeatmap ? (
                <div className="space-y-6">
                  {/* 热力图日历 */}
                  <div className="flex justify-center">
                    <Card className="border shadow-sm">
                      <CardContent className="p-0">
                        <SimpleCalendarHeatmap
                          initialMonth={startDate}
                          heatmapData={getCalendarHeatmapData()}
                          maxValue={getCalendarHeatmapMaxValue()}
                          locale="zh-CN"
                          className="mx-auto"
                        />
                      </CardContent>
                    </Card>
                  </div>

                  {/* 图例 */}
                  <div className="flex flex-col items-center gap-4">
                    <div className="flex items-center justify-center gap-2">
                      <span className="text-sm font-medium">活动频率:</span>
                      <div className="flex items-center gap-1">
                        <div className="w-4 h-4 bg-muted rounded-sm"></div>
                        <span className="text-xs">无</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <div className="w-4 h-4 bg-zinc-200 rounded-sm"></div>
                        <span className="text-xs">低</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <div className="w-4 h-4 bg-zinc-500 rounded-sm"></div>
                        <span className="text-xs">中</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <div className="w-4 h-4 bg-zinc-900 rounded-sm"></div>
                        <span className="text-xs">高</span>
                      </div>
                    </div>
                    <div className="text-xs text-muted-foreground text-center max-w-md">
                      注意：此图表展示的是您在一周内不同时间段的学习活动频率，而非特定日期的活动。相同星期的日期将显示相同的活动模式。
                    </div>
                  </div>
                </div>
              ) : (
                <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                  <CalendarIcon className="h-12 w-12 mb-4 opacity-20" />
                  <p>暂无学习热力图数据</p>
                  <p className="text-sm mt-1">开始学习后这里将显示您的学习热力图</p>
                </div>
              )}
            </CardContent>
            {dateHeatmap && (
              <CardFooter className="flex-col items-start gap-2 text-sm">
                <div className="flex gap-2 font-medium leading-none">
                  学习时间模式展示了您在一周内的学习习惯
                </div>
                <div className="leading-none text-muted-foreground">
                  通过分析您的学习时间模式，可以帮助您安排更高效的学习计划
                </div>
              </CardFooter>
            )}
          </Card>
        </TabsContent>

        {/* 课程学习情况 (仅在全局统计中显示) */}
        {!courseId && (
          <TabsContent value="courses" className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>课程学习进度</CardTitle>
                <CardDescription>各课程的学习进度和时长</CardDescription>
              </CardHeader>
              <CardContent>
                {statsData?.courseStatistics && statsData.courseStatistics.length > 0 ? (
                  <div className="space-y-6">
                    {statsData.courseStatistics.map((course: any, index: number) => (
                      <div key={index} className="space-y-2">
                        <div className="flex justify-between items-center">
                          <div className="flex items-center">
                            <div className="w-12 h-12 rounded overflow-hidden mr-3 bg-muted flex-shrink-0">
                              {course.courseCover ? (
                                <img
                                  src={course.courseCover}
                                  alt={course.courseTitle}
                                  className="w-full h-full object-cover"
                                />
                              ) : (
                                <div className="w-full h-full flex items-center justify-center text-muted-foreground">
                                  <BookOpen className="h-6 w-6" />
                                </div>
                              )}
                            </div>
                            <div>
                              <h4 className="font-medium text-sm">{course.courseTitle}</h4>
                              <p className="text-xs text-muted-foreground">
                                学习时长: {formatDuration(course.learningDuration)}
                              </p>
                            </div>
                          </div>
                          <Badge variant={course.progress >= 100 ? "success" : "outline"}>
                            {course.progress}%
                          </Badge>
                        </div>
                        <div className="h-2 bg-muted rounded-full overflow-hidden">
                          <div
                            className="h-full bg-primary"
                            style={{ width: `${course.progress}%` }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                    <BookOpen className="h-12 w-12 mb-4 opacity-20" />
                    <p>暂无课程学习数据</p>
                    <p className="text-sm mt-1">您尚未开始学习任何课程</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        )}
      </Tabs>
    </div>
  );
}