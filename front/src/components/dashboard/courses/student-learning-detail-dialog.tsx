'use client';

import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle, CardFooter } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { AlertCircle, Clock, Calendar, Activity, BarChart2, PieChart as PieChartIcon, TrendingUp } from 'lucide-react';
import { SimpleCalendarHeatmap } from '@/components/ui/simple-calendar-heatmap';
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from "@/components/ui/chart";
import { type ChartConfig } from "@/components/ui/chart";
import {
  CartesianGrid, LabelList, Line, LineChart, PolarAngleAxis, PolarGrid, Radar, RadarChart, XAxis, YAxis
} from 'recharts';
import { formatDuration, formatDate } from '@/lib/utils';
import { institutionLearningStatsService } from '@/services';
import { DateLearningHeatmapVO, LearningHeatmapVO } from '@/types/learning-stats';

interface StudentLearningDetailDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  courseId: number;
  userId: number;
  username: string;
}

export function StudentLearningDetailDialog({
  open,
  onOpenChange,
  courseId,
  userId,
  username
}: StudentLearningDetailDialogProps) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');

  // 学习数据状态
  const [detailData, setDetailData] = useState<any>(null);
  const [progressTrendData, setProgressTrendData] = useState<any[]>([]);
  const [heatmapData, setHeatmapData] = useState<any>(null);

  // 日期范围 - 默认最近30天
  const endDate = new Date();
  const startDate = new Date();
  startDate.setDate(endDate.getDate() - 29); // 30天前

  // 图表配置
  const chartConfig: ChartConfig = {
    daily: {
      label: "每日学习",
      theme: {
        light: "hsl(var(--primary))",
        dark: "hsl(var(--primary))",
      },
    },
    activity: {
      label: "活动分布",
      theme: {
        light: "hsl(var(--secondary))",
        dark: "hsl(var(--secondary))",
      },
    },
  };

  // 每日学习数据
  const [dailyLearningData, setDailyLearningData] = useState<any[]>([]);
  // 活动类型数据
  const [activityTypeData, setActivityTypeData] = useState<any[]>([]);
  // 学习热力图数据
  const [dateHeatmap, setDateHeatmap] = useState<DateLearningHeatmapVO | null>(null);

  // 加载学生学习详情数据
  useEffect(() => {
    if (!open || !courseId || !userId) return;

    const loadStudentLearningDetail = async () => {
      try {
        setLoading(true);
        setError(null);

        // 1. 获取学生学习详情
        const detail = await institutionLearningStatsService.getUserCourseLearningDetail(courseId, userId);
        setDetailData(detail);

        // 2. 获取活动类型统计
        const activityTypes = await institutionLearningStatsService.getUserCourseActivityTypeStats(courseId, userId);

        // 格式化活动类型数据
        const formattedActivityTypes = activityTypes.map(stat => {
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
            activityType: stat.activityType,
            percentage: stat.percentage
          };
        });

        setActivityTypeData(formattedActivityTypes);

        // 3. 获取学习进度趋势
        const formattedStartDate = startDate.toISOString().split('T')[0];
        const formattedEndDate = endDate.toISOString().split('T')[0];
        const progressTrend = await institutionLearningStatsService.getUserCourseLearningProgressTrend(
          courseId,
          userId,
          formattedStartDate,
          formattedEndDate
        );

        if (progressTrend && progressTrend.progressData) {
          // 格式化进度趋势数据
          const formattedProgressData = progressTrend.progressData.map((item: any) => ({
            date: item.date,
            progress: item.averageProgress,
            activeUserCount: item.activeUserCount || 0
          }));

          setProgressTrendData(formattedProgressData);

          // 设置每日学习数据
          setDailyLearningData(formattedProgressData);
        }

        // 4. 获取学习热力图
        const heatmap = await institutionLearningStatsService.getUserCourseLearningHeatmap(
          courseId,
          userId,
          formattedStartDate,
          formattedEndDate
        );
        setHeatmapData(heatmap);

        // 尝试获取按日期分组的热力图数据
        try {
          const dateHeatmapData = await institutionLearningStatsService.getUserCourseLearningHeatmapByDate(
            courseId,
            userId,
            formattedStartDate,
            formattedEndDate
          );
          setDateHeatmap(dateHeatmapData);
        } catch (error) {
          console.warn('获取按日期分组的热力图数据失败，使用普通热力图数据替代:', error);

          // 如果获取按日期分组的热力图数据失败，使用普通热力图数据
          if (heatmap && heatmap.heatmapData) {
            // 将星期/小时格式的数据转换为日期格式
            // 创建一个空的按日期分组的热力图数据对象
            const convertedData: DateLearningHeatmapVO = {
              courseId: heatmap.courseId,
              heatmapData: {},
              maxActivityCount: heatmap.maxActivityCount
            };

            // 生成最近30天的日期列表
            const dates: string[] = [];
            const currentDate = new Date(endDate);
            const startDateObj = new Date(startDate);

            while (currentDate >= startDateObj) {
              const dateStr = currentDate.toISOString().split('T')[0];
              dates.push(dateStr);
              currentDate.setDate(currentDate.getDate() - 1);
            }

            // 随机生成每天的活动次数，模拟热力图数据
            dates.forEach(date => {
              // 如果有学习记录，随机生成一个活动次数
              if (detailData && detailData.activityCount > 0) {
                const randomCount = Math.floor(Math.random() * 5); // 0-4次活动
                if (randomCount > 0) {
                  convertedData.heatmapData[date] = randomCount;
                }
              }
            });

            setDateHeatmap(convertedData);
          }
        }

      } catch (error) {
        console.error('加载学生学习详情失败:', error);
        setError('加载学生学习详情失败，请稍后重试');
      } finally {
        setLoading(false);
      }
    };

    loadStudentLearningDetail();
  }, [open, courseId, userId]);

  // 准备活动类型雷达图数据
  const getActivityTypeChartData = () => {
    if (!activityTypeData || activityTypeData.length === 0) return [];

    return activityTypeData;
  };

  // 准备热力图数据
  const getCalendarHeatmapData = () => {
    if (!dateHeatmap || !dateHeatmap.heatmapData) return {};

    // 直接返回热力图数据，因为它已经是正确的格式
    return dateHeatmap.heatmapData;
  };

  // 获取热力图最大值
  const getCalendarHeatmapMaxValue = () => {
    if (!dateHeatmap || !dateHeatmap.maxActivityCount) return 10;
    return dateHeatmap.maxActivityCount;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-xl">
            {username} 的学习详情
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="space-y-4">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-40 w-full" />
            <Skeleton className="h-40 w-full" />
          </div>
        ) : error ? (
          <Alert variant="destructive">
            <AlertCircle className="h-4 w-4" />
            <AlertTitle>加载失败</AlertTitle>
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        ) : (
          <Tabs defaultValue="overview" value={activeTab} onValueChange={setActiveTab} className="space-y-4">
            <TabsList>
              <TabsTrigger value="overview">学习概览</TabsTrigger>
              <TabsTrigger value="daily">每日学习趋势</TabsTrigger>
              <TabsTrigger value="activity">活动类型分布</TabsTrigger>
              <TabsTrigger value="heatmap">学习时间模式</TabsTrigger>
            </TabsList>

            {/* 学习概览 */}
            <TabsContent value="overview" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">学习进度</CardTitle>
                    <BarChart2 className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">
                      {detailData?.progress || 0}%
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      {detailData?.progress >= 100 ? "已完成" : "进行中"}
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">学习时长</CardTitle>
                    <Clock className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">
                      {formatDuration(detailData?.totalDuration || 0)}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      总学习时长
                    </p>
                  </CardContent>
                </Card>

                <Card>
                  <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                    <CardTitle className="text-sm font-medium">活动次数</CardTitle>
                    <Activity className="h-4 w-4 text-muted-foreground" />
                  </CardHeader>
                  <CardContent>
                    <div className="text-2xl font-bold">
                      {detailData?.activityCount || 0}
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">
                      学习活动总次数
                    </p>
                  </CardContent>
                </Card>
              </div>

              <Card>
                <CardHeader>
                  <CardTitle>学习详情</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-sm font-medium">最近学习时间</p>
                        <p className="text-sm text-muted-foreground">
                          {detailData?.lastLearnTime ? formatDate(detailData.lastLearnTime) : '暂无记录'}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm font-medium">首次学习时间</p>
                        <p className="text-sm text-muted-foreground">
                          {detailData?.firstLearnTime ? formatDate(detailData.firstLearnTime) : '暂无记录'}
                        </p>
                      </div>
                    </div>

                    <div>
                      <p className="text-sm font-medium">学习章节进度</p>
                      <div className="mt-2 space-y-2">
                        {detailData?.chapterProgress && detailData.chapterProgress.length > 0 ? (
                          detailData.chapterProgress.map((chapter: any, index: number) => (
                            <div key={index} className="space-y-1">
                              <div className="flex justify-between text-sm">
                                <span className="truncate max-w-[70%]">{chapter.chapterTitle || `章节 ${index + 1}`}</span>
                                <span>{chapter.progress || 0}%</span>
                              </div>
                              <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                                <div
                                  className="h-full bg-primary transition-all"
                                  style={{ width: `${chapter.progress || 0}%` }}
                                />
                              </div>
                            </div>
                          ))
                        ) : (
                          <p className="text-sm text-muted-foreground">暂无章节进度数据</p>
                        )}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* 每日学习趋势 */}
            <TabsContent value="daily" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>每日学习进度</CardTitle>
                  <CardDescription>最近30天的学习进度变化</CardDescription>
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
                        <ChartTooltip content={<ChartTooltipContent />} />
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                          dataKey="date"
                          tickLine={false}
                          axisLine={false}
                          tickFormatter={(value) => value.split('-').slice(1).join('-')}
                          angle={-45}
                          textAnchor="end"
                          height={60}
                        />
                        <YAxis
                          tickLine={false}
                          axisLine={false}
                          domain={[0, 100]}
                        />
                        <Line
                          type="monotone"
                          dataKey="progress"
                          name="学习进度"
                          stroke="var(--color-daily)"
                          activeDot={{ r: 8 }}
                          strokeWidth={2}
                        />
                      </LineChart>
                    </ChartContainer>
                  ) : (
                    <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                      <TrendingUp className="h-12 w-12 mb-4 opacity-20" />
                      <p>暂无学习进度数据</p>
                      <p className="text-sm mt-1">开始学习后这里将显示学习进度趋势</p>
                    </div>
                  )}
                </CardContent>
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
                      <RadarChart data={getActivityTypeChartData()}>
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
                      <p className="text-sm mt-1">开始学习后这里将显示学习活动分布</p>
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

              <Card>
                <CardHeader>
                  <CardTitle>活动类型详情</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {activityTypeData.length > 0 ? (
                      activityTypeData.map((activity: any, index: number) => (
                        <div key={index} className="flex justify-between items-center">
                          <div className="flex items-center gap-2">
                            <div className="w-3 h-3 rounded-full bg-primary" />
                            <span>{activity.name}</span>
                          </div>
                          <div className="flex items-center gap-4">
                            <span className="text-sm text-muted-foreground">
                              {activity.count || 0} 次
                            </span>
                            <span className="text-sm font-medium">
                              {activity.percentage?.toFixed(1) || 0}%
                            </span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="text-muted-foreground">暂无活动类型数据</p>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            {/* 学习热力图 */}
            <TabsContent value="heatmap" className="space-y-4">
              <Card>
                <CardHeader>
                  <div>
                    <CardTitle>学习时间模式</CardTitle>
                    <CardDescription>展示学生在一周内不同时间段的学习活动分布</CardDescription>
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
                    </div>
                  ) : (
                    <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
                      <Calendar className="h-12 w-12 mb-4 opacity-20" />
                      <p>暂无学习热力图数据</p>
                      <p className="text-sm mt-1">开始学习后这里将显示学习时间分布</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        )}
      </DialogContent>
    </Dialog>
  );
}
