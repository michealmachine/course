'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  AlertCircle, FileEdit, Plus, Search, Trash2, Eye, ExternalLink,
  BookOpen, Star, Heart, Users, ChevronDown, ChevronUp, MessageSquare,
  BarChart2, Clock, Calendar, Activity, PieChart as PieChartIcon, TrendingUp
} from 'lucide-react';
import useCourseStore from '@/stores/course-store';
import { courseService } from '@/services';
import { CourseStatus, CoursePaymentType, Course } from '@/types/course';
import { formatDate } from '../../../utils/date';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious
} from '@/components/ui/pagination';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger
} from '@/components/ui/dialog';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Separator } from '@/components/ui/separator';
import CourseStatusBadge from '@/components/dashboard/courses/CourseStatusBadge';
import CoursePublishBadge from '@/components/dashboard/courses/CoursePublishBadge';
import { CourseLearningStats } from '@/components/dashboard/courses/course-learning-stats';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useToast } from '@/components/ui/use-toast';
import { cn } from '@/lib/utils';
import institutionLearningStatsService from '@/services/institution-learning-stats-service';
import courseReviewService from '@/services/course-review-service';

// 导入图表组件
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  ChartLegend,
  ChartLegendContent,
} from '@/components/ui/chart';
import { type ChartConfig } from '@/components/ui/chart';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Legend,
  RadarChart,
  Radar,
  PolarGrid,
  PolarAngleAxis
} from 'recharts';

