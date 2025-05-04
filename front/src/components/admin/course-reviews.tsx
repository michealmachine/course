'use client';

import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Skeleton } from '@/components/ui/skeleton';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { Star, User, MessageSquare, ThumbsUp, Flag, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { request } from '@/services/api';
import { ApiResponse, PaginationResult } from '@/types/api';

interface ReviewVO {
  id: number;
  userId: number;
  username: string;
  avatar: string;
  rating: number;
  content: string;
  createdAt: string;
  likeCount: number;
  isReported: boolean;
}

interface CourseReviewsProps {
  courseId: number;
  stats: any;
}

export function CourseReviews({ courseId, stats }: CourseReviewsProps) {
  const [reviews, setReviews] = useState<ReviewVO[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    if (courseId) {
      fetchReviews();
    }
  }, [courseId, currentPage]);

  const fetchReviews = async () => {
    setIsLoading(true);
    try {
      // 由于后端API尚未实现，使用模拟数据

      // 模拟API调用延迟
      await new Promise(resolve => setTimeout(resolve, 500));

      // 生成模拟评价数据
      const mockReviews: ReviewVO[] = [
        {
          id: 1,
          userId: 101,
          username: '张三',
          avatar: '',
          rating: 5,
          content: '这门课程非常棒，内容丰富，讲解清晰，非常推荐！',
          createdAt: '2023-01-15T10:30:00',
          likeCount: 12,
          isReported: false
        },
        {
          id: 2,
          userId: 102,
          username: '李四',
          avatar: '',
          rating: 4,
          content: '课程内容不错，但是有些地方讲解不够详细，希望能够改进。',
          createdAt: '2023-01-10T15:20:00',
          likeCount: 5,
          isReported: false
        },
        {
          id: 3,
          userId: 103,
          username: '王五',
          avatar: '',
          rating: 3,
          content: '课程内容一般，有些概念讲解不够深入，希望能够增加更多的实例。',
          createdAt: '2023-01-05T09:15:00',
          likeCount: 2,
          isReported: true
        }
      ];

      setReviews(mockReviews);
      setTotalPages(3); // 模拟总页数
    } catch (error) {
      console.error('获取评价列表出错:', error);
      toast.error('获取评价列表出错');
    } finally {
      setIsLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }).map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${i < rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'}`}
      />
    ));
  };

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-medium">评价管理</h3>

      <div className="grid gap-4 md:grid-cols-3">
        <Card className="md:col-span-1">
          <CardHeader>
            <CardTitle>评分分布</CardTitle>
            <CardDescription>
              总评分: {stats ? (
                <span className="font-medium">{stats.ratingDistribution ?
                  (Object.entries(stats.ratingDistribution).reduce((acc: number, [key, value]: [string, any]) =>
                    acc + (parseInt(key) * value), 0) /
                    Object.values(stats.ratingDistribution).reduce((acc: number, value: any) => acc + value, 0)).toFixed(1) :
                  '0.0'
                }</span>
              ) : '-'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Skeleton key={i} className="h-6 w-full" />
                ))}
              </div>
            ) : stats && stats.ratingDistribution ? (
              <div className="space-y-3">
                {[5, 4, 3, 2, 1].map((rating) => (
                  <div key={rating} className="space-y-1">
                    <div className="flex items-center justify-between text-sm">
                      <div className="flex items-center">
                        <span className="mr-2">{rating}</span>
                        <Star className="h-4 w-4 text-yellow-400 fill-yellow-400" />
                      </div>
                      <span>{(stats.ratingDistribution[rating] * 100).toFixed(0)}%</span>
                    </div>
                    <Progress value={stats.ratingDistribution[rating] * 100} className="h-2" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="flex items-center justify-center h-40 text-muted-foreground">
                暂无评分数据
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle>评价列表</CardTitle>
            <CardDescription>
              共 {reviews.length} 条评价
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-6">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Skeleton className="h-10 w-10 rounded-full" />
                      <div className="space-y-1">
                        <Skeleton className="h-4 w-[100px]" />
                        <Skeleton className="h-3 w-[60px]" />
                      </div>
                    </div>
                    <Skeleton className="h-4 w-full" />
                    <Skeleton className="h-4 w-3/4" />
                  </div>
                ))}
              </div>
            ) : reviews.length > 0 ? (
              <div className="space-y-6">
                {reviews.map((review) => (
                  <div key={review.id} className="space-y-2">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center space-x-2">
                        <Avatar className="h-10 w-10">
                          <AvatarImage src={review.avatar} alt={review.username} />
                          <AvatarFallback>
                            <User className="h-5 w-5" />
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <div className="font-medium">{review.username}</div>
                          <div className="flex items-center space-x-1 text-sm text-muted-foreground">
                            <div className="flex">
                              {renderStars(review.rating)}
                            </div>
                            <span>•</span>
                            <span>{formatDate(review.createdAt)}</span>
                          </div>
                        </div>
                      </div>
                      {review.isReported && (
                        <Badge variant="outline" className="bg-red-50 text-red-700 border-red-200">
                          <Flag className="h-3 w-3 mr-1" />
                          已举报
                        </Badge>
                      )}
                    </div>

                    <p className="text-sm">{review.content}</p>

                    <div className="flex items-center justify-between pt-2">
                      <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                        <div className="flex items-center">
                          <ThumbsUp className="h-3 w-3 mr-1" />
                          <span>{review.likeCount}</span>
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <Button variant="outline" size="sm">
                          <MessageSquare className="h-3 w-3 mr-1" />
                          回复
                        </Button>
                        {review.isReported ? (
                          <Button variant="outline" size="sm">
                            忽略举报
                          </Button>
                        ) : (
                          <Button variant="outline" size="sm" className="text-red-600 hover:text-red-700">
                            <Flag className="h-3 w-3 mr-1" />
                            删除
                          </Button>
                        )}
                      </div>
                    </div>

                    <Separator className="mt-4" />
                  </div>
                ))}

                {totalPages > 1 && (
                  <div className="flex justify-center pt-4">
                    <div className="flex space-x-1">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                        disabled={currentPage === 0}
                      >
                        上一页
                      </Button>
                      {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => {
                        const pageNumber = i;
                        return (
                          <Button
                            key={i}
                            variant={currentPage === pageNumber ? "default" : "outline"}
                            size="sm"
                            onClick={() => setCurrentPage(pageNumber)}
                          >
                            {pageNumber + 1}
                          </Button>
                        );
                      })}
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                        disabled={currentPage === totalPages - 1}
                      >
                        下一页
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
                <MessageSquare className="h-12 w-12 mb-4" />
                <p>该课程暂无评价</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
