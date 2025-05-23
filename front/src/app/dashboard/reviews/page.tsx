'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { CourseStatus, CoursePaymentType } from '@/types/course';
import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import {
  CheckCircle,
  Clock,
  FileText,
  Loader2,
  Search,
  Building,
  PlusCircle,
  Filter,
  Eye,
  History,
  DollarSign,
  BookOpen
} from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { formatDate } from '@/utils/date';
import reviewService from '@/services/review-service';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious
} from '@/components/ui/pagination';
import { ReviewPagination } from '@/components/ui/review-pagination';
import { toast } from 'sonner';
import { CoursePreviewDialog } from '@/components/dashboard/reviews/course-preview-dialog';
import ReviewHistoryTable from '@/components/dashboard/review-history/review-history-table';
import { ReviewType } from '@/types/review-record';
import { useAuthStore } from '@/stores/auth-store';

export default function ReviewsPage() {
  const router = useRouter();

  const [activeTab, setActiveTab] = useState<string>('pending');
  const [courses, setCourses] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [keyword, setKeyword] = useState<string>('');
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalItems, setTotalItems] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);

  // 预览对话框状态
  const [showPreviewDialog, setShowPreviewDialog] = useState<boolean>(false);
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);

  // 加载待审核任务
  const loadCourses = async () => {
    try {
      setIsLoading(true);
      setError(null);

      let response;
      if (activeTab === 'pending') {
        // 加载待审核课程
        response = await reviewService.getAllCourses(currentPage, pageSize);
      } else {
        // 加载正在审核中的课程
        response = await reviewService.getReviewingCourses(currentPage, pageSize);
      }

      setCourses(response.content);
      setTotalPages(response.totalPages);
      setTotalItems(response.totalElements);
    } catch (err: any) {
      setError(err.message || '获取课程失败');
      console.error('获取课程失败:', err);
    } finally {
      setIsLoading(false);
    }
  };

  // 根据当前选项卡加载数据
  useEffect(() => {
    loadCourses();
  }, [activeTab, currentPage, pageSize, keyword]);

  // 处理开始审核操作
  const handleStartReview = async (courseId: number) => {
    try {
      await reviewService.startReview(courseId);
      // 导航到全屏预览页面
      router.push(`/dashboard/reviews/${courseId}/preview`);
    } catch (err: any) {
      console.error('开始审核失败:', err);
      toast.error('开始审核失败', {
        description: err.message || '无法开始审核任务'
      });
    }
  };

  // 处理继续审核操作
  const handleContinueReview = (courseId: number) => {
    // 跳转到全屏预览页面，而不是显示对话框
    router.push(`/dashboard/reviews/${courseId}/preview`);
  };

  // 审核完成后刷新数据
  const handleReviewComplete = () => {
    loadCourses();
    toast.success('审核操作已完成');
  };

  // 渲染审核状态标签
  const renderStatusBadge = (status: number) => {
    switch (status) {
      case CourseStatus.PENDING_REVIEW: // 待审核
        return <Badge variant="outline" className="bg-yellow-50 text-yellow-800 border-yellow-300">待审核</Badge>;
      case CourseStatus.REVIEWING: // 审核中
        return <Badge variant="outline" className="bg-blue-50 text-blue-800 border-blue-300">审核中</Badge>;
      case CourseStatus.PUBLISHED: // 已发布
        return <Badge variant="outline" className="bg-green-50 text-green-800 border-green-300">已发布</Badge>;
      case CourseStatus.REJECTED: // 已拒绝
        return <Badge variant="outline" className="bg-red-50 text-red-800 border-red-300">已拒绝</Badge>;
      default:
        return <Badge variant="outline">未知</Badge>;
    }
  };

  // 渲染付费类型
  const renderPaymentType = (type?: CoursePaymentType, price?: number, discountPrice?: number) => {
    // 如果类型未定义或无效，尝试根据价格判断
    if (type === undefined || (type !== CoursePaymentType.FREE && type !== CoursePaymentType.PAID)) {
      type = (price && price > 0) || (discountPrice && discountPrice > 0) ? CoursePaymentType.PAID : CoursePaymentType.FREE;
    }

    if (type === CoursePaymentType.FREE) {
      return <span className="text-green-600 font-medium">免费</span>;
    } else {
      // 有折扣价且折扣价小于原价
      if (discountPrice !== undefined && discountPrice > 0 && price !== undefined && discountPrice < price) {
        return (
          <div className="flex items-center gap-2">
            <span className="text-amber-600 font-medium">¥ {discountPrice}</span>
            <span className="text-muted-foreground line-through text-xs">¥ {price}</span>
          </div>
        );
      } else {
        // 只有原价
        return <span className="text-amber-600 font-medium">¥ {price || 0}</span>;
      }
    }
  };

  return (
    <div className="container py-6">
      <h1 className="text-3xl font-bold tracking-tight mb-6">内容审核</h1>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <div className="flex justify-between items-center mb-4">
          <TabsList>
            <TabsTrigger value="pending">待审核课程</TabsTrigger>
            <TabsTrigger value="reviewing">审核中课程</TabsTrigger>
            <TabsTrigger value="history">审核历史</TabsTrigger>
          </TabsList>

          <div className="flex gap-2">
            <Input
              placeholder="搜索课程..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="w-64"
            />

            <Select value={pageSize.toString()} onValueChange={(value) => setPageSize(Number(value))}>
              <SelectTrigger className="w-[120px]">
                <SelectValue placeholder="每页显示" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="5">5条/页</SelectItem>
                <SelectItem value="10">10条/页</SelectItem>
                <SelectItem value="20">20条/页</SelectItem>
                <SelectItem value="50">50条/页</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <TabsContent value="pending">
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">加载中...</span>
            </div>
          ) : courses.length === 0 ? (
            <div className="text-center py-12 border rounded-md bg-muted/20">
              <Clock className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
              <h3 className="text-lg font-medium mb-2">暂无待审核课程</h3>
              <p className="text-muted-foreground">当有新的审核任务时会显示在这里</p>
            </div>
          ) : (
            <>
              <div className="space-y-4">
                {courses.map((course) => (
                  <Card
                    key={course.id}
                    className="overflow-hidden hover:shadow-sm transition-all duration-200"
                  >
                    <div className="flex items-stretch">
                      {/* 课程图片 */}
                      <div className="w-48 h-32 bg-muted relative flex-shrink-0">
                        {course.coverUrl ? (
                          <img
                            src={course.coverUrl}
                            alt={course.title}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="flex items-center justify-center w-full h-full">
                            <FileText className="w-12 h-12 text-muted-foreground/30" />
                          </div>
                        )}

                        {/* 课程状态标签 */}
                        <div className="absolute bottom-2 left-2">
                          {renderStatusBadge(course.status)}
                        </div>
                      </div>

                      {/* 课程信息 */}
                      <div className="p-4 flex-1 flex flex-col justify-between">
                        <div>
                          <h3 className="font-semibold text-lg line-clamp-1">{course.title}</h3>

                          <div className="flex flex-wrap gap-4 mt-2">
                            <div className="flex items-center text-sm text-muted-foreground">
                              <Building className="h-4 w-4 mr-2" />
                              <span>{course.institution?.name || '未知机构'}</span>
                            </div>

                            <div className="flex items-center text-sm">
                              <DollarSign className="h-4 w-4 mr-2 text-muted-foreground" />
                              {renderPaymentType(course.paymentType, course.price)}
                            </div>

                            <div className="flex items-center text-sm text-muted-foreground">
                              <Clock className="h-4 w-4 mr-2" />
                              <span>提交于: {course.submittedAt ? formatDate(course.submittedAt) : '未知'}</span>
                            </div>
                          </div>
                        </div>

                        <div className="mt-4">
                          <Button
                            onClick={() => handleStartReview(course.id)}
                            size="sm"
                            className="gap-1"
                          >
                            <Eye className="h-4 w-4" />
                            开始审核
                          </Button>
                        </div>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>

              {totalPages > 1 && (
                <div className="mt-4 flex justify-center">
                  <ReviewPagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                  />
                </div>
              )}
            </>
          )}
        </TabsContent>

        <TabsContent value="reviewing">
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">加载中...</span>
            </div>
          ) : courses.length === 0 ? (
            <div className="text-center py-12 border rounded-md bg-muted/20">
              <Clock className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
              <h3 className="text-lg font-medium mb-2">暂无审核中课程</h3>
              <p className="text-muted-foreground">当您开始审核课程后会显示在这里</p>
            </div>
          ) : (
            <>
              <div className="space-y-4">
                {courses.map((course) => (
                  <Card
                    key={course.id}
                    className="overflow-hidden hover:shadow-sm transition-all duration-200"
                  >
                    <div className="flex items-stretch">
                      {/* 课程图片 */}
                      <div className="w-48 h-32 bg-muted relative flex-shrink-0">
                        {course.coverUrl ? (
                          <img
                            src={course.coverUrl}
                            alt={course.title}
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <div className="flex items-center justify-center w-full h-full">
                            <FileText className="w-12 h-12 text-muted-foreground/30" />
                          </div>
                        )}

                        {/* 课程状态标签 */}
                        <div className="absolute bottom-2 left-2">
                          {renderStatusBadge(course.status)}
                        </div>
                      </div>

                      {/* 课程信息 */}
                      <div className="p-4 flex-1 flex flex-col justify-between">
                        <div>
                          <h3 className="font-semibold text-lg line-clamp-1">{course.title}</h3>

                          <div className="flex flex-wrap gap-4 mt-2">
                            <div className="flex items-center text-sm text-muted-foreground">
                              <Building className="h-4 w-4 mr-2" />
                              <span>{course.institution?.name || '未知机构'}</span>
                            </div>

                            <div className="flex items-center text-sm">
                              <DollarSign className="h-4 w-4 mr-2 text-muted-foreground" />
                              {renderPaymentType(course.paymentType, course.price)}
                            </div>
                          </div>
                        </div>

                        <div className="mt-4">
                          <Button
                            onClick={() => handleContinueReview(course.id)}
                            size="sm"
                            className="gap-1"
                          >
                            <Eye className="h-4 w-4" />
                            继续审核
                          </Button>
                        </div>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>

              {totalPages > 1 && (
                <div className="mt-4 flex justify-center">
                  <ReviewPagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                  />
                </div>
              )}
            </>
          )}
        </TabsContent>

        <TabsContent value="history">
          <div className="mb-4">
            <h2 className="text-xl font-semibold mb-2">审核历史记录</h2>
            <p className="text-muted-foreground">
              查看所有课程的审核历史记录
            </p>
          </div>

          <ReviewHistoryTable
            isAdmin={useAuthStore.getState().user?.roles?.some(role => role.code === 'ROLE_ADMIN')}
            reviewType={ReviewType.COURSE}
          />
        </TabsContent>
      </Tabs>

      {/* 课程预览与审核对话框 */}
      {selectedCourseId && (
        <CoursePreviewDialog
          open={showPreviewDialog}
          onOpenChange={setShowPreviewDialog}
          courseId={selectedCourseId}
          onReviewComplete={handleReviewComplete}
        />
      )}
    </div>
  );
}