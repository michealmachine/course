'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter
} from '@/components/ui/dialog';
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
import { Loader2, Search, BookOpen, Star } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { CourseVO } from '@/types/course';
import { ApiResponse, PaginationResult } from '@/types/api';

interface CourseListDialogProps {
  institutionId: number;
  institutionName: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CourseListDialog({
  institutionId,
  institutionName,
  open,
  onOpenChange
}: CourseListDialogProps) {
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [courses, setCourses] = useState<CourseVO[]>([]);
  const [totalCourses, setTotalCourses] = useState(0);
  const [showWorkspace, setShowWorkspace] = useState(false);

  useEffect(() => {
    if (open && institutionId) {
      fetchCourses();
    }
  }, [open, institutionId, currentPage, pageSize, showWorkspace]);

  const fetchCourses = async () => {
    if (!institutionId) return;

    setIsLoading(true);
    try {
      const params: any = {
        page: currentPage,
        size: pageSize
      };

      if (searchQuery) {
        params.keyword = searchQuery;
      }

      if (statusFilter !== 'all') {
        params.status = statusFilter;
      }

      // 根据是否显示工作区课程选择不同的API
      const url = showWorkspace
        ? `/admin/courses/institutions/${institutionId}/workspace`
        : `/admin/courses/institutions/${institutionId}`;

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

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchCourses();
  };

  const handleStatusFilterChange = (value: string) => {
    setStatusFilter(value);
    setCurrentPage(0);
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
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <BookOpen className="h-5 w-5" />
            {institutionName} 的课程
          </DialogTitle>
          <DialogDescription>
            查看机构的所有课程信息
          </DialogDescription>
        </DialogHeader>

        <div className="flex items-center justify-between py-4">
          <div className="flex items-center gap-2">
            <Button
              variant={showWorkspace ? "default" : "outline"}
              size="sm"
              onClick={() => setShowWorkspace(true)}
            >
              工作区课程
            </Button>
            <Button
              variant={!showWorkspace ? "default" : "outline"}
              size="sm"
              onClick={() => setShowWorkspace(false)}
            >
              已发布课程
            </Button>
          </div>
          <div className="text-sm text-muted-foreground">
            共 <span className="font-medium">{totalCourses}</span> 个课程
          </div>
        </div>

        <div className="flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0 py-2">
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
          {showWorkspace && (
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
          )}
        </div>

        <div className="flex-1 overflow-auto">
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">加载中...</span>
            </div>
          ) : (
            <div className="rounded-md border">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>课程名称</TableHead>
                    <TableHead>状态</TableHead>
                    <TableHead>价格</TableHead>
                    <TableHead>学习人数</TableHead>
                    <TableHead>收藏数</TableHead>
                    <TableHead>评分</TableHead>
                    <TableHead>创建时间</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {courses.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className="h-24 text-center">
                        没有找到课程
                      </TableCell>
                    </TableRow>
                  ) : (
                    courses.map((course) => (
                      <TableRow key={course.id}>
                        <TableCell>
                          <div className="flex items-center space-x-2">
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
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>
          )}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-end space-x-2 py-4">
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

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            关闭
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
