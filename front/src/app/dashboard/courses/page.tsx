'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
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
import { AlertCircle, FileEdit, Plus, Search, Trash2 } from 'lucide-react';
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
import CourseStatusBadge from '@/components/dashboard/courses/CourseStatusBadge';

export default function CoursesPage() {
  const router = useRouter();
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
    loadCourses();
  }, [currentPage, pageSize, filterTitle, filterStatus]);
  
  // 加载课程列表
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
  
  // 创建新课程
  const handleCreateCourse = () => {
    router.push('/dashboard/courses/create');
  };
  
  // 编辑课程
  const handleEditCourse = (id: number) => {
    router.push(`/dashboard/courses/${id}`);
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
      
      {/* 课程列表 */}
      <Card className="p-6">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>课程名称</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead>状态</TableHead>
              <TableHead className="w-[150px]">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {courses.map((course) => (
              <TableRow key={course.id}>
                <TableCell className="font-medium">{course.title}</TableCell>
                <TableCell>{renderPaymentType(course.paymentType, course.price)}</TableCell>
                <TableCell>{course.createdAt ? formatDate(course.createdAt) : '未知时间'}</TableCell>
                <TableCell><CourseStatusBadge status={course.status} /></TableCell>
                <TableCell>
                  <div className="flex space-x-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleEditCourse(course.id)}
                    >
                      <FileEdit className="h-4 w-4" />
                    </Button>
                    
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDeleteClick(course.id)}
                      disabled={course.status !== CourseStatus.DRAFT}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
            
            {/* 空状态 */}
            {courses.length === 0 && !isLoading && (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10">
                  <div className="flex flex-col items-center justify-center space-y-3">
                    <div className="text-muted-foreground">暂无课程数据</div>
                    <Button onClick={handleCreateCourse} variant="outline">
                      <Plus className="mr-2 h-4 w-4" />
                      创建第一个课程
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            )}
            
            {/* 加载状态 */}
            {isLoading && (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10">
                  <div className="flex items-center justify-center">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-gray-900"></div>
                    <span className="ml-2">加载中...</span>
                  </div>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
        
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
      </Card>
      
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