'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  CardFooter
} from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogClose
} from '@/components/ui/dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  Star,
  StarIcon,
  ThumbsUp,
  MessageSquare,
  MoreVertical,
  Edit,
  Trash2,
  AlertCircle
} from 'lucide-react';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Skeleton } from '@/components/ui/skeleton';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { StarRating } from '@/components/common/star-rating';
import { formatDate } from '@/lib/utils';
import { courseReviewService } from '@/services';
import {
  ReviewVO,
  ReviewSortOrder,
  CourseReviewSectionVO,
  ReviewCreateDTO
} from '@/types/course-review';
import { useAuthStore } from '@/stores/auth-store';
import { toast } from 'sonner';

interface CourseReviewSectionProps {
  courseId: number;
  isUserEnrolled?: boolean;
}

export function CourseReviewSection({ courseId, isUserEnrolled = false }: CourseReviewSectionProps) {
  const router = useRouter();
  const { user, isAuthenticated } = useAuthStore();

  // 状态管理
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reviewSection, setReviewSection] = useState<CourseReviewSectionVO | null>(null);
  const [sortOrder, setSortOrder] = useState<ReviewSortOrder>(ReviewSortOrder.NEWEST);
  const [ratingFilter, setRatingFilter] = useState<number | null>(null);
  const [page, setPage] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 评论相关状态
  const [userReview, setUserReview] = useState<ReviewVO | null>(null);
  const [showReviewDialog, setShowReviewDialog] = useState(false);
  const [showEditDialog, setShowEditDialog] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [reviewContent, setReviewContent] = useState('');
  const [reviewRating, setReviewRating] = useState(5);

  // 加载评论数据
  useEffect(() => {
    const loadReviewData = async () => {
      if (!courseId) return;

      try {
        setLoading(true);
        setError(null);

        // 获取评论区数据
        const data = await courseReviewService.getCourseReviewSection(courseId, page, 10, sortOrder);
        setReviewSection(data);

        // 如果用户已登录，尝试获取用户自己的评论
        if (isAuthenticated && user?.id) {
          try {
            const userReviewData = await courseReviewService.getUserReviewOnCourse(courseId);
            setUserReview(userReviewData);
          } catch (err) {
            console.error('获取用户评论失败:', err);
            // 不设置错误状态，因为用户可能没有评论过这个课程
          }
        }
      } catch (err: any) {
        console.error('加载评论数据失败:', err);
        setError(err.message || '加载评论数据失败');
        toast.error('加载评论数据失败');
      } finally {
        setLoading(false);
      }
    };

    loadReviewData();
  }, [courseId, page, sortOrder, isAuthenticated, user?.id]);

  // 更改排序或筛选
  const handleSortChange = (newSort: ReviewSortOrder) => {
    setSortOrder(newSort);
    setPage(0); // 重置页码
  };

  const handleRatingFilter = (rating: number | null) => {
    setRatingFilter(rating);
    setPage(0); // 重置页码
  };

  // 分页处理
  const handleNextPage = () => {
    if (reviewSection && page < reviewSection.totalPages - 1) {
      setPage(page + 1);
    }
  };

  const handlePrevPage = () => {
    if (page > 0) {
      setPage(page - 1);
    }
  };

  // 打开评论对话框
  const handleOpenReviewDialog = () => {
    if (!isAuthenticated) {
      toast.error('请先登录再评论');
      return;
    }

    if (!isUserEnrolled) {
      toast.error('您需要购买此课程才能评论');
      return;
    }

    if (userReview) {
      // 如果用户已有评论，则打开编辑对话框并填充现有内容
      setReviewContent(userReview.content || '');
      setReviewRating(userReview.rating);
      setShowEditDialog(true);
    } else {
      // 否则打开新建评论对话框
      setReviewContent('');
      setReviewRating(5);
      setShowReviewDialog(true);
    }
  };

  // 提交评论
  const handleSubmitReview = async () => {
    if (!isAuthenticated || !courseId) return;

    try {
      setIsSubmitting(true);

      const reviewData: ReviewCreateDTO = {
        courseId: courseId,
        rating: reviewRating,
        content: reviewContent.trim() || undefined
      };

      const newReview = await courseReviewService.createReview(reviewData);

      // 更新状态
      setUserReview(newReview);

      // 重新加载评论区数据以获取更新后的评分等信息
      const updatedSection = await courseReviewService.getCourseReviewSection(courseId, page, 10, sortOrder);
      setReviewSection(updatedSection);

      setShowReviewDialog(false);
      toast.success('评论发表成功');
    } catch (err: any) {
      console.error('提交评论失败:', err);
      toast.error(err.response?.data?.message || '提交评论失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 编辑评论
  const handleEditReview = async () => {
    if (!isAuthenticated || !userReview || !courseId) return;

    try {
      setIsSubmitting(true);

      const reviewData: ReviewCreateDTO = {
        courseId: courseId,
        rating: reviewRating,
        content: reviewContent.trim() || undefined
      };

      const updatedReview = await courseReviewService.updateReview(userReview.id, reviewData);

      // 更新状态
      setUserReview(updatedReview);

      // 重新加载评论区数据以获取更新后的评分等信息
      const updatedSection = await courseReviewService.getCourseReviewSection(courseId, page, 10, sortOrder);
      setReviewSection(updatedSection);

      setShowEditDialog(false);
      toast.success('评论已更新');
    } catch (err: any) {
      console.error('更新评论失败:', err);
      toast.error(err.response?.data?.message || '更新评论失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 删除评论
  const handleDeleteReview = async () => {
    if (!isAuthenticated || !userReview) return;

    try {
      setIsSubmitting(true);

      await courseReviewService.deleteReview(userReview.id);

      // 更新状态
      setUserReview(null);

      // 重新加载评论区数据以获取更新后的评分等信息
      const updatedSection = await courseReviewService.getCourseReviewSection(courseId, page, 10, sortOrder);
      setReviewSection(updatedSection);

      setShowDeleteDialog(false);
      toast.success('评论已删除');
    } catch (err: any) {
      console.error('删除评论失败:', err);
      toast.error(err.response?.data?.message || '删除评论失败');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 渲染评分分布
  const renderRatingDistribution = () => {
    if (!reviewSection || !reviewSection.stats || !reviewSection.stats.ratingDistribution) {
      return null;
    }

    // 计算最大值以便确定进度条宽度比例
    const maxCount = Math.max(
      ...Object.values(reviewSection.stats.ratingDistribution),
      1 // 防止除以零
    );

    // 确保评分计数存在，否则使用0
    const ratingCount = reviewSection.stats.ratingCount || 0;

    return (
      <div className="space-y-2">
        {[5, 4, 3, 2, 1].map((rating) => {
          const count = reviewSection.stats.ratingDistribution[rating] || 0;
          const percentage = (count / maxCount) * 100;

          return (
            <div key={rating} className="flex items-center gap-2 group cursor-pointer hover:bg-slate-50 p-1 rounded-md transition-colors">
              <div className="flex items-center w-16">
                <span className="text-sm font-medium">{rating}</span>
                <StarIcon className="h-4 w-4 text-yellow-500 fill-yellow-500 ml-1" />
              </div>

              <div className="h-2 bg-slate-100 rounded-full flex-1 overflow-hidden">
                <div
                  className="h-2 bg-yellow-500 rounded-full transition-all duration-500 ease-out"
                  style={{ width: `${percentage}%` }}
                />
              </div>

              <span className="text-sm text-slate-500 w-14 text-right group-hover:font-medium transition-all">
                {count} <span className="text-xs">({ratingCount > 0 ? Math.round((count / ratingCount) * 100) : 0}%)</span>
              </span>
            </div>
          );
        })}
      </div>
    );
  };

  // 渲染单个评论
  const renderReviewItem = (review: ReviewVO) => {
    const isOwner = user?.id === review.userId;
    const isUpdated = review.createdAt !== review.updatedAt;

    return (
      <Card key={review.id} className="mb-4 shadow-sm border border-slate-200 hover:shadow-md transition-shadow">
        <CardHeader className="p-4 pb-2">
          <div className="flex justify-between items-start">
            <div className="flex items-start gap-3">
              <Avatar className="h-10 w-10 border border-slate-200">
                {review.userAvatar ? (
                  <AvatarImage src={review.userAvatar} alt={review.username} />
                ) : null}
                <AvatarFallback className="bg-primary/10 text-primary">
                  {review.username.charAt(0).toUpperCase()}
                </AvatarFallback>
              </Avatar>

              <div>
                <div className="flex items-center gap-2">
                  <span className="font-medium">{review.username}</span>
                  {isOwner && (
                    <Badge variant="secondary" className="text-xs py-0">我的评论</Badge>
                  )}
                </div>

                <div className="flex items-center mt-1">
                  <div className="flex">
                    {Array.from({ length: 5 }).map((_, i) => (
                      <Star
                        key={i}
                        className={`h-4 w-4 ${i < review.rating ? 'text-yellow-500 fill-yellow-500' : 'text-slate-300'}`}
                      />
                    ))}
                  </div>
                  <div className="flex items-center ml-2">
                    <span className="text-sm text-slate-500">
                      {formatDate(review.createdAt)}
                    </span>
                    {isUpdated && (
                      <span className="text-xs text-slate-400 ml-1">(已编辑)</span>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {isOwner && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="icon" className="h-8 w-8">
                    <MoreVertical className="h-4 w-4" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem
                    onClick={() => {
                      setReviewContent(review.content || '');
                      setReviewRating(review.rating);
                      setShowEditDialog(true);
                    }}
                  >
                    <Edit className="h-4 w-4 mr-2" />
                    编辑评论
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => setShowDeleteDialog(true)}
                    className="text-red-500 focus:text-red-500"
                  >
                    <Trash2 className="h-4 w-4 mr-2" />
                    删除评论
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </div>
        </CardHeader>

        <CardContent className="p-4 pt-2">
          {review.content ? (
            <p className="text-slate-700 whitespace-pre-line leading-relaxed">{review.content}</p>
          ) : (
            <p className="text-slate-500 italic">用户未留下评论内容</p>
          )}
        </CardContent>
      </Card>
    );
  };

  // 加载中状态
  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <Skeleton className="h-10 w-36" />
          <Skeleton className="h-10 w-32" />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="md:col-span-1">
            <Card>
              <CardHeader>
                <Skeleton className="h-6 w-32 mb-2" />
                <Skeleton className="h-4 w-24" />
              </CardHeader>
              <CardContent className="space-y-4">
                <Skeleton className="h-24 w-full" />
                <Skeleton className="h-16 w-full" />
              </CardContent>
            </Card>
          </div>

          <div className="md:col-span-2">
            <div className="space-y-4">
              <Skeleton className="h-12 w-full" />
              <Skeleton className="h-[200px] w-full" />
              <Skeleton className="h-[200px] w-full" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  // 错误状态
  if (error) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>加载失败</AlertTitle>
        <AlertDescription>{error}</AlertDescription>
      </Alert>
    );
  }

  // 评论为空状态
  if (!reviewSection) {
    return (
      <Card className="p-6 text-center">
        <div className="py-8">
          <MessageSquare className="h-16 w-16 mx-auto text-slate-300 mb-4" />
          <h3 className="text-xl font-medium text-slate-800 mb-2">暂无评论数据</h3>
          <p className="text-slate-600 mb-6 max-w-md mx-auto">
            这门课程暂时还没有评论。
            {isAuthenticated ? (
              isUserEnrolled ?
                "成为第一个分享学习体验的用户吧！" :
                "购买课程后，您可以分享您的学习体验。"
            ) : "登录并购买课程后，您可以分享您的学习体验。"}
          </p>

          {isAuthenticated ? (
            isUserEnrolled ? (
              <Button onClick={handleOpenReviewDialog} className="mx-auto">
                <MessageSquare className="h-4 w-4 mr-2" />
                发表第一条评论
              </Button>
            ) : (
              <Button variant="outline" className="mx-auto">
                购买课程
              </Button>
            )
          ) : (
            <Button variant="outline" className="mx-auto" onClick={() => router.push('/auth/login')}>
              登录账号
            </Button>
          )}
        </div>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* 顶部操作栏 */}
      <div className="flex justify-between items-center">
        <h3 className="text-xl font-bold text-slate-800">
          课程评论 ({reviewSection.totalReviews})
        </h3>

        <Button
          onClick={handleOpenReviewDialog}
          disabled={!isAuthenticated || !isUserEnrolled}
          title={!isAuthenticated ? "请登录后评论" : !isUserEnrolled ? "购买课程后才能评论" : ""}
        >
          <MessageSquare className="h-4 w-4 mr-2" />
          {userReview ? '编辑我的评论' : '发表评论'}
        </Button>
      </div>

      {/* 未购买课程提示 */}
      {isAuthenticated && !isUserEnrolled && (
        <Alert className="mb-4 bg-yellow-50 border border-yellow-200">
          <AlertCircle className="h-4 w-4 text-yellow-600" />
          <AlertTitle className="text-yellow-800">您尚未购买此课程</AlertTitle>
          <AlertDescription className="text-yellow-700">
            购买课程后，您将可以对课程进行评分和评论，分享您的学习体验。
          </AlertDescription>
        </Alert>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* 左侧评分统计区域 */}
        <div className="md:col-span-1">
          <Card className="border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <CardHeader>
              <CardTitle className="text-lg font-semibold">总体评分</CardTitle>
              <CardDescription>
                基于 {reviewSection.stats.ratingCount} 个用户评价
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* 总体评分展示 */}
              <div className="flex items-center justify-center">
                <div className="text-center">
                  <div className="text-5xl font-bold text-slate-800 mb-1">
                    {reviewSection.stats.averageRating !== null && reviewSection.stats.averageRating !== undefined
                      ? reviewSection.stats.averageRating.toFixed(1)
                      : '0.0'}
                  </div>
                  <div className="flex justify-center mb-2">
                    {Array.from({ length: 5 }).map((_, i) => (
                      <Star
                        key={i}
                        className={`h-6 w-6 ${
                          reviewSection.stats.averageRating && i < Math.floor(reviewSection.stats.averageRating)
                            ? 'text-yellow-500 fill-yellow-500'
                            : reviewSection.stats.averageRating && i < reviewSection.stats.averageRating
                              ? 'text-yellow-500 fill-yellow-500 opacity-50'
                              : 'text-slate-200'
                        }`}
                      />
                    ))}
                  </div>
                  <p className="text-sm text-slate-500">
                    {reviewSection.stats.ratingCount} 条评价
                  </p>
                </div>
              </div>

              {/* 评分分布 */}
              <div className="bg-slate-50 p-3 rounded-lg">
                <h4 className="text-sm font-medium text-slate-700 mb-3">评分分布</h4>
                {renderRatingDistribution()}
              </div>
            </CardContent>
            <CardFooter className="flex flex-col items-start gap-2 bg-slate-50 border-t border-slate-100 rounded-b-lg">
              <p className="text-sm font-medium text-slate-700">按评分筛选</p>
              <div className="flex flex-wrap gap-2">
                <Button
                  variant={ratingFilter === null ? "default" : "outline"}
                  size="sm"
                  onClick={() => handleRatingFilter(null)}
                  className={ratingFilter === null ? "bg-primary hover:bg-primary/90" : ""}
                >
                  全部
                </Button>
                {[5, 4, 3, 2, 1].map(rating => (
                  <Button
                    key={rating}
                    variant={ratingFilter === rating ? "default" : "outline"}
                    size="sm"
                    onClick={() => handleRatingFilter(rating)}
                    className={ratingFilter === rating ? "bg-primary hover:bg-primary/90" : ""}
                  >
                    {rating} <Star className="h-3 w-3 ml-1 inline-block" fill={ratingFilter === rating ? "white" : "none"} />
                  </Button>
                ))}
              </div>
            </CardFooter>
          </Card>
        </div>

        {/* 右侧评论列表区域 */}
        <div className="md:col-span-2">
          {/* 排序选项 */}
          <div className="bg-slate-50 p-3 rounded-md mb-4 flex items-center justify-between">
            <span className="text-sm text-slate-500">排序方式：</span>
            <Tabs
              value={sortOrder}
              onValueChange={(v) => handleSortChange(v as ReviewSortOrder)}
              className="w-auto"
            >
              <TabsList className="bg-slate-200">
                <TabsTrigger value={ReviewSortOrder.NEWEST} className="text-sm">
                  最新
                </TabsTrigger>
                <TabsTrigger value={ReviewSortOrder.HIGHEST_RATING} className="text-sm">
                  最高评分
                </TabsTrigger>
                <TabsTrigger value={ReviewSortOrder.LOWEST_RATING} className="text-sm">
                  最低评分
                </TabsTrigger>
              </TabsList>
            </Tabs>
          </div>

          {/* 评论列表 */}
          <div className="space-y-4">
            {reviewSection.reviews.length > 0 ? (
              <>
                {reviewSection.reviews.map(review => renderReviewItem(review))}

                {/* 分页 */}
                {reviewSection.totalPages > 1 && (
                  <div className="flex justify-between items-center mt-6">
                    <Button
                      variant="outline"
                      onClick={handlePrevPage}
                      disabled={page === 0}
                    >
                      上一页
                    </Button>
                    <span className="text-sm text-slate-500">
                      第 {page + 1} 页 / 共 {reviewSection.totalPages} 页
                    </span>
                    <Button
                      variant="outline"
                      onClick={handleNextPage}
                      disabled={page >= reviewSection.totalPages - 1}
                    >
                      下一页
                    </Button>
                  </div>
                )}
              </>
            ) : (
              <Card className="py-8">
                <div className="text-center">
                  <MessageSquare className="h-12 w-12 mx-auto text-slate-300 mb-3" />
                  <h3 className="text-lg font-medium text-slate-700 mb-1">暂无评论</h3>
                  <p className="text-slate-500 mb-4">
                    {ratingFilter !== null
                      ? `没有 ${ratingFilter} 星的评价`
                      : '这门课程暂时还没有评论'}
                  </p>
                  {isAuthenticated ? (
                    isUserEnrolled && !userReview ? (
                      <Button
                        variant="outline"
                        onClick={handleOpenReviewDialog}
                        className="mx-auto"
                      >
                        成为第一个评论的用户
                      </Button>
                    ) : !isUserEnrolled ? (
                      <p className="text-sm text-slate-500">购买课程后可以发表评论</p>
                    ) : null
                  ) : (
                    <Button
                      variant="outline"
                      className="mx-auto"
                      onClick={() => router.push('/auth/login')}
                    >
                      登录后评论
                    </Button>
                  )}
                </div>
              </Card>
            )}
          </div>
        </div>
      </div>

      {/* 新建评论对话框 */}
      <Dialog open={showReviewDialog} onOpenChange={setShowReviewDialog}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>评价课程</DialogTitle>
            <DialogDescription>
              分享您对这门课程的学习体验和建议
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <h4 className="text-sm font-medium">课程评分</h4>
              <StarRating
                rating={reviewRating}
                onChange={setReviewRating}
                size="large"
              />
            </div>

            <Separator />

            <div className="space-y-2">
              <h4 className="text-sm font-medium">评论内容 (选填)</h4>
              <Textarea
                placeholder="分享您对这门课程的看法和建议..."
                value={reviewContent}
                onChange={(e) => setReviewContent(e.target.value)}
                rows={6}
              />
            </div>
          </div>

          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">取消</Button>
            </DialogClose>
            <Button onClick={handleSubmitReview} disabled={isSubmitting}>
              {isSubmitting ? '提交中...' : '提交评论'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 编辑评论对话框 */}
      <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>编辑评论</DialogTitle>
            <DialogDescription>
              您可以更新对这门课程的评分和评论内容
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <h4 className="text-sm font-medium">课程评分</h4>
              <StarRating
                rating={reviewRating}
                onChange={setReviewRating}
                size="large"
              />
            </div>

            <Separator />

            <div className="space-y-2">
              <h4 className="text-sm font-medium">评论内容 (选填)</h4>
              <Textarea
                placeholder="分享您对这门课程的看法和建议..."
                value={reviewContent}
                onChange={(e) => setReviewContent(e.target.value)}
                rows={6}
              />
            </div>
          </div>

          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">取消</Button>
            </DialogClose>
            <Button onClick={handleEditReview} disabled={isSubmitting}>
              {isSubmitting ? '更新中...' : '更新评论'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 删除评论对话框 */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent className="sm:max-w-[450px]">
          <DialogHeader>
            <DialogTitle>删除评论</DialogTitle>
            <DialogDescription>
              确定要删除您对这门课程的评论吗？此操作无法撤销。
            </DialogDescription>
          </DialogHeader>

          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">取消</Button>
            </DialogClose>
            <Button
              variant="destructive"
              onClick={handleDeleteReview}
              disabled={isSubmitting}
            >
              {isSubmitting ? '删除中...' : '确认删除'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}