export default function CoursesPage() {
  const router = useRouter();
  const { toast } = useToast();
  const {
    courses,
    totalCourses,
    currentPage,
    pageSize,
    filterTitle,
    filterStatus,
    isLoading,
    error,
    setCourses,
    setPage,
    setFilter,
    setLoading,
    setError
  } = useCourseStore();

  const [deleteDialog, setDeleteDialog] = useState<{open: boolean, courseId: number | null}>({
    open: false,
    courseId: null
  });

  // 当前视图类型: workspace-工作区课程，published-已发布课程
  const [viewType, setViewType] = useState<'workspace' | 'published'>('workspace');
  const [isLoadingPublishedVersion, setIsLoadingPublishedVersion] = useState(false);

  // 可展开项管理
  const [expandedItems, setExpandedItems] = useState<Record<number, boolean>>({});

  // 课程统计数据
  const [courseStats, setCourseStats] = useState<Record<number, any>>({});
  const [dailyStats, setDailyStats] = useState<Record<number, any[]>>({});
  const [activityStats, setActivityStats] = useState<Record<number, any[]>>({});
  const [courseReviews, setCourseReviews] = useState<Record<number, any>>({});

  // 加载状态
  const [statsLoading, setStatsLoading] = useState<Record<number, boolean>>({});
  const [dailyLoading, setDailyLoading] = useState<Record<number, boolean>>({});
  const [activityLoading, setActivityLoading] = useState<Record<number, boolean>>({});
  const [reviewsLoading, setReviewsLoading] = useState<Record<number, boolean>>({});

  // 课程状态选项
  const statusOptions = [
    { value: null, label: '全部状态' },
    { value: CourseStatus.DRAFT, label: '草稿' },
    { value: CourseStatus.REVIEWING, label: '审核中' },
    { value: CourseStatus.PUBLISHED, label: '已发布' },
    { value: CourseStatus.REJECTED, label: '已拒绝' }
  ];

  // 初始加载
  useEffect(() => {
    if (viewType === 'workspace') {
      loadCourses();
    } else {
      loadPublishedCourses();
    }
  }, [currentPage, pageSize, filterTitle, filterStatus, viewType]);

  // 加载工作区课程列表
  const loadCourses = async () => {
    try {
      setLoading(true);
      setError(null);

      const result = await courseService.getCourseList(
        currentPage - 1,
        pageSize,
        filterTitle,
        filterStatus as CourseStatus
      );

      // 检查返回结果格式并安全处理
      if (result && typeof result === 'object') {
        if (Array.isArray(result)) {
          // 如果直接返回数组
          setCourses(result, result.length);
        } else if (result.content && Array.isArray(result.content)) {
          // 如果返回分页对象
          setCourses(result.content, result.totalElements || result.content.length);
        } else {
          // 格式不匹配
          setCourses([], 0);
          setError('返回数据格式不正确');
          console.error('API返回格式不匹配:', result);
        }
      } else {
        setCourses([], 0);
        setError('未能获取课程数据');
        console.error('API返回格式不匹配或为空:', result);
      }
    } catch (error: any) {
      console.error('加载课程列表失败:', error);
      setError(error.message || '加载课程列表失败');
      setCourses([], 0);
    } finally {
      setLoading(false);
    }
  };

  // 加载已发布版本课程列表
  const loadPublishedCourses = async () => {
    try {
      setLoading(true);
      setError(null);

      const result = await courseService.getPublishedCoursesByInstitution({
        page: currentPage - 1,
        size: pageSize,
        keyword: filterTitle,
        status: filterStatus as CourseStatus
      });

      // 检查返回结果格式并安全处理
      if (result && typeof result === 'object') {
        if (Array.isArray(result)) {
          // 如果直接返回数组
          setCourses(result, result.length);
        } else if (result.content && Array.isArray(result.content)) {
          // 如果返回分页对象
          setCourses(result.content, result.totalElements || result.content.length);
        } else {
          // 格式不匹配
          setCourses([], 0);
          setError('返回数据格式不正确');
          console.error('API返回格式不匹配:', result);
        }
      } else {
        setCourses([], 0);
        setError('未能获取课程数据');
        console.error('API返回格式不匹配或为空:', result);
      }
    } catch (error: any) {
      console.error('加载已发布课程列表失败:', error);
      setError(error.message || '加载已发布课程列表失败');
      setCourses([], 0);
    } finally {
      setLoading(false);
    }
  };

  // 创建新课程
  const handleCreateCourse = () => {
    router.push('/dashboard/courses/create');
  };

  // 编辑课程
  const handleEditCourse = (id: number) => {
    router.push(`/dashboard/courses/${id}`);
  };

  // 查看课程
  const handleViewCourse = (id: number) => {
    router.push(`/dashboard/courses/${id}`);
  };

  // 查看发布版本
  const handleViewPublishedVersion = async (courseId: number) => {
    try {
      setIsLoadingPublishedVersion(true);
      const publishedVersion = await courseService.getPublishedVersion(courseId);
      router.push(`/dashboard/courses/${publishedVersion.id}`);
    } catch (error: any) {
      console.error('获取发布版本失败:', error);
      toast({
        title: '获取发布版本失败',
        description: error.message || '无法获取发布版本',
      } as any);
    } finally {
      setIsLoadingPublishedVersion(false);
    }
  };

  // 切换课程展开/收起状态
  const toggleExpand = (courseId: number) => {
    setExpandedItems(prev => {
      const newState = { ...prev };
      newState[courseId] = !prev[courseId];

      // 如果展开，加载相关数据
      if (newState[courseId]) {
        loadCourseStatistics(courseId);
        loadCourseDailyStats(courseId);
        loadCourseActivityStats(courseId);
        loadCourseReviews(courseId);
      }

      return newState;
    });
  };

  // 加载课程统计概览
  const loadCourseStatistics = async (courseId: number) => {
    if (courseStats[courseId] || statsLoading[courseId]) return;

    try {
      setStatsLoading(prev => ({ ...prev, [courseId]: true }));
      const stats = await institutionLearningStatsService.getCourseStatisticsOverview(courseId);
      setCourseStats(prev => ({ ...prev, [courseId]: stats }));
    } catch (error) {
      console.error(`加载课程统计数据失败, 课程ID: ${courseId}:`, error);
      toast({
        title: '加载统计数据失败',
        description: '无法获取课程统计数据',
        variant: 'destructive'
      } as any);
    } finally {
      setStatsLoading(prev => ({ ...prev, [courseId]: false }));
    }
  };

  // 加载课程每日学习统计
  const loadCourseDailyStats = async (courseId: number) => {
    if (dailyStats[courseId]?.length > 0 || dailyLoading[courseId]) return;

    try {
      setDailyLoading(prev => ({ ...prev, [courseId]: true }));

      // 获取最近14天的数据
      const endDate = new Date().toISOString().split('T')[0];
      const startDate = new Date(Date.now() - 13 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

      const stats = await institutionLearningStatsService.getCourseDailyStatistics(courseId, startDate, endDate);
      setDailyStats(prev => ({ ...prev, [courseId]: stats }));
    } catch (error) {
      console.error(`加载课程每日统计数据失败, 课程ID: ${courseId}:`, error);
    } finally {
      setDailyLoading(prev => ({ ...prev, [courseId]: false }));
    }
  };

  // 加载课程活动类型统计
  const loadCourseActivityStats = async (courseId: number) => {
    if (activityStats[courseId]?.length > 0 || activityLoading[courseId]) return;

    try {
      setActivityLoading(prev => ({ ...prev, [courseId]: true }));
      const stats = await institutionLearningStatsService.getCourseActivityTypeStatistics(courseId);
      setActivityStats(prev => ({ ...prev, [courseId]: stats }));
    } catch (error) {
      console.error(`加载课程活动类型统计失败, 课程ID: ${courseId}:`, error);
    } finally {
      setActivityLoading(prev => ({ ...prev, [courseId]: false }));
    }
  };

  // 加载课程评论
  const loadCourseReviews = async (courseId: number) => {
    if (courseReviews[courseId] || reviewsLoading[courseId]) return;

    try {
      setReviewsLoading(prev => ({ ...prev, [courseId]: true }));
      const reviewData = await courseReviewService.getCourseReviewSection(courseId, 0, 3);
      setCourseReviews(prev => ({ ...prev, [courseId]: reviewData }));
    } catch (error) {
      console.error(`加载课程评论失败, 课程ID: ${courseId}:`, error);
    } finally {
      setReviewsLoading(prev => ({ ...prev, [courseId]: false }));
    }
  };

  // 删除课程
  const handleDeleteClick = (courseId: number) => {
    setDeleteDialog({ open: true, courseId });
  };

  const confirmDelete = async () => {
    if (!deleteDialog.courseId) return;

    try {
      setLoading(true);
      await courseService.deleteCourse(deleteDialog.courseId);
      setDeleteDialog({ open: false, courseId: null });
      loadCourses(); // 重新加载列表
    } catch (error: any) {
      setError(error.message || '删除课程失败');
    } finally {
      setLoading(false);
    }
  };

  // 渲染付费类型
  const renderPaymentType = (type: CoursePaymentType, price?: number) => {
    if (type === CoursePaymentType.FREE) {
      return <span className="text-green-600">免费</span>;
    } else {
      return <span className="text-amber-600">¥ {price || 0}</span>;
    }
  };

  // 计算总页数
  const totalPages = Math.max(1, Math.ceil(totalCourses / pageSize) || 1);

  // 获取分页项
  const getPaginationItems = () => {
    const items = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 1; i <= totalPages; i++) {
        items.push(i);
      }
    } else {
      // 总是显示第一页
      items.push(1);

      // 计算中间页码的范围
      let start = Math.max(2, currentPage - 1);
      let end = Math.min(totalPages - 1, currentPage + 1);

      // 调整以确保我们显示5个页码
      if (end - start + 1 < 3) {
        if (start === 2) {
          end = Math.min(totalPages - 1, end + 1);
        } else if (end === totalPages - 1) {
          start = Math.max(2, start - 1);
        }
      }

      // 添加省略号
      if (start > 2) {
        items.push('...');
      }

      // 添加中间页码
      for (let i = start; i <= end; i++) {
        items.push(i);
      }

      // 添加省略号
      if (end < totalPages - 1) {
        items.push('...');
      }

      // 总是显示最后一页
      items.push(totalPages);
    }

    return items;
  };

  // 格式化学习时长
  const formatLearningDuration = (seconds: number) => {
    if (!seconds) return '0分钟';

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}小时${minutes > 0 ? ` ${minutes}分钟` : ''}`;
    } else {
      return `${minutes}分钟`;
    }
  };

  // 图表配置
  const dailyChartConfig: ChartConfig = {
    duration: {
      label: '学习时长',
      theme: {
        light: '#3b82f6',
        dark: '#60a5fa'
      }
    },
    count: {
      label: '学习人数',
      theme: {
        light: '#10b981',
        dark: '#34d399'
      }
    }
  };

  // 活动类型图表配置 - 使用更高对比度的颜色
  const activityChartConfig: ChartConfig = {
    VIDEO_WATCH: {
      label: '视频观看',
      theme: {
        light: '#2563eb', // 更深的蓝色
        dark: '#3b82f6'
      }
    },
    DOCUMENT_READ: {
      label: '文档阅读',
      theme: {
        light: '#059669', // 更深的绿色
        dark: '#10b981'
      }
    },
    QUIZ_ATTEMPT: {
      label: '测验尝试',
      theme: {
        light: '#d97706', // 更深的琥珀色
        dark: '#f59e0b'
      }
    },
    SECTION_START: {
      label: '小节开始',
      theme: {
        light: '#4f46e5', // 更深的靛蓝色
        dark: '#6366f1'
      }
    },
    SECTION_END: {
      label: '小节完成',
      theme: {
        light: '#db2777', // 更深的粉色
        dark: '#ec4899'
      }
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold tracking-tight">课程管理</h1>
        <Button onClick={handleCreateCourse}>
          <Plus className="mr-2 h-4 w-4" />
          创建课程
        </Button>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>错误</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* 版本切换 */}
      <Tabs value={viewType} onValueChange={(value) => setViewType(value as 'workspace' | 'published')}>
        <TabsList className="mb-4">
          <TabsTrigger value="workspace">工作区课程</TabsTrigger>
          <TabsTrigger value="published">已发布课程</TabsTrigger>
        </TabsList>

        <TabsContent value="workspace" className="space-y-4">
          {/* 搜索和筛选 */}
          <div className="flex gap-4 items-center">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索课程..."
                value={filterTitle}
                onChange={(e) => setFilter({ title: e.target.value })}
                className="pl-8"
              />
            </div>

            <Select
              value={filterStatus?.toString() || 'null'}
              onValueChange={(value) => setFilter({
                status: value === 'null' ? null : parseInt(value) as CourseStatus
              })}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="选择状态" />
              </SelectTrigger>
              <SelectContent>
                {statusOptions.map((option) => (
                  <SelectItem
                    key={option.value === null ? 'null' : option.value}
                    value={option.value === null ? 'null' : option.value.toString()}
                  >
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* 工作区课程列表 */}
          <div className="space-y-4">
            {isLoading ? (
              // 加载状态
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                <span className="ml-2">加载中...</span>
              </div>
            ) : courses.length === 0 ? (
              // 空状态
              <Card className="p-6">
                <div className="flex flex-col items-center justify-center py-12 space-y-3">
                  <BookOpen className="h-12 w-12 text-muted-foreground/30" />
                  <div className="text-xl font-medium">暂无课程数据</div>
                  <div className="text-muted-foreground mb-4">您还没有创建任何课程</div>
                  <Button onClick={handleCreateCourse}>
                    <Plus className="mr-2 h-4 w-4" />
                    创建第一个课程
                  </Button>
                </div>
              </Card>
            ) : (
              // 课程列表
              courses.map((course) => (
                <Card
                  key={course.id}
                  className="overflow-hidden hover:shadow-sm transition-all duration-200"
                >
                  <div className="flex items-stretch border-b overflow-hidden">
                    {/* 课程图片 */}
                    <div className="w-48 h-32 bg-muted relative flex-shrink-0">
                      {course.coverUrl ? (
                        <img
                          src={course.coverUrl}
                          alt={course.title}
                          className="object-cover w-full h-full"
                        />
                      ) : (
                        <div className="flex items-center justify-center w-full h-full">
                          <BookOpen className="w-12 h-12 text-muted-foreground/30" />
                        </div>
                      )}

                      {/* 课程状态标签 */}
                      <div className="absolute bottom-2 left-2">
                        <CourseStatusBadge status={course.status} />
                      </div>
                    </div>

                    {/* 课程信息 */}
                    <div className="flex-1 p-4 flex flex-col justify-between">
                      <div>
                        <h3 className="font-semibold text-lg">{course.title}</h3>
                        <p className="text-sm text-muted-foreground line-clamp-1 mt-1">{course.description || "暂无描述"}</p>
                      </div>

                      <div className="flex items-center gap-4 text-sm">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1 text-muted-foreground" />
                          <span>{course.createdAt ? formatDate(course.createdAt) : '未知时间'}</span>
                        </div>
                        <div>
                          {renderPaymentType(course.paymentType, course.price)}
                        </div>
                        <div>
                          <CoursePublishBadge course={course} />
                        </div>
                      </div>
                    </div>

                    {/* 操作按钮 */}
                    <div className="flex items-center p-4 gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleEditCourse(course.id)}
                        title={course.status === CourseStatus.REVIEWING ? "只能预览，不能编辑" : "编辑课程"}
                      >
                        {course.status === CourseStatus.REVIEWING
                          ? <Eye className="h-4 w-4 mr-1" />
                          : <FileEdit className="h-4 w-4 mr-1" />}
                        {course.status === CourseStatus.REVIEWING ? '预览' : '编辑'}
                      </Button>

                      {course.publishedVersionId && (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleViewPublishedVersion(course.id)}
                          disabled={isLoadingPublishedVersion}
                          title="查看发布版本"
                        >
                          <ExternalLink className="h-4 w-4 mr-1" />
                          发布版
                        </Button>
                      )}

                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleDeleteClick(course.id)}
                        disabled={course.status !== CourseStatus.DRAFT}
                      >
                        <Trash2 className="h-4 w-4 mr-1" />
                        删除
                      </Button>
                    </div>
                  </div>
                </Card>
              ))
            )}

            {/* 分页 */}
            {totalPages > 1 && (
              <div className="mt-4">
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => setPage(Math.max(1, currentPage - 1))}
                        className={currentPage === 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>

                    {getPaginationItems().map((item, index) => (
                      <PaginationItem key={index}>
                        {item === '...' ? (
                          <span className="px-4 py-2">...</span>
                        ) : (
                          <PaginationLink
                            onClick={() => typeof item === 'number' && setPage(item)}
                            isActive={currentPage === item}
                          >
                            {item}
                          </PaginationLink>
                        )}
                      </PaginationItem>
                    ))}

                    <PaginationItem>
                      <PaginationNext
                        onClick={() => setPage(Math.min(totalPages, currentPage + 1))}
                        className={currentPage === totalPages ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}
          </div>
        </TabsContent>

        <TabsContent value="published" className="space-y-4">
          {/* 搜索和筛选 */}
          <div className="flex gap-4 items-center">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索已发布课程..."
                value={filterTitle}
                onChange={(e) => setFilter({ title: e.target.value })}
                className="pl-8"
              />
            </div>
          </div>

          {/* 已发布课程列表 */}
          <div className="space-y-4">
            {isLoading ? (
              // 加载状态
              <div className="flex items-center justify-center py-12">
                <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                <span className="ml-2">加载中...</span>
              </div>
            ) : courses.length === 0 ? (
              // 空状态
              <Card className="p-6">
                <div className="flex flex-col items-center justify-center py-12 space-y-3">
                  <BookOpen className="h-12 w-12 text-muted-foreground/30" />
                  <div className="text-xl font-medium">暂无已发布课程</div>
                  <div className="text-muted-foreground">您还没有发布任何课程</div>
                </div>
              </Card>
            ) : (
              // 课程列表
              courses.map((course) => {
                const isExpanded = expandedItems[course.id] || false;
                const stats = courseStats[course.id];
                const dailyData = dailyStats[course.id] || [];
                const activityData = activityStats[course.id] || [];
                const reviewData = courseReviews[course.id];

                return (
                  <Card
                    key={course.id}
                    className={cn(
                      "overflow-hidden transition-all duration-200",
                      isExpanded ? "shadow-md" : "hover:shadow-sm"
                    )}
                  >
                    {/* 课程主要信息行 - 点击展开/收起 */}
                    <div
                      className="flex items-stretch border-b overflow-hidden cursor-pointer"
                      onClick={() => toggleExpand(course.id)}
                    >
                      {/* 课程图片 */}
                      <div className="w-48 h-32 bg-muted relative flex-shrink-0">
                        {course.coverUrl ? (
                          <img
                            src={course.coverUrl}
                            alt={course.title}
                            className="object-cover w-full h-full"
                          />
                        ) : (
                          <div className="flex items-center justify-center w-full h-full">
                            <BookOpen className="w-12 h-12 text-muted-foreground/30" />
                          </div>
                        )}

                        {/* 课程状态标签 */}
                        <div className="absolute bottom-2 left-2">
                          <CourseStatusBadge status={course.status} />
                        </div>
                      </div>

                      {/* 课程信息 */}
                      <div className="flex-1 p-4 flex flex-col justify-between">
                        <div>
                          <h3 className="font-semibold text-lg">{course.title}</h3>
                          <p className="text-sm text-muted-foreground line-clamp-1 mt-1">{course.description || "暂无描述"}</p>
                        </div>

                        <div className="flex items-center gap-4 text-sm">
                          <div className="flex items-center">
                            <Users className="w-4 h-4 mr-1 text-muted-foreground" />
                            <span>{course.studentCount || stats?.learnerCount || 0} 学习</span>
                          </div>
                          <div className="flex items-center">
                            <Star className="w-4 h-4 mr-1 text-yellow-500" />
                            <span>{course.averageRating?.toFixed(1) || '暂无'}</span>
                            {course.ratingCount && course.ratingCount > 0 && (
                              <span className="text-xs text-muted-foreground ml-1">({course.ratingCount})</span>
                            )}
                          </div>
                          <div className="flex items-center">
                            <Heart className="w-4 h-4 mr-1 text-red-500" />
                            <span>{course.favoriteCount || 0} 收藏</span>
                          </div>
                          <div className="flex items-center">
                            <Clock className="w-4 h-4 mr-1 text-blue-500" />
                            <span>{stats ? formatLearningDuration(stats.totalDuration) : '0分钟'}</span>
                          </div>
                        </div>
                      </div>

                      {/* 操作按钮 */}
                      <div className="flex items-center pr-4 gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleViewCourse(course.id);
                          }}
                          title="查看课程"
                        >
                          <Eye className="h-4 w-4" />
                        </Button>

                        <div className="flex items-center justify-center w-8 h-8">
                          <ChevronDown className={`w-5 h-5 transition-transform ${isExpanded ? 'rotate-180' : ''}`} />
                        </div>
                      </div>
                    </div>

                    {/* 展开后显示的详细信息 */}
                    {isExpanded && (
                      <div className="p-4 animate-in slide-in-from-top duration-300">
                        {/* 基础统计数据 */}
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
                          <Card>
                            <CardContent className="p-4 flex flex-col items-center justify-center">
                              <Users className="h-8 w-8 text-primary mb-2" />
                              <div className="text-2xl font-bold">{course.studentCount || 0}</div>
                              <div className="text-xs text-muted-foreground">学习人数</div>
                            </CardContent>
                          </Card>

                          <Card>
                            <CardContent className="p-4 flex flex-col items-center justify-center">
                              <Clock className="h-8 w-8 text-primary mb-2" />
                              <div className="text-2xl font-bold">{formatLearningDuration(stats?.totalDuration || 0)}</div>
                              <div className="text-xs text-muted-foreground">总学习时长</div>
                            </CardContent>
                          </Card>

                          <Card>
                            <CardContent className="p-4 flex flex-col items-center justify-center">
                              <BarChart2 className="h-8 w-8 text-primary mb-2" />
                              <div className="text-2xl font-bold">{stats?.averageProgress?.toFixed(1) || 0}%</div>
                              <div className="text-xs text-muted-foreground">平均学习进度</div>
                            </CardContent>
                          </Card>
                        </div>

                        {/* 高级统计数据 - 使用新组件 */}
                        <CourseLearningStats courseId={course.id} />

                        {/* 最新评论 */}
                        <Card className="mt-6">
                          <CardHeader className="pb-2">
                            <CardTitle className="text-base font-medium flex items-center">
                              <MessageSquare className="w-4 h-4 mr-2" />
                              最新评论
                            </CardTitle>
                          </CardHeader>
                          <CardContent>
                            {reviewsLoading[course.id] ? (
                              <div className="space-y-4">
                                <Skeleton className="h-16 w-full" />
                                <Skeleton className="h-16 w-full" />
                                <Skeleton className="h-16 w-full" />
                              </div>
                            ) : reviewData?.reviews?.length > 0 ? (
                              <div className="space-y-4">
                                {reviewData.reviews.map((review: any) => (
                                  <div key={review.id} className="flex gap-3">
                                    <Avatar>
                                      <AvatarImage src={review.userAvatar} />
                                      <AvatarFallback>{review.username.charAt(0)}</AvatarFallback>
                                    </Avatar>
                                    <div className="flex-1">
                                      <div className="flex items-center justify-between">
                                        <div className="font-medium">{review.username}</div>
                                        <div className="flex">
                                          {Array(5).fill(0).map((_, i) => (
                                            <Star key={i} className={`w-3.5 h-3.5 ${i < review.rating ? 'text-yellow-500 fill-yellow-500' : 'text-muted-foreground'}`} />
                                          ))}
                                        </div>
                                      </div>
                                      <p className="text-sm text-muted-foreground mt-1">{review.content || '用户没有留下评论'}</p>
                                      <div className="text-xs text-muted-foreground mt-1">{formatDate(review.createdAt)}</div>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <div className="text-center py-6 text-muted-foreground">
                                <MessageSquare className="w-8 h-8 mx-auto mb-2" />
                                <p>暂无评论</p>
                              </div>
                            )}
                          </CardContent>
                          {reviewData?.totalReviews > 3 && (
                            <CardFooter>
                              <Button variant="outline" size="sm" className="w-full" onClick={(e) => {
                                e.stopPropagation();
                                // 跳转到评论列表页面
                                router.push(`/dashboard/reviews/${course.id}/course`);
                              }}>
                                查看全部评论 ({reviewData.totalReviews})
                              </Button>
                            </CardFooter>
                          )}
                        </Card>
                      </div>
                    )}
                  </Card>
                );
              })
            )}

            {/* 分页 */}
            {totalPages > 1 && (
              <div className="mt-4">
                <Pagination>
                  <PaginationContent>
                    <PaginationItem>
                      <PaginationPrevious
                        onClick={() => setPage(Math.max(1, currentPage - 1))}
                        className={currentPage === 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>

                    {getPaginationItems().map((item, index) => (
                      <PaginationItem key={index}>
                        {item === '...' ? (
                          <span className="px-4 py-2">...</span>
                        ) : (
                          <PaginationLink
                            onClick={() => typeof item === 'number' && setPage(item)}
                            isActive={currentPage === item}
                          >
                            {item}
                          </PaginationLink>
                        )}
                      </PaginationItem>
                    ))}

                    <PaginationItem>
                      <PaginationNext
                        onClick={() => setPage(Math.min(totalPages, currentPage + 1))}
                        className={currentPage === totalPages ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                      />
                    </PaginationItem>
                  </PaginationContent>
                </Pagination>
              </div>
            )}
          </div>
        </TabsContent>
      </Tabs>

      {/* 删除确认对话框 */}
      <Dialog open={deleteDialog.open} onOpenChange={(open) => setDeleteDialog({ ...deleteDialog, open })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>确认删除</DialogTitle>
            <DialogDescription>
              确定要删除这个课程吗？此操作不可逆。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialog({ open: false, courseId: null })}>
              取消
            </Button>
            <Button variant="destructive" onClick={confirmDelete} disabled={isLoading}>
              {isLoading ? '删除中...' : '确认删除'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}