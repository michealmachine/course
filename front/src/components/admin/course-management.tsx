'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { Badge } from '@/components/ui/badge';
import { Loader2, Search, BookOpen, ChevronDown, ChevronRight, Eye, Star, CheckCircle, PlusCircle, Users } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { CourseVO } from '@/types/course';
import { InstitutionVO } from '@/types/institution';
import { ApiResponse, PaginationResult } from '@/types/api';
import { CourseStatsCard } from './course-stats-card';
import { CourseDetailDialog } from './course-detail-dialog';
import { CourseExpandedRow } from './course-expanded-row';
import { CourseRankingChart } from './course-ranking-chart';

export default function CourseManagement() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [institutionFilter, setInstitutionFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [courses, setCourses] = useState<CourseVO[]>([]);
  const [totalCourses, setTotalCourses] = useState(0);
  const [expandedCourseId, setExpandedCourseId] = useState<number | null>(null);
  const [selectedCourse, setSelectedCourse] = useState<CourseVO | null>(null);
  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState(false);
  const [institutions, setInstitutions] = useState<InstitutionVO[]>([]);
  const [stats, setStats] = useState({
    totalCourses: 0,
    publishedCourses: 0,
    newCoursesThisMonth: 0,
    totalLearners: 0
  });

  // 加载课程列表
  useEffect(() => {
    fetchCourses();
  }, [currentPage, pageSize, statusFilter, institutionFilter]);

  // 加载机构列表（用于筛选）
  useEffect(() => {
    fetchInstitutions();
  }, []);

  // 加载统计数据
  useEffect(() => {
    fetchStats();
  }, []);

  const fetchCourses = async () => {
    setIsLoading(true);
    try {
      // 构建查询参数
      const params: any = {
        page: currentPage,
        size: pageSize
      };

      if (searchQuery) {
        params.keyword = searchQuery;
      }

      if (statusFilter !== 'all') {
        params.status = parseInt(statusFilter);
      }

      let url = '/admin/courses';

      // 如果选择了特定机构，使用机构课程API
      if (institutionFilter !== 'all') {
        url = `/admin/courses/institutions/${institutionFilter}/workspace`;
      }

      const response = await request.get<PaginationResult<CourseVO>>(url, { params });

      if (response.data.code === 200 && response.data.data) {
        setCourses(response.data.data.content);
        setTotalPages(response.data.data.totalPages);
        setTotalCourses(response.data.data.totalElements);
      } else {
        toast.error('获取课程列表失败');
      }
    } catch (error) {
      console.error('获取课程列表出错:', error);
      toast.error('获取课程列表出错');
    } finally {
      setIsLoading(false);
    }
  };

  const fetchInstitutions = async () => {
    try {
      const response = await request.get<PaginationResult<InstitutionVO>>('/admin/institutions', {
        params: {
          page: 0,
          size: 100,
          status: 1 // 只获取状态正常的机构
        }
      });

      if (response.data.code === 200 && response.data.data) {
        setInstitutions(response.data.data.content);
      }
    } catch (error) {
      console.error('获取机构列表出错:', error);
    }
  };

  const fetchStats = async () => {
    try {
      // 使用现有的API获取课程列表，然后计算统计数据
      const coursesResponse = await request.get<PaginationResult<CourseVO>>('/admin/courses', {
        params: {
          page: 0,
          size: 1000 // 获取足够多的课程以计算统计数据
        }
      });

      if (coursesResponse.data.code === 200 && coursesResponse.data.data) {
        const courses = coursesResponse.data.data.content;
        const totalCourses = courses.length;

        // 计算已发布课程数
        const publishedCourses = courses.filter(course => course.status === 4).length;

        // 计算本月新增课程数
        const now = new Date();
        const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
        const newCoursesThisMonth = courses.filter(course => {
          const createdAt = new Date(course.createdAt);
          return createdAt >= firstDayOfMonth && createdAt <= now;
        }).length;

        // 计算总学习人次（所有课程的学习人数总和）
        const totalLearners = courses.reduce((sum, course) => sum + (course.learningCount || 0), 0);

        setStats({
          totalCourses,
          publishedCourses,
          newCoursesThisMonth,
          totalLearners
        });
      } else {
        // 如果API调用失败，使用默认数据
        console.warn('无法获取课程列表，使用默认值');
        setStats({
          totalCourses: 0,
          publishedCourses: 0,
          newCoursesThisMonth: 0,
          totalLearners: 0
        });
      }
    } catch (error) {
      console.error('获取统计数据出错:', error);
      // 出错时使用默认数据
      setStats({
        totalCourses: 0,
        publishedCourses: 0,
        newCoursesThisMonth: 0,
        totalLearners: 0
      });
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchCourses();
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value);
    setCurrentPage(0);
  };

  const handleInstitutionFilterChange = (value: string) => {
    setInstitutionFilter(value);
    setCurrentPage(0);
  };

  const handleExpandRow = (courseId: number) => {
    if (expandedCourseId === courseId) {
      setExpandedCourseId(null);
    } else {
      setExpandedCourseId(courseId);
    }
  };

  const handleOpenDetailDialog = (course: CourseVO) => {
    setSelectedCourse(course);
    setIsDetailDialogOpen(true);
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
  };

  const formatPrice = (price: number) => {
    if (price === 0) return '免费';
    return `¥${(price / 100).toFixed(2)}`;
  };

  const getCourseStatusBadge = (status: number) => {
    switch (status) {
      case 1:
        return <Badge variant="outline" className="bg-gray-50 text-gray-700 border-gray-200">草稿</Badge>;
      case 2:
        return <Badge variant="outline" className="bg-yellow-50 text-yellow-700 border-yellow-200">审核中</Badge>;
      case 3:
        return <Badge variant="outline" className="bg-red-50 text-red-700 border-red-200">已拒绝</Badge>;
      case 4:
        return <Badge variant="outline" className="bg-green-50 text-green-700 border-green-200">已发布</Badge>;
      default:
        return <Badge variant="outline">未知</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      {/* 统计卡片 */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <CourseStatsCard
          title="课程总数"
          value={stats.totalCourses}
          icon={<BookOpen className="h-4 w-4" />}
        />
        <CourseStatsCard
          title="已发布课程"
          value={stats.publishedCourses}
          icon={<CheckCircle className="h-4 w-4" />}
          description={`占比 ${stats.totalCourses ? Math.round((stats.publishedCourses / stats.totalCourses) * 100) : 0}%`}
        />
        <CourseStatsCard
          title="本月新增"
          value={stats.newCoursesThisMonth}
          icon={<PlusCircle className="h-4 w-4" />}
        />
        <CourseStatsCard
          title="总学习人次"
          value={stats.totalLearners}
          icon={<Users className="h-4 w-4" />}
        />
      </div>

      {/* 课程排名图表 */}
      <div className="grid gap-4 md:grid-cols-1">
        <CourseRankingChart institutionId={institutionFilter !== 'all' ? parseInt(institutionFilter) : undefined} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>课程列表</CardTitle>
          <CardDescription>
            管理平台上的所有课程，查看详情、学习统计和评价信息。
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <form onSubmit={handleSearch} className="flex gap-2">
              <Input
                placeholder="搜索课程名称..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="max-w-sm"
              />
              <Button type="submit" variant="outline">
                <Search className="h-4 w-4 mr-2" />
                搜索
              </Button>
            </form>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">状态:</span>
                <Select value={statusFilter} onValueChange={handleStatusFilterChange}>
                  <SelectTrigger className="w-[120px]">
                    <SelectValue placeholder="全部" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">全部</SelectItem>
                    <SelectItem value="1">草稿</SelectItem>
                    <SelectItem value="2">审核中</SelectItem>
                    <SelectItem value="3">已拒绝</SelectItem>
                    <SelectItem value="4">已发布</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">机构:</span>
                <Select value={institutionFilter} onValueChange={handleInstitutionFilterChange}>
                  <SelectTrigger className="w-[180px]">
                    <SelectValue placeholder="全部机构" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">全部机构</SelectItem>
                    {institutions.map((institution) => (
                      <SelectItem key={institution.id} value={institution.id.toString()}>
                        {institution.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">加载中...</span>
            </div>
          ) : (
            <>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-10"></TableHead>
                      <TableHead>课程名称</TableHead>
                      <TableHead>所属机构</TableHead>
                      <TableHead>状态</TableHead>
                      <TableHead>价格</TableHead>
                      <TableHead>学习人数</TableHead>
                      <TableHead>收藏数</TableHead>
                      <TableHead>评分</TableHead>
                      <TableHead>创建时间</TableHead>
                      <TableHead className="text-right">操作</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {courses.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={10} className="h-24 text-center">
                          没有找到课程
                        </TableCell>
                      </TableRow>
                    ) : (
                      courses.map((course) => (
                        <React.Fragment key={course.id}>
                          <TableRow>
                            <TableCell>
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => handleExpandRow(course.id)}
                              >
                                {expandedCourseId === course.id ?
                                  <ChevronDown className="h-4 w-4" /> :
                                  <ChevronRight className="h-4 w-4" />
                                }
                              </Button>
                            </TableCell>
                            <TableCell>
                              <div className="flex items-center gap-2">
                                <div className="h-10 w-16 bg-muted rounded overflow-hidden flex-shrink-0">
                                  {course.coverUrl ? (
                                    <img
                                      src={course.coverUrl}
                                      alt={course.title}
                                      className="h-full w-full object-cover"
                                    />
                                  ) : (
                                    <div className="h-full w-full flex items-center justify-center">
                                      <BookOpen className="h-4 w-4 text-muted-foreground" />
                                    </div>
                                  )}
                                </div>
                                <span className="font-medium">{course.title}</span>
                              </div>
                            </TableCell>
                            <TableCell>{course.institutionName || '-'}</TableCell>
                            <TableCell>{getCourseStatusBadge(course.status)}</TableCell>
                            <TableCell>{formatPrice(course.price)}</TableCell>
                            <TableCell>{course.learningCount || 0}</TableCell>
                            <TableCell>{course.favoriteCount || 0}</TableCell>
                            <TableCell>
                              <div className="flex items-center">
                                <Star className="h-4 w-4 text-yellow-400 mr-1 fill-yellow-400" />
                                <span>{course.averageRating?.toFixed(1) || '-'}</span>
                              </div>
                            </TableCell>
                            <TableCell>{formatDate(course.createdAt)}</TableCell>
                            <TableCell className="text-right">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleOpenDetailDialog(course)}
                              >
                                <Eye className="h-4 w-4 mr-1" />
                                查看
                              </Button>
                            </TableCell>
                          </TableRow>
                          {expandedCourseId === course.id && (
                            <TableRow>
                              <TableCell colSpan={10} className="p-0 border-t-0">
                                <CourseExpandedRow course={course} />
                              </TableCell>
                            </TableRow>
                          )}
                        </React.Fragment>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>

              {totalPages > 1 && (
                <div className="flex items-center justify-end space-x-2 py-4">
                  <div className="text-sm text-muted-foreground">
                    共 <span className="font-medium">{totalCourses}</span> 个课程，
                    第 <span className="font-medium">{currentPage + 1}</span> 页，
                    共 <span className="font-medium">{totalPages}</span> 页
                  </div>
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                          className={currentPage === 0 ? 'pointer-events-none opacity-50' : ''}
                        />
                      </PaginationItem>
                      {Array.from({ length: Math.min(5, totalPages) }).map((_, index) => {
                        // 显示当前页附近的页码
                        let pageToShow = currentPage - 2 + index;
                        if (currentPage < 2) {
                          pageToShow = index;
                        } else if (currentPage > totalPages - 3) {
                          pageToShow = totalPages - 5 + index;
                        }

                        // 确保页码在有效范围内
                        if (pageToShow >= 0 && pageToShow < totalPages) {
                          return (
                            <PaginationItem key={pageToShow}>
                              <PaginationLink
                                onClick={() => setCurrentPage(pageToShow)}
                                isActive={currentPage === pageToShow}
                              >
                                {pageToShow + 1}
                              </PaginationLink>
                            </PaginationItem>
                          );
                        }
                        return null;
                      })}
                      <PaginationItem>
                        <PaginationNext
                          onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                          className={currentPage === totalPages - 1 ? 'pointer-events-none opacity-50' : ''}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* 课程详情弹窗 */}
      {selectedCourse && (
        <CourseDetailDialog
          course={selectedCourse}
          open={isDetailDialogOpen}
          onOpenChange={setIsDetailDialogOpen}
        />
      )}
    </div>
  );
}
